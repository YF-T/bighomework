from django.db import models
from django.db.models import Q
from django.dispatch import receiver
from django.core.files import File
import datetime
import pandas as pd
import os
import re

from user.models import User, GetUserById, CreateUser, GetUserByName

class DongTai(models.Model):
    title = models.CharField(max_length = 100)  # 文章标题
    author = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='dongtais')  # 文章作者
    created_time = models.CharField(max_length = 20)  # 发表时间，在初始化dongtai时生成
    content = models.TextField()  # 文章内容，暂定字符串形式输入，markdown格式
    tag = models.CharField(max_length = 20)  # 文章对应的标签，暂定一个
    thumbing_users = models.ManyToManyField('user.User', related_name = 'favorites')
       # 点赞这篇文章的用户列表（避免重复点赞），文章存入用户的收藏列表
    browse = models.IntegerField()  # 浏览次数
    num_thumbs = models.IntegerField()  # 点赞数
    num_comments = models.IntegerField()  # 评论数
    num_collects = models.IntegerField()  # 收藏数
    url_images = models.TextField()  # 该条对应的图片


def CreateDongTai(info: dict):
    dongtai = DongTai()
    dongtai.title = info['title']
    dongtai.author = info['author']
    dongtai.content = info['content']
    dongtai.tag = info['tag']
    # 加入用户
    dongtai.author = info['author']
    dongtai.url_images = info['url_images']
    # 调用当前时间，调试时检验一下是否出错
    dongtai.created_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    dongtai.browse = 0
    dongtai.num_thumbs = 0
    dongtai.num_comments = 0  # 初始浏览、点赞、评论数都为0
    dongtai.num_collects = 0  # 初始浏览、点赞、评论数都为0
    dongtai.save()
    for user in dongtai.author.followers.all():
        SendDongTaiMessage(dongtai.author, user, dongtai)
    return dongtai, True

def GetDongTaiById(myid: int):
    try:
        dongtai = DongTai.objects.get(id = myid)
        return dongtai, True
    except:
        return "errors", False

def UpdateDongTaiBrowse(dongtai: DongTai):
    dongtai.browse += 1
    dongtai.save()
    return 'success', True
    # 每次访问一篇文章，就给该文章的访问量+1

def GetDongTaiApproveStatus(dongtai: DongTai, user: User):
    # if user is None:
    #     return False
    return dongtai.thumbing_users.filter(id = user.id).exists()

def GetUserFollowStatus(dongtai: DongTai, user: User):
    if not isinstance(user, User):
        return False
    return user.followings.filter(id = dongtai.author.id).exists()

def OpenDongTai(myid: int, user: User):
    mydongtai, flag = GetDongTaiById(myid)
    if flag:
        UpdateDongTaiBrowse(mydongtai)  # 更新博客访问量
        dongtai_info = {
            'id': mydongtai.id,
            'title': mydongtai.title, 
            'author': mydongtai.author.name,
            'author_image': mydongtai.author.image.url,
            'author_id': mydongtai.author.id,
            'tag' : mydongtai.tag,
            'created_time': mydongtai.created_time,
            'content': mydongtai.content,
            'num_thumbing_users': mydongtai.num_thumbs,
            'browse': mydongtai.browse,
            'num_comment': mydongtai.num_comments,
            'num_collect': mydongtai.num_collects,
            'bool_thumb': GetDongTaiApproveStatus(mydongtai, user),
            'bool_follow': GetUserFollowStatus(mydongtai, user),
            'url_images': mydongtai.url_images
        }
        return dongtai_info, True
    else: 
        return "cannot open dongtai", False

def SearchUserFavorites(user: User):
    user = GetUserById(user.id)
    index = ['id','title','author_id','author__name','author__image','tag','created_time','num_thumbs','browse','num_comments','num_collects','url_images']
    favorites = list(user.favorites.all().values(*index))
    return favorites, True
    
def SearchUserDongTais(user: User):
    user = GetUserById(user.id)
    index = ['id','title','author_id','author__name','author__image','tag','created_time','num_thumbs','browse','num_comments','num_collects','url_images']
    dongtais = list(user.dongtais.all().values(*index))
    return dongtais, True


