from django.shortcuts import render
from django.http import HttpResponse, JsonResponse
from django.http import QueryDict
from blog.models import *
from user.models import User, GetUserById
from user.jwt import check_login, login_required

import json


@login_required
def create(request):
    info = {
        'title': request.POST.get('title', ''),
        'author': request.user,
        'content': request.POST.get('content', ''),
        'tag': request.POST.get('tag', ''),
        'position': request.POST.get('position', ''),
        'url_images': request.POST.get('url_images', '')
    }
    dongtai, flag = CreateDongTai(info)
    if flag:
        response = JsonResponse({'status': 'success'})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 400
    return response

def open(request):
    dongtai_id = int(request.GET.get('id', ''))
    print(dongtai_id)
    user, flag = check_login(request)
    if not flag:
        # 用户没有登录或登录过期时，把user设置成一个伪类，这样可以实现所有的评论/博客都不点赞
        # 因为实际上是通过比对id来查找当前用户是否点赞，所以只要把id设为None就可以了
        class Fake:
            def __init__(self):
                self.id = None
        user = Fake()
    mydongtai, flag = OpenDongTai(dongtai_id, user)
    comments, flag2 = showcomments(GetDongTaiById(dongtai_id)[0], user) # 因为函数返回的是一个(dongtai, Ture)二元组故要加一个[0]
    if flag and flag2:
        response = JsonResponse({'status': 'success', 'dongtai': mydongtai, 'comments': comments})
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
    flag, dongtailist = SearchDongTai(key, tag, type, user)
    if flag and dongtailist != "empty list":
        response = JsonResponse({'status': 'success',
            'dongtais_time': dongtailist[0],
            'dongtais_thumb': dongtailist[1],
            'dongtais_browse': dongtailist[2],
            'dongtais_comment': dongtailist[3]})
        response.status_code = 200
    elif flag:
        response = JsonResponse({'status': 'success', 
            'dongtais_time':[],
            'dongtais_thumb': [],
            'dongtais_browse': [],
            'dongtais_comment': []})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 400
    return response

@login_required    
def searchfavorites(request):
    dongtais, flag = SearchUserFavorites(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'dongtais': dongtais})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 200
    return response

@login_required    
def searchdongtais(request):
    dongtais, flag = SearchUserDongTais(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'dongtais': dongtais})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 200
    return response
    
@login_required    
def searchuserdongtais(request):
    username = request.GET.get('username', '')
    user = GetUserByName(username)[0]
    print(user)
    dongtais, flag = SearchUserDongTais(user)
    if flag:
        response = JsonResponse({'status': 'success', 'dongtais': dongtais})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'fail'})
        response.status_code = 200
    return response

@login_required
def approvedongtai(request):
    id = int(request.POST.get('id', ''))
    dongtai, flag = GetDongTaiById(id)
    if not flag:
        response = JsonResponse({'status': 'dongtai not found'})
        response.status_code = 400
        return response
    support, flag = ApproveDongTai(dongtai, request.user)
    bool_support = True if support == 'approve' else False

    response = JsonResponse({'status': 'success',
                             'bool_support': bool_support,
                             'num_thumbing_users': dongtai.num_thumbs})
    response.status_code = 200
    return response
    
@login_required
def collectdongtai(request):
    id = int(request.POST.get('id', ''))
    dongtai, flag = GetDongTaiById(id)
    if not flag:
        response = JsonResponse({'status': 'dongtai not found'})
        response.status_code = 400
        return response
    collect, flag = CollectDongTai(dongtai, request.user)
    bool_collect = True if collect == 'approve' else False

    response = JsonResponse({'status': 'success',
                             'bool_collect': bool_collect,
                             'num_collect_users': dongtai.num_collects})
    response.status_code = 200
    return response

# 通过博客返回所有该博客的评论
def showcomments(dongtai: DongTai, user: User):
    comments, flag = GetCommentsByDongTai(dongtai)
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
    dongtai, flag = GetDongTaiById(id)
    if not flag:
        response = JsonResponse({'status': 'dongtai not found'})
        response.status_code = 200
        return response
    info = {'content': content,
            'author': request.user,
            'dongtai': dongtai}
    comment, flag = CreateComment(info)
    comment_info = {'id': comment.id,
                    'author': comment.author.name,
                    'author_image': comment.author.image.url,
                    'content': comment.content,
                    'created_time': comment.created_time,
                    'num_thumbing_users': comment.thumbing_users.count(),
                    'bool_thumb': GetCommentApproveStatus(comment, author)}
    response = JsonResponse({'status': 'success',
                             'num_comment': dongtai.num_comments,
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
def uploaddongtaiimage(request):
    image = request.FILES['image']
    dongtaiimage, flag = CreateDongTaiImage(image)
    if not flag:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
        return response

    response = JsonResponse({'status': 'success',
                             'id': dongtaiimage.id,
                             'url': dongtaiimage.image.url})
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
def sendusermessage(request):
    messages, flag = GetUserRecieveMessage(request.user)
    if flag:
        response = JsonResponse({'status': 'success', 'messages': messages})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response

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

def getchaterlist(request):
    user = request.POST.get('user', '')
    messages, flag = GetChaterList(user)
    if flag:
        response = JsonResponse({'status': 'success', 'chaterList': messages})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response

def newmsgtime(request):
    user = request.POST.get('user', '')
    last_time,flag = NewMsgTime(user)
    print(last_time)
    if flag:
        response = JsonResponse({'status': 'success', 'last_time':last_time})
        response.status_code = 200
    else:
        response = JsonResponse({'status': 'error'})
        response.status_code = 200
    return response
