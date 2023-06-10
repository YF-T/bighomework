from django.shortcuts import render
from django.http import HttpResponse, JsonResponse
from user.models import *
from django.core.files import File
import json
import time
from user.jwt import generate_jwt, login_required, check_login
from user.image2text import ocrtableinspection, ocrinspection, ocrprescription
from user.remind.wechatreminder import wx_createqrcode
from user.remind.medicationrecord import send_address_required

# Create your views here.

def runhome(request):
    pass
    return render(request, "home.html")

def register(request):
    username = request.POST.get('username', '')
    password = request.POST.get('password', '')
    info = {'name':username, 
            'password':password, 
            'age':0, 
            'sex':'M', 
            'identity':'COMMON', 
            'description':'无', 
            'image':File(open('./abc.jpg','rb'))}
    _, flag = CreateUser(info)
    if flag:
        response = JsonResponse({'status':'success'})
        response.status_code = 200
    else:
        response = JsonResponse({'status':'error'})
        response.status_code = 200
    return response


def login(request):
    username = request.GET.get('username', '')
    password = request.GET.get('password', '')
    user, flag = GetUserByName(username)
    if flag and password == user.password:
        token = generate_jwt({'id': user.id, 'username': user.name})
        myurl = '/image/user' + user.image.url[6:]
        myurl = myurl.replace('user/user', 'user')
        response = JsonResponse({'status':'success', 'jwt':token, 'image':myurl})
        response.status_code = 200
    elif flag:
        response = JsonResponse({'status':'invalid password'})
        response.status_code = 200
    else:
        response = JsonResponse({'status':'user not found'})
        response.status_code = 200
    return response

def checklogin(request):
    user, flag = check_login(request)
    if not flag:
        response = JsonResponse({'status':'error'})
        response.status_code = 200
        return response
    myurl = '/image/user' + user.image.url[6:]
    myurl = myurl.replace('user/user', 'user')
    name = user.name
    response = JsonResponse({'status':'success', 'name':name, 'image':myurl})
    response.status_code = 200
    return response

@login_required
def followauthor(request):
    id = request.POST.get('username', '')
    print(id)
    author, flag = GetUserByName(id)  # 待关注的博客作者
    if not flag:
        response = JsonResponse({'status': 'author not found'})
        response.status_code = 200
        return response
    user = request.user
    follow, flag = FollowOrUnfollowAuthors(user, author)
    bool_follow = True if follow == 'follow' else False
    response = JsonResponse({'status': 'success',
                            'bool_follow': bool_follow})
    response.status_code = 200
    return response
    

@login_required
def banauthor(request):
    id = request.POST.get('username', '')
    print(id)
    author, flag = GetUserByName(id)  # 待关注的博客作者
    if not flag:
        response = JsonResponse({'status': 'author not found'})
        response.status_code = 200
        return response
    user = request.user
    banned, flag = BanOrDisbanUsers(user, author)
    bool_banned = True if banned == 'ban' else False
    response = JsonResponse({'status': 'success',
                            'bool_banned': bool_banned})
    response.status_code = 200
    return response


# 有条件的话可以将下方函数改成try-except形式
@login_required
def individualinfo(request):  # 返回用户主页的个人信息
    ich = request.user.id
    ich, _ = GetUserById(ich)
    # print(ich.image.url)
    # 此处由后端对用户头像image的url进行字符串处理
    myurl = '/image/user' + ich.image.url[6:]
    myurl = myurl.replace('user/user', 'user')
    # print(myurl)
    myfollow = ich.followings.all()
    length = len(myfollow)
    follow_images = []
    urllist = ''
    # 对关注列表的用户头像，也进行了url字符串操作
    for i in range(length):
        urllist = '/image/user' + myfollow[i].image.url[6:]
        urllist = urllist.replace('/user/user', '/user')
        follow_images.append(urllist)
    info = {
        'name': ich.name,
        'age': ich.age,
        'sex': ich.sex,
        'identity': ich.identity,
        'description': ich.description,
        'image': myurl,
        'follow_num': length,
        'follow_images': follow_images
    }
    response = JsonResponse({'status': True, 'info': info})
    response.status_code = 200
    return response


@login_required
def getfollowings(request):  # 返回用户主页的个人信息
    ich = request.user.id
    ich, _ = GetUserById(ich)
    followings, _ = GetUserFollower(ich)
    response = JsonResponse({'status': True, 'followings': followings})
    response.status_code = 200
    return response