def SortDongTai(sequence: str, data: pd.DataFrame):
    if data.empty:
        return []
    # data = pd.DataFrame(dongtais)  # 转化为DataFrame形式，方便排序
    data = data.sort_values(by = sequence, ascending = False)
    # 按照给定的sequence排序，默认为降序
    return data

'''
在全部博客中搜索，key为搜索词，tag为给定的标签
返回结果为一个dongtai的list，每条dongtai包括：
'id': <博客id，由数据库自动生成>，
'title': <文章标题>，
'author': <作者>，
'author_image'：<作者头像url>，（可能会改存储方式）
'tag'：<文章标签>，
'created_time'：<创建时间>，
'num_supporting_users'：<点赞数量>，
'browse'：<阅读数>，
'num_comments'：<评论数>
'num_collects'：<收藏数>
'''
def SearchDongTai(key: str, tag: str, type: str, user: User):
    dongtais = []
    temptag = tag
    index = ['id','title','author_id','author__name','author__image','tag','created_time','num_thumbs','browse','num_comments','num_collects','url_images']
    if temptag == '':
        temptag = 'all'
    tpkey = ['default', 'default', 'default', 'default', 'default', 'default']
    if key == None or key == '' or len(key) == key.count(' '):  # 纯空格等于全部搜索
        key = ''
        tpkey[0] = key
    # 一定含有非空格字符，排除掉字符串开头的空格部分
    i = 0
    key_len = len(key)
    while i < key_len:
        if key[i] != ' ':
            break
        else:
            i = i + 1
    key = key[i:]
    key.replace('  ', ' ')
    num = min(key.count(' '), 5)  # 最多只接受6个关键词搜索
    for i in range(num):
        tabflag = key.find(' ')
        tpkey[i] = key[:tabflag]
        key = key[tabflag+1:]
    tpkey[num] = key
    for i in range(1, 6):
        if tpkey[i] == 'default' or tpkey[i] == '' or tpkey[i] == ' ':
            tpkey[i] = tpkey[0]
    # 确定原始集合
    if type == 'all':
        origin_set = DongTai.objects.all()
    elif type == 'mydongtai':
        user = GetUserById(user.id)[0]
        origin_set = user.dongtais.all()
    elif type == 'favorite':
        user = GetUserById(user.id)[0]
        origin_set = user.favorites.all()
    try:
        if temptag != 'all':  # 先进行标签筛选
            dongtais = origin_set.values(*index).\
            filter(Q(tag = temptag, title__icontains=tpkey[0]) | Q(tag = temptag, author__name__icontains=tpkey[0]) | \
                   Q(tag = temptag, title__icontains=tpkey[1]) | Q(tag = temptag, author__name__icontains=tpkey[1]) | \
                   Q(tag = temptag, title__icontains=tpkey[2]) | Q(tag = temptag, author__name__icontains=tpkey[2]) | \
                   Q(tag = temptag, title__icontains=tpkey[3]) | Q(tag = temptag, author__name__icontains=tpkey[3]) | \
                   Q(tag = temptag, title__icontains=tpkey[4]) | Q(tag = temptag, author__name__icontains=tpkey[4]) | \
                   Q(tag = temptag, title__icontains=tpkey[5]) | Q(tag = temptag, author__name__icontains=tpkey[5]))
        else:  # 标签不筛选，返回全部
            dongtais = origin_set.values(*index).\
            filter(Q(title__icontains=tpkey[0]) | Q(author__name__icontains=tpkey[0]) | \
                   Q(title__icontains=tpkey[1]) | Q(author__name__icontains=tpkey[1]) | \
                   Q(title__icontains=tpkey[2]) | Q(author__name__icontains=tpkey[2]) | \
                   Q(title__icontains=tpkey[3]) | Q(author__name__icontains=tpkey[3]) | \
                   Q(title__icontains=tpkey[4]) | Q(author__name__icontains=tpkey[4]) | \
                   Q(title__icontains=tpkey[5]) | Q(author__name__icontains=tpkey[5]))
    except:
        return False, "cannot find dongtais according to tag"
    dongtais_new = list(dongtais)
    if dongtais_new == []:
        return True, "empty list"
    for dongtai_new in dongtais_new:
        dongtai_new['author_name'] = dongtai_new['author__name']
        dongtai_new['author_image'] = '/image/' + dongtai_new['author__image']
    data = pd.DataFrame(dongtais_new)
    try:
        # 四种排序都需要测试并返回，返回的均为dataframe格式，在这转成list格式
        dongtais_time = SortDongTai('created_time', data).to_dict('records')
        dongtais_thumb = SortDongTai('num_thumbs', data).to_dict('records')
        dongtais_browse = SortDongTai('browse', data).to_dict('records')
        dongtais_comment = SortDongTai('num_comments', data).to_dict('records')
        return True, [dongtais_time, dongtais_thumb, dongtais_browse, dongtais_comment]
    except:
        return False, "sort error"

