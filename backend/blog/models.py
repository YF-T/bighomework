from django.db import models
from django.db.models import Q
from django.dispatch import receiver
from django.core.files import File
import datetime
import pandas as pd
import os
import re

from user.models import User, GetUserById, CreateUser

class Blog(models.Model):
    title = models.CharField(max_length = 100)  # 文章标题
    author = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='blogs')  # 文章作者
    created_time = models.CharField(max_length = 20)  # 发表时间，在初始化blog时生成
    content = models.TextField()  # 文章内容，暂定字符串形式输入，markdown格式
    tag = models.CharField(max_length = 20)  # 文章对应的标签，暂定一个
    thumbing_users = models.ManyToManyField('user.User', related_name = 'favorites')
       # 点赞这篇文章的用户列表（避免重复点赞），文章存入用户的收藏列表
    browse = models.IntegerField()  # 浏览次数
    num_thumbs = models.IntegerField()  # 点赞数
    num_comments = models.IntegerField()  # 评论数
    cover_image = models.ImageField(upload_to='blogcover/', height_field = None, width_field = None)   # 存储封面图，可以指定图片大小


def CreateBlog(info: dict):
    blog = Blog()
    blog.title = info['title']
    blog.author = info['author']
    blog.content = info['content']
    blog.tag = info['tag']
    # 加入用户
    blog.author = info['author']
    # 调用当前时间，调试时检验一下是否出错
    blog.created_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    blog.browse = 0
    blog.num_thumbs = 0
    blog.num_comments = 0  # 初始浏览、点赞、评论数都为0
    blog.cover_image = info['image']
    blog.save()
    for user in blog.author.followers.all():
        SendBlogMessage(blog.author, user, blog)
    return blog, True

def GetBlogById(myid: int):
    try:
        blog = Blog.objects.get(id = myid)
        return blog, True
    except:
        return "errors", False

def UpdateBlogBrowse(blog: Blog):
    blog.browse += 1
    blog.save()
    return 'success', True
    # 每次访问一篇文章，就给该文章的访问量+1

def GetBlogApproveStatus(blog: Blog, user: User):
    # if user is None:
    #     return False
    return blog.thumbing_users.filter(id = user.id).exists()

def GetUserFollowStatus(blog: Blog, user: User):
    if not isinstance(user, User):
        return False
    return user.followings.filter(id = blog.author.id).exists()

def OpenBlog(myid: int, user: User):
    myblog, flag = GetBlogById(myid)
    if flag:
        UpdateBlogBrowse(myblog)  # 更新博客访问量
        blog_info = {
            'id': myblog.id,
            'title': myblog.title, 
            'author': myblog.author.name,
            'author_image': myblog.author.image.url,
            'author_id': myblog.author.id,
            'tag' : myblog.tag,
            'created_time': myblog.created_time,
            'content': myblog.content,
            'num_thumbing_users': myblog.num_thumbs,
            'browse': myblog.browse,
            'num_comment': myblog.num_comments,
            'bool_thumb': GetBlogApproveStatus(myblog, user),
            'bool_follow': GetUserFollowStatus(myblog, user),
        }
        return blog_info, True
    else: 
        return "cannot open blog", False

def SearchUserFavorites(user: User):
    user = GetUserById(user.id)
    index = ['id','title','author_id','author__name','author__image','tag','created_time','num_thumbs','browse','num_comments','cover_image']
    favorites = list(user.favorites.all().values(*index))
    return favorites, True
    
def SearchUserBlogs(user: User):
    user = GetUserById(user.id)
    index = ['id','title','author_id','author__name','author__image','tag','created_time','num_thumbs','browse','num_comments','cover_image']
    blogs = list(user.blogs.all().values(*index))
    return blogs, True


def SortBlog(sequence: str, data: pd.DataFrame):
    if data.empty:
        return []
    # data = pd.DataFrame(blogs)  # 转化为DataFrame形式，方便排序
    data = data.sort_values(by = sequence, ascending = False)
    # 按照给定的sequence排序，默认为降序
    return data