@login_required
def showothersinfo(request):
    id = request.POST.get('username', '')
    print(id)
    author, flag = GetUserByName(id)
    myurl = author.image.url
    if not ('/image/user' in myurl):
        myurl = '/image/user' + myurl[6:]
    myurl = myurl.replace('user/user', 'user')
    user, flag = GetUserById(request.user.id)
    info = {
        'name': author.name,
        'age': author.age,
        'sex': author.sex,
        'identity': author.identity,
        'description': author.description,
        'image': myurl,
        'following': author.followings.count(),
        'follower': author.followers.count(),
        'bool_follow': user.followings.filter(id=author.id).exists(),
        'bool_ban': user.bannings.filter(id=author.id).exists(),
    }
    response = JsonResponse({'status': True, 'info': info})
    response.status_code = 200
    return response
    try:
        pass
    except:
        response = JsonResponse({'status': True, 'info': 'error'})
        response.status_code = 200
        return response

@login_required
def tobeupdatedinfo(request):  # 返回用户主页的个人信息
    try:
        ich = request.user
        myurl = ich.image.url
        if not ('/image/user' in myurl):
            myurl = '/image/user' + myurl[6:]
        myurl = myurl.replace('user/user', 'user')
        info = {
            'name': ich.name,
            'password': '******',
            'age': ich.age,
            'sex': ich.sex,
            'description': ich.description,
            'image': myurl,
            # 此处用户头像image的url不用进行字符串处理，返回后由前端处理
            'email': ich.email,
            'uid': ich.wx_uid
        }
        response = JsonResponse({'status': True, 'info': info})
        response.status_code = 200
        return response
    except:
        response = JsonResponse({'status': True, 'info': 'error'})
        response.status_code = 200
        return response


@login_required
def updatemyinfo(request):  # 更新用户个人信息（不包括头像）
    try:
        ich = request.user.id
        ich, _ = GetUserById(ich)
        password = request.POST.get('password', '')
        if password == '******':
            password = ich.password
        print(password)
        ChangeProfile = request.POST.get('ifChangeImage')
        if ChangeProfile == '1':
            print("success")
            img = request.FILES['image']  # 参考博客封面图
            info = {
                'name': request.POST.get('username', ''),
                'password': password,
                'age': request.POST.get('age', ''),
                'sex': request.POST.get('sex', ''),
                'email': request.POST.get('email', ''),
                'wx_uid': request.POST.get('uid', ''),
                'image': img,
                'description': request.POST.get('description', '')
                }
        else:
            info = {
                'name': request.POST.get('username', ''),
                'password': password,
                'age': request.POST.get('age', ''),
                'sex': request.POST.get('sex', ''),
                'email': request.POST.get('email', ''),
                'wx_uid': request.POST.get('uid', ''),
                'description': request.POST.get('description', '')
                }
        flag, message = VerifyInfo(ich, info)  # message标记发生问题的变量
        if flag:  # 在更新后的信息能通过验证的前提下才能更新
            flag = UpdateUser(ich.id, info)
        if not flag:
            response = JsonResponse({'status': False, 'id': message})
            response.status_code = 200
            return response
        else:
            response = JsonResponse({'status': True, 'id': (int)(ich.id), 'url': GetUserById(ich.id)[0].image.url})
            response.status_code = 200
            return response
    except:
        response = JsonResponse({'status': False, 'id': 'update failed, unknown error'})
        response.status_code = 200
        return response


@login_required
def showfollowinglist(request):  # 返回“关注的人”列表
    ich = request.user.id
    ich, _ = GetUserById(ich)
    try:
        length = len(ich.followings.all())
        if length != 0:
            index = ['id', 'name', 'identity', 'description', 'image']
            author_list = list(ich.followings.values(*index).filter())
            for author in author_list:
                author['image_url'] = '/image/' + author['image']
                author['iffollow'] = ich.followings.filter(id = author['id']).exists()
                author.pop('image')
            response = JsonResponse({'status': True, 'list': author_list})
            response.status_code = 200
            return response
        else:
            response = JsonResponse({'status': True, 'list': []})
            response.status_code = 200
            return response
    except:
        response = JsonResponse({'status': False, 'list': 'error'})
        response.status_code = 200
        return response
    

@login_required
@send_address_required
def createprescription(request):
    # 构造请求字典
    info = {}
    for key in request.POST.keys():
        if key == 'image':
            info[key] = request.FILES[key]
        elif key == 'medicines':
            info[key] = json.loads(request.POST.get(key, ''))
        else:
            info[key] = request.POST.get(key, '')
    info['image'] = request.FILES['image']
    prescription, flag = CreatePrescription(info, user=request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'id': prescription.id})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response