def ApproveDongTai(dongtai: DongTai, user: User):
    if dongtai.thumbing_users.filter(id = user.id).exists():
        dongtai.thumbing_users.remove(user)
        dongtai.num_thumbs -= 1
        dongtai.save()
        return 'cancel approval', True
    else:
        dongtai.thumbing_users.add(user)
        dongtai.num_thumbs += 1
        dongtai.save()
        return 'approve', True


def InitDongTaiDatabase():
    info = {'name':'default', 
            'password':'37a8eec1ce19687d132fe29051dca629d164e2c4958ba141d5f4133a33f0688f', 
            'age': 0, 
            'sex':'M', 
            'identity':'VERIFY', 
            'description':'这是一个平平无奇的平台管理员', 
            'image':File(open('abc.jpg','rb'))}
    author, flag = CreateUser(info)
    info = {
        'title': '欢迎访问聚核力！',
        'author': author,
        'content': '欢迎阅读其它博客！',
        'tag': 'life_experience',
        'image': File(open('abc.jpg', 'rb'))
    }
    CreateDongTai(info)


class DongTaiImage(models.Model):
    image = models.ImageField(upload_to='dongtai/', height_field = None, width_field = None)
    dongtai = models.ForeignKey('DongTai', on_delete=models.CASCADE, null=True)

def CreateDongTaiImage(image):
    dongtaiimage = DongTaiImage()
    dongtaiimage.image = image
    dongtaiimage.save()
    return dongtaiimage, True

def AddDongTaiImageToDongTai(dongtai: DongTai, images: models.QuerySet):
    images.update(dongtai=dongtai)
    return 'success', True

def GetDongTaiImageById(myid: int):
    try:
        dongtaiimage = DongTaiImage.objects.get(id = myid)
        return dongtaiimage, True
    except:
        return "errors", False
 
def GetDongTaiImagesByIds(myids: list):
    try:
        dongtaiimages = DongTaiImage.objects.filter(id__in=myids)
        return dongtaiimages, True
    except:
        return "errors", False
        
@receiver(models.signals.post_delete, sender=DongTaiImage)
def DeleteDongTaiImageFile(sender, instance, **kwargs):
    # 清除文件，因为delete方法在删除时不会自动清除文件
    if instance.image:
        if os.path.isfile(instance.image.path):
            os.remove(instance.image.path)


# 表示评论的类，已经更新到admin.py
class Comment(models.Model):
    content = models.TextField()  # 内容
    created_time = models.CharField(max_length = 20) # 创建时间
    author = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='comments')
    dongtai = models.ForeignKey('DongTai', on_delete=models.CASCADE, related_name='comments')
    num_thumbs = models.IntegerField()  # 点赞数
    thumbing_users = models.ManyToManyField('user.User', related_name='Approved_comments')

def CreateComment(info: dict):
    comment = Comment()
    comment.content = info['content']
    comment.created_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    comment.author = info['author']
    comment.dongtai = info['dongtai']
    comment.num_thumbs = 0
    comment.save()
    comment.dongtai.num_comments += 1
    comment.dongtai.save()
    SendCommentMessage(comment.author, comment.dongtai.author, comment.dongtai, comment)
    return comment, True

def ApproveComment(comment: Comment, user: User):
    if comment.thumbing_users.filter(id = user.id).exists():
        comment.thumbing_users.remove(user)
        comment.num_thumbs -= 1
        comment.save()
        return 'cancel approval', True
    else:
        comment.thumbing_users.add(user)
        comment.num_thumbs += 1
        comment.save()
        return 'approve', True
    
def GetCommentsByDongTai(dongtai: DongTai):
    return dongtai.comments.order_by('-created_time'), True

