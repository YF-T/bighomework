from unittest.util import _MAX_LENGTH
from django.db import models
from django.dispatch import receiver
from django.core.files import File
from django.forms import model_to_dict
import datetime
import os
import matplotlib
import matplotlib.pyplot as plt
from user.remind.medicationrecord import add_remind, update_remind, delete_remind

mapping = {'normal':0, 'positive':1, 'negative':-1}

class User(models.Model):
    SEX = (('M', 'Male'), ('F', 'Female'))
    IDENTITY = (  # 身份类别（认证用户/普通用户/游客）
        ('VERIFY', 'VERIFYED USER'),
        ('COMMON', 'COMMON USER'),
        ('VISITOR', 'VISITOR'))
    name = models.CharField(max_length = 30)  # 用户名
    password = models.CharField(max_length = 80)  # 密码
    age = models.IntegerField()  # 年龄
    sex = models.CharField(max_length = 1, choices = SEX)  # 性别
    identity = models.CharField(max_length = 7, choices = IDENTITY)  # 身份类别
    followings = models.ManyToManyField('User', related_name='followers')  # 用户关注的人
    blacklist = models.ManyToManyField('User', related_name='behate')  # 拉黑
    description = models.CharField(max_length = 200)  # 个人介绍
    image = models.ImageField(upload_to='user/')
    wx_uid = models.CharField(default = '', max_length = 50, blank=True)
    email = models.CharField(default = '', max_length = 40, blank=True)
    

def GetUserByName(myname: str):
    try:
        user = User.objects.get(name = myname)
        return user, True
    except:
        return "errors", False


def GetUserById(myid: int):
    try:
        user = User.objects.get(id = myid)
        return user, True
    except:
        return "errors", False
    

def GetUserFollower(user: User):
    index = ['id','name','description','image']
    followings = list(user.followings.all().values(*index))
    for following in followings:
        following['image_url'] = '/image/' + following['image']
        following.pop('image')
    return followings, True

def GetUserFollowStatus(reader: User, author: User):
    return reader.followings.filter(id = author.id).exists()


def FollowOrUnfollowAuthors(reader: User, author: User):
    if GetUserFollowStatus(reader, author):
        reader.followings.remove(author)
        reader.save()
        return 'unfollow', True
    else:
        reader.followings.add(author)
        reader.save()
        return 'follow', True


def CreateUser(info : dict):
    _, flag = GetUserByName(info['name'])
    if(flag):
        return 'error', False
    user = User()
    user.name = info['name']
    user.password = info['password']
    user.age = info['age']
    user.sex = info['sex']
    user.identity = info['identity']  # 原则上所有使用CreateUser函数初始化的用户，其身份都为COMMON
    user.description = info['description']
    user.image = info['image']
    user.save()
    # 创建用户时信息初始化
    return user, True
    
def UpdateUser(id, info):
    print("filter and update")
    if 'image' in info:
        profile = info['image']
        print(type(profile))
        _, flag = CreateProfile(profile)
        print("successfully created profile example")
        # AddProfileToUser(User.objects.filter(id = id)[0], profile)
        User.objects.filter(id = id).update(**info)
        User.objects.filter(id = id)[0].save()
    else:
        User.objects.filter(id = id).update(**info)
    return True


'''检验用户信息的合法性，主要包括：
    用户名：不重名，且不为空
    密码：不为空
'''
def VerifyInfo(ich: User, info: dict):
    if 'name' in info:
        name = info['name']
        if name == '' or name == None:
            return False
        usr, flag = GetUserByName(name)
        if flag and usr.id != ich.id:  # 判断用户名是否已经存在（且不是原用户名）
            return False, 'username'
    if 'password' in info:
        password = info['password']
        if password == '' or password == None:
            return False, 'password'
    return True, 'ok'


def InitDatabase():
    '''
    info = {'name':'default', 
            'password':'default', 
            'age':3, 
            'sex':'M', 
            'identity':'COMMON', 
            'description':'None', 
            'image':File(open('abc.jpg','rb'))}
    CreateUser(info)
    '''
    pass