'''
在全部博客中搜索，key为搜索词，tag为给定的标签
返回结果为一个blog的list，每条blog包括：
'id': <博客id，由数据库自动生成>，
'title': <文章标题>，
'author': <作者>，
'author_image'：<作者头像url>，（可能会改存储方式）
'tag'：<文章标签>，
'created_time'：<创建时间>，
'num_supporting_users'：<点赞数量>，
'browse'：<阅读数>，
'num_comments'：<评论数>
'''
def SearchBlog(key: str, tag: str, type: str, user: User):
    blogs = []
    temptag = tag
    index = ['id','title','author_id','author__name','author__image','tag','created_time','num_thumbs','browse','num_comments','cover_image']
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
        origin_set = Blog.objects.all()
    elif type == 'myblog':
        user = GetUserById(user.id)[0]
        origin_set = user.blogs.all()
    elif type == 'favorite':
        user = GetUserById(user.id)[0]
        origin_set = user.favorites.all()
    try:
        if temptag != 'all':  # 先进行标签筛选
            blogs = origin_set.values(*index).\
            filter(Q(tag = temptag, title__icontains=tpkey[0]) | Q(tag = temptag, author__name__icontains=tpkey[0]) | \
                   Q(tag = temptag, title__icontains=tpkey[1]) | Q(tag = temptag, author__name__icontains=tpkey[1]) | \
                   Q(tag = temptag, title__icontains=tpkey[2]) | Q(tag = temptag, author__name__icontains=tpkey[2]) | \
                   Q(tag = temptag, title__icontains=tpkey[3]) | Q(tag = temptag, author__name__icontains=tpkey[3]) | \
                   Q(tag = temptag, title__icontains=tpkey[4]) | Q(tag = temptag, author__name__icontains=tpkey[4]) | \
                   Q(tag = temptag, title__icontains=tpkey[5]) | Q(tag = temptag, author__name__icontains=tpkey[5]))
        else:  # 标签不筛选，返回全部
            blogs = origin_set.values(*index).\
            filter(Q(title__icontains=tpkey[0]) | Q(author__name__icontains=tpkey[0]) | \
                   Q(title__icontains=tpkey[1]) | Q(author__name__icontains=tpkey[1]) | \
                   Q(title__icontains=tpkey[2]) | Q(author__name__icontains=tpkey[2]) | \
                   Q(title__icontains=tpkey[3]) | Q(author__name__icontains=tpkey[3]) | \
                   Q(title__icontains=tpkey[4]) | Q(author__name__icontains=tpkey[4]) | \
                   Q(title__icontains=tpkey[5]) | Q(author__name__icontains=tpkey[5]))
    except:
        return False, "cannot find blogs according to tag"
    blogs_new = list(blogs)
    if blogs_new == []:
        return True, "empty list"
    for blog_new in blogs_new:
        blog_new['author_name'] = blog_new['author__name']
        blog_new['author_image'] = '/image/' + blog_new['author__image']
        blog_new['cover_image'] = '/image/' + blog_new['cover_image']
    data = pd.DataFrame(blogs_new)
    try:
        # 四种排序都需要测试并返回，返回的均为dataframe格式，在这转成list格式
        blogs_time = SortBlog('created_time', data).to_dict('records')
        blogs_thumb = SortBlog('num_thumbs', data).to_dict('records')
        blogs_browse = SortBlog('browse', data).to_dict('records')
        blogs_comment = SortBlog('num_comments', data).to_dict('records')
        return True, [blogs_time, blogs_thumb, blogs_browse, blogs_comment]
    except:
        return False, "sort error"

def ApproveBlog(blog: Blog, user: User):
    if blog.thumbing_users.filter(id = user.id).exists():
        blog.thumbing_users.remove(user)
        blog.num_thumbs -= 1
        blog.save()
        return 'cancel approval', True
    else:
        blog.thumbing_users.add(user)
        blog.num_thumbs += 1
        blog.save()
        return 'approve', True


def InitBlogDatabase():
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
    CreateBlog(info)

@receiver(models.signals.post_delete, sender=Blog)
def DeleteBlogFile(sender, instance, **kwargs):
    # 清除文件，因为delete方法在删除时不会自动清除文件
    if instance.cover_image:
        if os.path.isfile(instance.cover_image.path):
            os.remove(instance.cover_image.path)


