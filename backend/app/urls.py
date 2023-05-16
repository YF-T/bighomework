"""app URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('dongtai/', include('dongtai.urls'))
"""
from django.contrib import admin
from django.urls import path, re_path
from django.views.static import serve
from app.settings import MEDIA_ROOT, STATIC_ROOT
from user import views as user_views
from blog import views as dongtai_views

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', user_views.runhome),
    path('user/register', user_views.register),
    path('user/login', user_views.login), 
    path('user/checklogin', user_views.checklogin), 
    path('user/follow', user_views.followauthor),
    path('user/homepage', user_views.individualinfo),
    path('user/foreigninfo', user_views.showothersinfo),
    path('user/myfollows', user_views.showfollowinglist),
    path('user/tobeupdated', user_views.tobeupdatedinfo),
    path('user/updatemyinfo', user_views.updatemyinfo),
    path('dongtai/message', dongtai_views.getusermessage),
    path('dongtai/create', dongtai_views.create),
    path('dongtai/search', dongtai_views.search),
    path('dongtai/user/favorites', dongtai_views.searchfavorites),
    path('dongtai/user/dongtais', dongtai_views.searchdongtais),
    path('dongtai/dongtai', dongtai_views.open),
    path('dongtai/support', dongtai_views.approvedongtai),
    path('dongtai/comment/create', dongtai_views.createcomment),
    path('dongtai/comment/approve', dongtai_views.approvecomment),
    path('dongtai/image/upload', dongtai_views.uploaddongtaiimage),
    path('prescription/create', user_views.createprescription),
    path('prescription/update', user_views.updateprescription),
    path('prescription/prescription', user_views.openprescription),
    path('prescription/search', user_views.searchprescription),
    path('prescription/delete', user_views.deleteprescription),
    path('prescription/image2prescription', user_views.image2prescription),
    path('inspection/create', user_views.createinspection),
    path('inspection/update', user_views.updateinspection),
    path('inspection/inspection', user_views.openinspection),
    path('inspection/search', user_views.searchinspection),
    path('inspection/delete', user_views.deleteinspection),
    path('inspection/image2inspection', user_views.image2inspection),
    path('inspection/getdiagram', user_views.getindicatordiagram),
    path('diary/create', user_views.creatediary),
    path('diary/update', user_views.updatediary),
    path('diary/diary', user_views.opendiary),
    path('diary/search', user_views.searchdiary),
    path('diary/delete', user_views.deletediary),
    path('diary/emoji', user_views.generateemoji),
    path('diary/image/upload', user_views.uploaddiaryimage),
    path('remind/getqrcode', user_views.getqrcodeforwechat),
    path('remind/wxpusher/update', user_views.updateuidbywxpusher),
    re_path(r'^image/(?P<path>.*)$', serve, {'document_root': MEDIA_ROOT}),
]