# 以下部分函数可能不会调用，但需要有一个删除用户头像图片文件的操作
class Profile(models.Model):  # 新建类，用户头像
    image = models.ImageField(upload_to='user/', height_field = None, width_field = None)
    user = models.ForeignKey('User', on_delete=models.CASCADE, null=True)

def CreateProfile(image):
    usrprofile = Profile()
    usrprofile.image = image
    usrprofile.save()
    return usrprofile, True

def AddProfileToUser(usr: User, images: models.QuerySet):  # 将图片与用户绑定
    images.update(user = usr)
    return 'success', True

# 删除本地存储的用户头像图片文件
@receiver(models.signals.post_delete, sender=Profile)
def DeleteBlogImageFile(sender, instance, **kwargs):
    if instance.image:
        if os.path.isfile(instance.image.path):
            os.remove(instance.image.path)


# 处方管理，一个处方包括多种药，每种药单独建一个类
class Prescription(models.Model):
    title = models.CharField(max_length = 30) # 标题
    created_time = models.CharField(max_length = 20)  # 开具时间
    remark = models.TextField()  # 备注
    image = models.ImageField(upload_to='prescription/', height_field = None, width_field = None)  # 处方图片
    user = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='prescriptions') # 用户
    # medicines字段：在Medicine类里生成字段，包含的药

# 药，包括多个用药提醒
class Medicine(models.Model):
    name = models.CharField(max_length = 30) # 药名
    specification = models.CharField(max_length = 30) # 规格
    amount = models.CharField(max_length = 30) # 总量
    prescription = models.ForeignKey('user.Prescription', on_delete=models.CASCADE, related_name='medicines') # 所属处方
    # medication_records字段：在MedicationRecord类生成字段，包含的用药提醒

# 用药提醒    
class MedicationRecord(models.Model):
    frequency = models.IntegerField(default = 1) # 频率/几日一次
    time = models.CharField(max_length = 20) # 提醒的时间，比如14:00
    created_time = models.CharField(max_length = 20) # 本提醒创建时间，由后端创建，遵循'%Y-%m-%d %H:%M:%S'格式
    requirements = models.TextField() # 服药备注需求
    user = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='medication_records') # 用户，用于搜索用药提醒
    medicine = models.ForeignKey('user.Medicine', on_delete=models.CASCADE, related_name='medication_records') # 所属药

# 删除MedicationRecord时需要把已经设置的闹钟删除
@receiver(models.signals.post_delete, sender=MedicationRecord)
def DeleteRemind(sender, instance, **kwargs):
    delete_remind(instance)

# 新建用药提醒    
def CreateMedicationRecord(info, **kwargs):
    info.update(kwargs)
    if 'id' in info:
        info.pop('id')
    now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    info['created_time'] = now
    medication_record = MedicationRecord(**info)
    medication_record.save()
    add_remind(medication_record)
    return medication_record, True

# 新建用药记录
def CreateMedicine(info, **kwargs):
    info.update(kwargs)
    if 'id' in info:
        info.pop('id')
    user = info.pop('user')
    medication_records = info.pop('medication_records')
    medicine = Medicine(**info)
    medicine.save()
    for medication_record_info in medication_records:
        CreateMedicationRecord(medication_record_info, medicine=medicine, user=user)
    return medicine, True
        
# 新建处方记录
def CreatePrescription(info, **kwargs):
    info.update(kwargs)
    if 'id' in info:
        info.pop('id')
    medicines = info.pop('medicines')
    prescription = Prescription(**info)
    prescription.save()
    for medicine_info in medicines:
        CreateMedicine(medicine_info, prescription=prescription, user=kwargs['user'])
    return prescription, True
    
# 更改服药提醒数据
def UpdateMedicationRecord(info):
    id = info.pop('id')
    num = MedicationRecord.objects.filter(id=id).update(**info)
    if num != 1:
        return False
    else:
        medication_record = MedicationRecord.objects.get(id=id)
        update_remind(medication_record)
        return True