@login_required
def createinspection(request):
    # 构造请求字典
    info = {}
    for key in request.POST.keys():
        if key == 'image':
            info[key] = request.FILES[key]
        elif key == 'indicators':
            info[key] = json.loads(request.POST.get(key, ''))
        else:
            info[key] = request.POST.get(key, '')
    info['image'] = request.FILES['image']
    inspection, flag = CreateInspection(info, user=request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'id': inspection.id})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
    

@login_required
def creatediary(request):
    # 构造请求字典
    info = {}
    for key in request.POST.keys():
        if key != 'id_image':
            info[key] = request.POST.get(key, '')
    id_image = json.loads('[%s]' % request.POST.get('id_image', ''))
    diary, flag = CreateDiary(info, author=request.user)
    AddDiaryImageToDiary(diary, GetDiaryImagesByIds(id_image)[0])
    if flag:
        response = JsonResponse({'status': 'success', 'id': diary.id})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
    

@login_required
def uploaddiaryimage(request):
    image = request.FILES['image']
    diaryimage, flag = CreateDiaryImage(image)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response

    response = JsonResponse({'status': 'success',
                             'id': diaryimage.id,
                             'url': diaryimage.image.url})
    response.status_code = 200
    return response


@login_required
def openprescription(request):
    prescription_id = request.GET.get('id', '')
    prescription, flag = GetPrescriptionById(prescription_id)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    info, flag = OpenPrescription(prescription)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    info['status'] = 'success'
    response = JsonResponse(info)
    response.status_code = 200
    return response


@login_required
def openinspection(request):
    inspection_id = request.GET.get('id', '')
    inspection, flag = GetInspectionById(inspection_id)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    info, flag = OpenInspection(inspection)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    info['status'] = 'success'
    response = JsonResponse(info)
    response.status_code = 200
    return response


@login_required
def opendiary(request):
    diary_id = request.GET.get('id', '')
    diary, flag = GetDiaryById(diary_id)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    info, flag = OpenDiary(diary)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    info['status'] = 'success'
    response = JsonResponse(info)
    response.status_code = 200
    return response
    

@login_required
def searchprescription(request):
    prescriptions, flag = SearchPrescription(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'prescriptions': prescriptions})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
    

@login_required
def searchinspection(request):
    inspections, flag = SearchInspection(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'inspections': inspections})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response


@login_required
def searchdiary(request):
    diaries, flag = SearchDiary(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'diaries': diaries})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
    

@login_required
def image2prescription(request):
    print("IMAGE")
    image = request.FILES['image']
    medicines, flag = ocrprescription(image)
    if flag:
        response = JsonResponse({'status': 'success', 'medicines': medicines})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error', 'medicines': medicines})
        response.status_code = 200
    return response


@login_required
def image2inspection(request):
    image = request.FILES['image']
    indicators, flag = ocrinspection(image)
    if flag:
        response = JsonResponse({'status': 'success', 'indicators': indicators})
        response.status_code = 200
    else:
        indicators2, flag2 = ocrtableinspection(image)
        if flag2:
            response = JsonResponse({'status': 'success', 'indicators': indicators2})
            response.status_code = 200
        else:
            response = JsonResponse({'status': 'error', 'indicators': indicators})
            response.status_code = 200
    return response


@login_required
def getindicatordiagram(request):
    try:
        ich = request.user.id
        ich, _ = GetUserById(ich)
        indicator_name = request.POST.get('indicator')
        begin = request.POST.get('begin')
        end = request.POST.get('end')
        # indicator, _ = GetIndicatorByName(indicator_name)
        flag = PaintDiagram(ich, indicator_name, begin, end)
        if flag == '':
            diagramurl = '/image/emoji/notaindex.png'
            response = JsonResponse({'status': 'success', 'diagram': diagramurl})
            response.status_code = 200
            return response
        x, date, value = flag[0], flag[1], flag[2]
        plt.plot(x, value, marker = 'o', color = 'b')
        plt.xlabel('date')
        plt.xticks(x, date)
        diagramurl = 'image/diagram.png'
        plt.savefig(diagramurl)
        plt.clf()
        # 不同时间创建的指标图不同
        print("url: ", diagramurl + '?time=' + str(time.time()))
        response = JsonResponse({'status': 'success', 'diagram': diagramurl + '?time=' + str(time.time())})
        response.status_code = 200
        return response
    except:
        response = JsonResponse({'status': 'fail', 'diagram': '/image/emoji/notaindex.png'})
        response.status_code = 200
        return response