def GetCommentById(myid: int):
    try:
        comment = Comment.objects.get(id = myid)
        return comment, True
    except:
        return "errors", False
    
# 判断当前用户是否给当前评论点赞
def GetCommentApproveStatus(comment: Comment, user: User):
    return comment.thumbing_users.filter(id = user.id).exists()

def InitCommentDatabase():
    # 初始化评论的模型使用，先预留接口
    pass
    
    
class UserMessage(models.Model):
    TYPE = (('C', 'Comment'), ('B', 'DongTai'))
    from_user = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='send_message') # 发送用户
    to_user = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='recieve_message') # 接收用户
    dongtai = models.ForeignKey('blog.DongTai', on_delete=models.CASCADE, related_name='about_message', null=True) # 相关博客
    message = models.TextField()
    created_time = models.CharField(max_length = 20)
    new = models.BooleanField(default=True)
    message_type = models.CharField(max_length = 1, choices = TYPE)
    
def SendMessage(info):
    usermessage = UserMessage(**info)
    usermessage.created_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    usermessage.save()
    return usermessage, True
    
def SendCommentMessage(from_user, to_user, dongtai, comment):
    info = {
        'from_user': from_user, 
        'to_user': to_user, 
        'dongtai': dongtai, 
        'message_type': 'Comment'
    }
    info['message'] = '用户“%s”评论了您的文章《%s》: \n%s' % (from_user.name, dongtai.title, comment.content)
    return SendMessage(info)
    
def SendDongTaiMessage(from_user, to_user, dongtai):
    info = {
        'from_user': from_user, 
        'to_user': to_user, 
        'dongtai': dongtai, 
        'message_type': 'DongTai'
    }
    info['message'] = '您关注的用户“%s”新发表了一篇文章《%s》，快去看看吧！' % (from_user.name, dongtai.title)
    return SendMessage(info)

def GetUserRecieveMessage(user):
    user = GetUserById(user.id)[0]
    index = ['id','from_user__name','from_user__image','dongtai__title','dongtai__tag','dongtai__id','new','message','message_type','created_time']
    messages = list(user.recieve_message.all().order_by('-created_time').values(*index))
    for message in messages:
        message['user'] = message.pop('from_user__name')
        myurl = '/image/user/' + message.pop('from_user__image')
        message['user_image'] = myurl.replace('user/user', 'user')
        message['dongtai_title'] = message.pop('dongtai__title')
        message['dongtai_tag'] = message.pop('dongtai__tag')
        message['dongtai_id'] = message.pop('dongtai__id')
    user.recieve_message.update(new=False)
    return messages, True


class ChatMessage(models.Model):
    sender = models.CharField(max_length = 20) # 发送用户
    receiver = models.CharField(max_length = 20) # 接收用户
    message = models.TextField()
    created_time = models.CharField(max_length = 20)
    
def SendChat(sender, receiver, message):
    info = {
        'sender': sender, 
        'receiver': receiver, 
        'message': message
    }
    chatmessage = ChatMessage(**info)
    time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    chatmessage.created_time = time
    info["created_time"] = time
    chatmessage.save()
    return info, True

def GetMessageList(user,chater):
    index = ['id','sender','receiver','message','created_time']
    # print(list(ChatMessage.objects.all()))
    msglist = ChatMessage.objects.filter(Q(sender=user,receiver=chater)|Q(sender=chater,receiver=user))
    MessageList = list(msglist.order_by('created_time').values(*index))
    for message in MessageList:
        message['sender'] = message.pop('sender')
        message['receiver'] = message.pop('receiver')
        message['message'] = message.pop('message')
        message['created_time'] = message.pop('created_time')
    return MessageList, True

def GetChaterList(user):
    index = ['id','sender','receiver','message','created_time']
    # print(list(ChatMessage.objects.all()))
    # 查询sender=user时的receiver集合
    sender_receiver_set = ChatMessage.objects.filter(sender=user).values_list('receiver', flat=True)

    # 查询receiver=user时的sender集合
    receiver_sender_set = ChatMessage.objects.filter(receiver=user).values_list('sender', flat=True)

    # 合并两个集合
    result_set = list(set(sender_receiver_set) | set(receiver_sender_set))

    return result_set, True
        