# 更改用药数据
def UpdateMedicine(info):
    medication_records = info.pop('medication_records')
    id = info.pop('id')
    num = Medicine.objects.filter(id=id).update(**info)
    if num != 1:
        return False
    medicine = Medicine.objects.get(id=id)
    medication_record_ids = list(medicine.medication_records.values_list('id', flat=True))
    success = True
    for medication_record_info in medication_records:
        if 'id' in medication_record_info and medication_record_info['id']:
            medication_record_ids.remove(int(medication_record_info['id']))
            flag = UpdateMedicationRecord(medication_record_info)
            success = success and flag
        else:
            _, flag = CreateMedicationRecord(medication_record_info, medicine=medicine, user=medicine.prescription.user)
            success = success and flag
    medicine.medication_records.filter(id__in=medication_record_ids).delete()
    return success

# 更改处方数据
def UpdatePrescription(info):
    medicines = info.pop('medicines')
    id = info.pop('id')
    num = Prescription.objects.filter(id=id).update(**info)
    if num != 1:
        return False
    prescription = Prescription.objects.get(id=id)
    medicine_ids = list(prescription.medicines.values_list('id', flat=True))
    success = True
    for medicine_info in medicines:
        if 'id' in medicine_info and medicine_info['id']:
            medicine_ids.remove(int(medicine_info['id']))
            flag = UpdateMedicine(medicine_info)
            success = success and flag
        else:
            _, flag = CreateMedicine(medicine_info, prescription=prescription, user=prescription.user)
            success = success and flag
    prescription.medicines.filter(id__in=medicine_ids).delete()
    return success
    
# 用id查找用药提醒
def GetMedicationRecordById(myid: int):
    try:
        medication_record = MedicationRecord.objects.get(id = myid)
        return medication_record, True
    except:
        return "errors", False

# 用id查找处方
def GetPrescriptionById(myid: int):
    try:
        prescription = Prescription.objects.get(id = myid)
        return prescription, True
    except:
        return "errors", False

# 打开用药提醒
def OpenMedicationRecord(medication_record: MedicationRecord):
    info = model_to_dict(medication_record)
    info.pop('user')
    info.pop('medicine')
    info.pop('created_time')
    return info, True

# 打开用药记录
def OpenMedicine(medicine: Medicine):
    info = model_to_dict(medicine)
    info.pop('prescription')
    info['medication_records'] = list(map(lambda x:OpenMedicationRecord(x)[0], medicine.medication_records.all()))
    return info, True

# 打开处方
def OpenPrescription(prescription: Prescription):
    info = model_to_dict(prescription)
    info.pop('user')
    info['medicines'] = list(map(lambda x:OpenMedicine(x)[0], prescription.medicines.all()))
    info['image_url'] = info.pop('image').url
    return info, True
    
# 搜索（其实就是返回所有）处方
def SearchPrescription(user: User):
    index = ['id','title','created_time','image','remark']
    prescriptions = list(user.prescriptions.order_by('-id').values(*index))
    print(prescriptions)
    for prescription in prescriptions:
        prescription['image_url'] = '/image/' + prescription['image']
        prescription['ps'] = prescription['remark'] if len(prescription['remark']) <= 40 else (prescription['remark'][:40] + '……')
    return prescriptions, True

# 删除处方（同时也会删除药和用药提醒）
def DeletePrescription(prescription: Prescription):
    prescription.delete()
    return True

# 检查单，一个检查单对应很多检查项    
class Inspection(models.Model):
    title = models.CharField(max_length = 30) # 标题
    created_time = models.CharField(max_length = 20)  # 开具时间
    remark = models.TextField()  # 备注
    image = models.ImageField(upload_to='inspection/', height_field = None, width_field = None)  # 检查单图片
    user = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='inspections') # 用户
    # indicators字段：在Indicator类里生成字段，包含的检查项
    