@login_required    
def getqrcodeforwechat(request):
    qrcode_url, flag = wx_createqrcode(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'qrcode_url': qrcode_url})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
    

def updateuidbywxpusher(request):
    info = json.loads(request.body)
    if info['action'] != 'app_subscribe':
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    wx_uid = info['data']['uid']
    user_id = int(info['data']['extra'])
    flag = UpdateUser(user_id, {'wx_uid': wx_uid})
    if flag:
        response = JsonResponse({'status': 'success'})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
    
@login_required
@send_address_required
def updateprescription(request):
    # 构造请求字典
    info = {}
    for key in request.POST.keys():
        if key == 'image':
            info[key] = request.FILES[key]
        elif key == 'medicines':
            info[key] = json.loads(request.POST.get(key, ''))
        else:
            info[key] = request.POST.get(key, '')
    try:
        info['image'] = request.FILES['image']
    except:
        pass
    if 'id' in info and bool(info['id']):
        id = int(info['id'])
        flag = UpdatePrescription(info)
    else:
        prescription, flag = CreatePrescription(info, user=request.user)
        id = prescription.id
    if flag:
        response = JsonResponse({'status': 'success', 'id': id})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response


@login_required
def updateinspection(request):
    # 构造请求字典
    info = {}
    for key in request.POST.keys():
        if key == 'image':
            info[key] = request.FILES[key]
        elif key == 'indicators':
            info[key] = json.loads(request.POST.get(key, ''))
        else:
            info[key] = request.POST.get(key, '')
    try:
        info['image'] = request.FILES['image']
    except:
        pass
    if 'id' in info and bool(info['id']):
        id = int(info['id'])
        flag = UpdateInspection(info)
    else:
        inspection, flag = CreateInspection(info, user=request.user)
        id = inspection.id
    if flag:
        response = JsonResponse({'status': 'success', 'id': id})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response

@login_required
def updatediary(request):
    # 构造请求字典
    info = {}
    for key in request.POST.keys():
        if key != 'id_image':
            info[key] = request.POST.get(key, '')
    if 'id' in info and bool(info['id']):
        id = int(info['id'])
        flag = UpdateDiary(info)
    else:
        id_image = json.loads('[%s]' % request.POST.get('id_image', ''))
        diary, flag = CreateDiary(info, author=request.user)
        AddDiaryImageToDiary(diary, GetDiaryImagesByIds(id_image)[0])
        id = diary.id
    if flag:
        response = JsonResponse({'status': 'success', 'id': id})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
    

@login_required
def generateemoji(request):
    begin = request.POST.get('begin', '')
    end = request.POST.get('end', '')
    ich = request.user.id
    ich, _ = GetUserById(ich)
    modes, flag = SearchDiaryInPeriod(ich, begin, end)
    if len(modes) > 0:
        level = int((sum(modes) / len(modes)) * 5) + 5
    else:
        level = 5
    if level <=4:
        level += 1
    if level == 10:
        level = 9
    url = '/image/emoji/%d.png' % level
    response = JsonResponse({'status': 'success', 'image_url': url})
    response.status_code = 200
    return response
    
    
@login_required
def deleteprescription(request):
    prescription_id = request.POST.get('id', '')
    prescription, flag = GetPrescriptionById(prescription_id)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    flag = DeletePrescription(prescription)
    if flag:
        response = JsonResponse({'status': 'success'})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response


@login_required
def deleteinspection(request):
    inspection_id = request.POST.get('id', '')
    inspection, flag = GetInspectionById(inspection_id)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    flag = DeleteInspection(inspection)
    if flag:
        response = JsonResponse({'status': 'success'})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response


@login_required
def deletediary(request):
    diary_id = request.POST.get('id', '')
    diary, flag = GetDiaryById(diary_id)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response
    flag = DeleteDiary(diary)
    if flag:
        response = JsonResponse({'status': 'success'})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response

@login_required
def showabnninglist(request):  # 返回“关注的人”列表
    ich = request.user.id
    ich, _ = GetUserById(ich)
    try:
        length = len(ich.bannings.all())
        if length != 0:
            index = ['id', 'name', 'identity', 'description', 'image']
            author_list = ich.bannings.values(*index).filter()
            response = JsonResponse({'status': True, 'list': list(author_list)})
            response.status_code = 200
            return response
        else:
            response = JsonResponse({'status': True, 'list': []})
            response.status_code = 200
            return response
    except:
        response = JsonResponse({'status': False, 'list': 'error'})
        response.status_code = 200
        return response