class BlogImage(models.Model):
    image = models.ImageField(upload_to='blog/', height_field = None, width_field = None)
    blog = models.ForeignKey('Blog', on_delete=models.CASCADE, null=True)

def CreateBlogImage(image):
    blogimage = BlogImage()
    blogimage.image = image
    blogimage.save()
    return blogimage, True

def AddBlogImageToBlog(blog: Blog, images: models.QuerySet):
    images.update(blog=blog)
    return 'success', True

def GetBlogImageById(myid: int):
    try:
        blogimage = BlogImage.objects.get(id = myid)
        return blogimage, True
    except:
        return "errors", False
 
def GetBlogImagesByIds(myids: list):
    try:
        blogimages = BlogImage.objects.filter(id__in=myids)
        return blogimages, True
    except:
        return "errors", False
        
@receiver(models.signals.post_delete, sender=BlogImage)
def DeleteBlogImageFile(sender, instance, **kwargs):
    # 清除文件，因为delete方法在删除时不会自动清除文件
    if instance.image:
        if os.path.isfile(instance.image.path):
            os.remove(instance.image.path)


# 表示评论的类，已经更新到admin.py
class Comment(models.Model):
    content = models.TextField()  # 内容
    created_time = models.CharField(max_length = 20) # 创建时间
    author = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='comments')
    blog = models.ForeignKey('Blog', on_delete=models.CASCADE, related_name='comments')
    num_thumbs = models.IntegerField()  # 点赞数
    thumbing_users = models.ManyToManyField('user.User', related_name='Approved_comments')

def CreateComment(info: dict):
    comment = Comment()
    comment.content = info['content']
    comment.created_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    comment.author = info['author']
    comment.blog = info['blog']
    comment.num_thumbs = 0
    comment.save()
    comment.blog.num_comments += 1
    comment.blog.save()
    SendCommentMessage(comment.author, comment.blog.author, comment.blog, comment)
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
    
def GetCommentsByBlog(blog: Blog):
    return blog.comments.order_by('-created_time'), True

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
    TYPE = (('C', 'Comment'), ('B', 'Blog'))
    from_user = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='send_message') # 发送用户
    to_user = models.ForeignKey('user.User', on_delete=models.CASCADE, related_name='recieve_message') # 接收用户
    blog = models.ForeignKey('blog.Blog', on_delete=models.CASCADE, related_name='about_message', null=True) # 相关博客
    message = models.TextField()
    created_time = models.CharField(max_length = 20)
    new = models.BooleanField(default=True)
    message_type = models.CharField(max_length = 1, choices = TYPE)
    
def SendMessage(info):
    usermessage = UserMessage(**info)
    usermessage.created_time = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    usermessage.save()
    return usermessage, True
    
def SendCommentMessage(from_user, to_user, blog, comment):
    info = {
        'from_user': from_user, 
        'to_user': to_user, 
        'blog': blog, 
        'message_type': 'Comment'
    }
    info['message'] = '用户“%s”评论了您的文章《%s》: \n%s' % (from_user.name, blog.title, comment.content)
    return SendMessage(info)
    
def SendBlogMessage(from_user, to_user, blog):
    info = {
        'from_user': from_user, 
        'to_user': to_user, 
        'blog': blog, 
        'message_type': 'Blog'
    }
    info['message'] = '您关注的用户“%s”新发表了一篇文章《%s》，快去看看吧！' % (from_user.name, blog.title)
    return SendMessage(info)

def GetUserRecieveMessage(user):
    user = GetUserById(user.id)[0]
    index = ['id','from_user__name','from_user__image','blog__title','blog__tag','blog__id','new','message','message_type','created_time']
    messages = list(user.recieve_message.all().order_by('-created_time').values(*index))
    for message in messages:
        message['user'] = message.pop('from_user__name')
        myurl = '/image/user/' + message.pop('from_user__image')
        message['user_image'] = myurl.replace('user/user', 'user')
        message['blog_title'] = message.pop('blog__title')
        message['blog_tag'] = message.pop('blog__tag')
        message['blog_id'] = message.pop('blog__id')
    user.recieve_message.update(new=False)
    return messages, True
        