# 检查项
class Indicator(models.Model):
    name = models.CharField(max_length = 30) # 项目名称
    value = models.CharField(max_length = 30) # 检验结果，因为可能是“阴性”所以不能写成int
    theoreticalvalue = models.CharField(max_length = 30) # 参考结果
    perunit = models.CharField(max_length = 12) # 单位
    user = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='indicators') # 用户，绘图时需要
    inspection = models.ForeignKey('user.Inspection', on_delete=models.CASCADE, related_name='indicators') # 所属检查单
    
# 新建检查项记录
def CreateIndicator(info, **kwargs):
    info.update(kwargs)
    if 'id' in info:
        info.pop('id')
    indicator = Indicator(**info)
    indicator.save()
    return indicator, True

# 新建检查单记录
def CreateInspection(info, **kwargs):
    info.update(kwargs)
    if 'id' in info:
        info.pop('id')
    indicators = info.pop('indicators')
    inspection = Inspection(**info)
    inspection.save()
    for indicator_info in indicators:
        CreateIndicator(indicator_info, inspection=inspection, user=kwargs['user'])
    return inspection, True
    
# 更改检查项数据
def UpdateIndicator(info):
    id = info.pop('id')
    num = Indicator.objects.filter(id=id).update(**info)
    if num != 1:
        return False
    else:
        return True
    
# 更改检查单数据
def UpdateInspection(info):
    indicators = info.pop('indicators')
    id = info.pop('id')
    num = Inspection.objects.filter(id=id).update(**info)
    if num != 1:
        return False
    inspection = Inspection.objects.get(id=id)
    indicator_ids = list(inspection.indicators.values_list('id', flat=True))
    success = True
    for indicator_info in indicators:
        if 'id' in indicator_info and indicator_info['id']:
            indicator_ids.remove(int(indicator_info['id']))
            flag = UpdateIndicator(indicator_info)
            success = success and flag
        else:
            _, flag = CreateIndicator(indicator_info, inspection=inspection, user=inspection.user)
            success = success and flag
    inspection.indicators.filter(id__in=indicator_ids).delete()
    return success

# 用id查找检查单
def GetInspectionById(myid: int):
    try:
        inspection = Inspection.objects.get(id = myid)
        return inspection, True
    except:
        return "errors", False

# 用id查找检查项
def GetIndicatorById(myid: int):
    try:
        indicator = Indicator.objects.get(id = myid)
        return indicator, True
    except:
        return "errors", False

def GetIndicatorByName(myname: str):
    try:
        indicator = Indicator.objects.get(id = myname)
        return indicator, True
    except:
        return "errors", False

# 打开检查项
def OpenIndicator(indicator: Indicator):
    info = model_to_dict(indicator)
    info.pop('user')
    info.pop('inspection')
    return info, True
    
# 打开检查单记录
def OpenInspection(inspection: Inspection):
    info = model_to_dict(inspection)
    info.pop('user')
    info['indicators'] = list(map(lambda x:OpenIndicator(x)[0], inspection.indicators.all()))
    info['image_url'] = info.pop('image').url
    return info, True

# 搜索（相当于返回该用户的所有）检查单
def SearchInspection(user: User):
    index = ['id','title','created_time','image','remark']
    inspections = list(user.inspections.order_by('-id').values(*index))
    for inspection in inspections:
        inspection['image_url'] = '/image/' + inspection['image']
        inspection['ps'] = inspection['remark'] if len(inspection['remark']) <= 40 else (inspection['remark'][:40] + '……')
    return inspections, True
    
# 搜索（相当于返回该用户的所有）检查单2
def SearchInspection2(user: User):
    index = ['created_time', 'indicators']
    inspections = list(user.inspections.order_by('created_time').values(*index))
    for inspection in inspections:
        y, m, d = inspection['created_time'].split('/')
        inspection['created_time'] = '%04d/%02d/%02d' % (int(y), int(m), int(d))
    inspections = sorted(inspections, key=lambda x: x['created_time'])
    print(inspections)
    return inspections, True

# 删除检查单（同时也会删除检查项）
def DeleteInspection(inspection: Inspection):
    inspection.delete()
    return True

