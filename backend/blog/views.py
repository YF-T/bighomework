from django.shortcuts import render
from django.http import HttpResponse, JsonResponse
from django.http import QueryDict
from blog.models import *
from user.models import User, GetUserById
from user.jwt import check_login, login_required

import json


@login_required
def create(request):
    title = request.POST.get('title', '')
    author = request.user
    content = request.POST.get('content', '')
    tag = request.POST.get('tag', '')
    img = request.FILES['image']# 这条应该是对的，这条能显示图片
    id_image = json.loads('[%s]' % request.POST.get('id_image', ''))
    info = {'title': title,
            'author': author,
            'content': content,
            'tag': tag,
            'image': img}
    blog, flag = CreateBlog(info)
    AddBlogImageToBlog(blog, GetBlogImagesByIds(id_image)[0])
    if flag:
        response = JsonResponse({'status': 'success'})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 400
    return response

def open(request):
    blog_id = request.GET.get('id', '')
    user, flag = check_login(request)
    if not flag:
        # 用户没有登录或登录过期时，把user设置成一个伪类，这样可以实现所有的评论/博客都不点赞
        # 因为实际上是通过比对id来查找当前用户是否点赞，所以只要把id设为None就可以了
        class Fake:
            def __init__(self):
                self.id = None
        user = Fake()
    myblog, flag = OpenBlog(blog_id, user)
    comments, flag2 = showcomments(GetBlogById(blog_id)[0], user) # 因为函数返回的是一个(blog, Ture)二元组故要加一个[0]
    if flag and flag2:
        response = JsonResponse({'status': 'success', 'blog': myblog, 'comments': comments})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 400
    return response

def search(request):
    key = request.GET.get('key', '')
    tag = request.GET.get('tag', '')
    if 'type' in request.GET.keys():
        type = request.GET.get('type', '')
    else:
        type = 'all'
    user, flag = check_login(request)
    if not flag and type != 'all':
        response = JsonResponse({'status': 'jwt error'})
        response.status_code = 200
        return response
    flag, bloglist = SearchBlog(key, tag, type, user)
    if flag and bloglist != "empty list":
        response = JsonResponse({'status': 'success',
            'blogs_time': bloglist[0],
            'blogs_thumb': bloglist[1],
            'blogs_browse': bloglist[2],
            'blogs_comment': bloglist[3]})
        response.status_code = 200
    elif flag:
        response = JsonResponse({'status': 'success', 
            'blogs_time':[],
            'blogs_thumb': [],
            'blogs_browse': [],
            'blogs_comment': []})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 400
    return response

@login_required    
def searchfavorites(request):
    blogs, flag = SearchUserFavorites(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'blogs': blogs})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 200
    return response

@login_required    
def searchblogs(request):
    blogs, flag = SearchUserBlogs(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'blogs': blogs})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 200
    return response

@login_required
def approveblog(request):
    id = request.POST.get('id', '')
    blog, flag = GetBlogById(id)
    if not flag:
        response = JsonResponse({'status': 'blog not found'})
        response.status_code = 400
        return response
    support, flag = ApproveBlog(blog, request.user)
    bool_support = True if support == 'approve' else False

    response = JsonResponse({'status': 'success',
                             'bool_support': bool_support,
                             'num_thumbing_users': blog.num_thumbs})
    response.status_code = 200
    return response

# 通过博客返回所有该博客的评论
def showcomments(blog: Blog, user: User):
    comments, flag = GetCommentsByBlog(blog)
    if not flag:
        return 'error', False
    comments_info = []
    for comment in comments:
        comment_info = {
            'id': comment.id,
            'author': comment.author.name,
            'author_image': comment.author.image.url,
            'content': comment.content,
            'created_time': comment.created_time,
            'num_thumbing_users': comment.thumbing_users.count(),
            'bool_thumb': GetCommentApproveStatus(comment, user)
        }
        comments_info.append(comment_info)
    return comments_info, True

@login_required
def createcomment(request):
    content = request.POST.get('content', '')
    author = request.user
    id = request.POST.get('id', '')
    blog, flag = GetBlogById(id)
    if not flag:
        response = JsonResponse({'status': 'blog not found'})
        response.status_code = 200
        return response
    info = {'content': content,
            'author': request.user,
            'blog': blog}
    comment, flag = CreateComment(info)
    comment_info = {'id': comment.id,
                    'author': comment.author.name,
                    'author_image': comment.author.image.url,
                    'content': comment.content,
                    'created_time': comment.created_time,
                    'num_thumbing_users': comment.thumbing_users.count(),
                    'bool_thumb': GetCommentApproveStatus(comment, author)}
    response = JsonResponse({'status': 'success',
                             'num_comment': blog.num_comments,
                             'comment': comment_info})
    response.status_code = 200
    return response

@login_required
def approvecomment(request):
    id = request.POST.get('id', '')
    comment, flag = GetCommentById(id)
    if not flag:
        response = JsonResponse({'status': 'comment not found'})
        response.status_code = 200
        return response
    support, flag = ApproveComment(comment, request.user)
    bool_support = True if support == 'approve' else False

    response = JsonResponse({'status': 'success',
                             'bool_support': bool_support,
                             'num_thumbing_users': comment.num_thumbs})
    response.status_code = 200
    return response


@login_required
def uploadblogimage(request):
    image = request.FILES['image']
    blogimage, flag = CreateBlogImage(image)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response

    response = JsonResponse({'status': 'success',
                             'id': blogimage.id,
                             'url': blogimage.image.url})
    response.status_code = 200
    return response

@login_required
def getusermessage(request):
    messages, flag = GetUserRecieveMessage(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'messages': messages})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response

@login_required
def sendchatmsg(request):
    chat_sender = request.POST.get('chat_sender', '')
    chat_receiver = request.POST.get('chat_receiver', '')
    msg = request.POST.get('msg', '')
    message, flag = SendChat(chat_sender,chat_receiver,msg)
    if flag:
        response = JsonResponse({'status': 'success', 'message': message})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response

@login_required
def getmsglist(request):
    user = request.POST.get('user', '')
    chater = request.POST.get('chater', '')
    messages, flag = GetMessageList(user,chater)
    if flag:
        response = JsonResponse({'status': 'success', 'messageList': messages})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