def PaintDiagram(user: User, indicator: str, start: str, end: str):
    # 格式化日期字符串
    y, m, d = start.split('/')
    start = '%04d/%02d/%02d' % (int(y), int(m), int(d))
    y, m, d = end.split('/')
    end = '%04d/%02d/%02d' % (int(y), int(m), int(d))
    if start >= end:
        return ''
    tempinspection, _ = SearchInspection2(user)
    myinspection = []
    #print(tempinspection)
    for i in range(len(tempinspection)):
        it = tempinspection[i]
        if ((it['created_time'] >= start) and (it['created_time'] <= end)):
            myinspection.append(it)
    if myinspection == []:
        return ''
    myindicator = []
    date = []
    # print('myinspection:')
    # print(myinspection)
    for i in range(len(myinspection)):
        it = myinspection[i]
        indi, _ = GetIndicatorById(it['indicators'])
        if(indi.name == indicator):
            date.append(it['created_time'])
            myindicator.append(indi)
        else:
            continue
    if myindicator == []:
        return ''
    x = []
    for i in range(len(date)):  # 设置画图的横坐标
        x.append(i + 1)
    value = []
    for ict in myindicator:  # 指标可以转化成数值，可以画图
        value.append(float(ict.value))
    return (x, date, value)



# 日记
class Diary(models.Model):
    date = models.CharField(max_length = 20)  # 当天日期
    mode = models.CharField(max_length = 10)  # 心情，几个选项之后再定
    subject = models.CharField(max_length = 30) # 日记主题
    content = content = models.TextField()  # 日记内容，暂定字符串形式输入，markdown格式
    author = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='diaries')  # 日记作者
    
# 日记图片类
class DiaryImage(models.Model):
    image = models.ImageField(upload_to='diary/', height_field = None, width_field = None) # 日记的图片
    diary = models.ForeignKey('user.Diary', on_delete=models.CASCADE, null=True) # 所属日记
    
# 新建日记
def CreateDiary(info, **kwargs):
    info.update(kwargs)
    if 'id' in info:
        info.pop('id')
    diary = Diary(**info)
    diary.save()
    return diary, True

# 新建日记图片
def CreateDiaryImage(image):
    diaryimage = DiaryImage()
    diaryimage.image = image
    diaryimage.save()
    return diaryimage, True

# 更改日记数据
def UpdateDiary(info):
    id = info.pop('id')
    num = Diary.objects.filter(id=id).update(**info)
    if num != 1:
        return False
    else:
        return True
    
# 把日记图片记录入所属日记中
def AddDiaryImageToDiary(diary: Diary, images: models.QuerySet):
    images.update(diary=diary)
    return 'success', True

# 用id查找图片
def GetDiaryImagesByIds(myids: list):
    try:
        diaryimages = DiaryImage.objects.filter(id__in=myids)
        return diaryimages, True
    except:
        return "errors", False
        
# 用id查找检查单
def GetDiaryById(myid: int):
    try:
        diary = Diary.objects.get(id = myid)
        return diary, True
    except:
        return "errors", False

# 打开日记
def OpenDiary(diary: Diary):
    info = model_to_dict(diary)
    info.pop('author')
    return info, True
    
# 搜索（其实就是返回所有）日记
def SearchDiary(user: User):
    index = ['id', 'subject', 'date', 'mode']
    diaries = list(user.diaries.order_by('-id').values(*index))
    return diaries, True

# 搜索在一个日期内的日记
def SearchDiaryInPeriod(user: User, begin, end):
    index = ['date', 'mode']
    diaries = list(user.diaries.all().values(*index))
    def str2date(s):
        y, m, d = s.split('/')
        return int(y)*10000+int(m)*100+int(d)
    begin = str2date(begin)
    end = str2date(end)
    modes = [x['mode'] for x in diaries if begin <= str2date(x['date']) <= end]
    modes = list(map(lambda x:mapping[x], modes))
    return modes, True
    
# 删除日记
def DeleteDiary(diary: Diary):
    diary.delete()
    return True
