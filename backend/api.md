## 后端启动

### 第一步
无论是否新建了文件都请先删掉db.sqlite3！
先在后端文件夹下运行如下命令
python manage.py makemigrations
python manage.py migrate
python manage.py runserver
启动项目

### 若出错

若出现形如这样的错误
django.db.utils.OperationalError: no such table: user_user
则按如下步骤解决
如果有则先删掉db.sqlite3，user/migrations文件夹，blog/migrations文件夹，所有文件夹下的__pycache__和__init__。
python manage.py makemigrations user（或者blog）
python manage.py makemigrations blog（或者user）
python manage.py migrate
python manage.py runserver 

## 注册

- 路径：/user/register

- 方法：post

- 参数：

username：通过formdata传输
password：通过formdata传输

- curl例子：

curl -X POST -d "username=admin122121&password=admin" "http://127.0.0.1:8000/user/register"
这样相当于注册一个用户，用户名为tyf密码为123456

- 返回： 

成功：

状态码：200
返回数据：{'status': 'success'}

失败：

状态码：200
返回数据：{'status': 'error'} 

## 登录 

- 路径：/user/login

- 方法：get

- 参数：

username：通过路径传输
password：通过路径传输

- curl例子：

>curl -X GET "http://127.0.0.1:8000/user/login?username=default&password=default"
>这样相当于登录，用户名和密码都是default

- 返回： 

成功：

状态码：200
返回数据：{'status': 'success', 'jwt': \<jwt\>, 'image': <头像url>}

用户不存在：

状态码：200
返回数据：{'status': 'user not found'}

密码错误:

状态码：200
返回数据：{'status': 'invalid password'}

失败：

状态码：200
返回数据：{'status': 'error'} 

## 个人信息

- 路径：/user/homepage

- 方法：get

- 参数：

暂时不需要

- curl例子：

- 返回： 

成功：

{‘status’: True, 

'info': {
'name': char 用户名，
 'age': int 用户年龄，
 'sex': char 性别，
 'identity': char 用户身份（患者/医生），
 'description': char 个人简介，
 'image': url 用户头像，
‘follow_num’: int 关注的用户数量，
'follow_images': 一个列表，里面存储了最多3名关注用户的头像的url
}
}
失败：

{'status': False, 'info': 'errror'}

## 返回待修改个人信息

- 路径：/user/tobeupdated

- 方法：get

- 参数：无

- curl例子：

- 返回： 

成功：

状态码：200 {'status': 'success'} 
{‘status’: True, 

'info':{

'username': 用户名 
 'password': 密码，默认为******
'age': 年龄
 'sex': 性别
 'email': 邮箱地址
 'uid': 微信uid
 'description': 个人简介
'image': 用户头像url

}
}
失败：

{'status': False, 'info': 'error'}


## 修改个人信息

- 路径：/user/updatemyinfo

- 方法：post

- 参数：

'username': 用户名 
 'password': 密码
'age': 年龄
 'sex': 性别
 'email': 邮箱地址
 'uid': 微信uid
 'description': 个人简介
'ifChangeImage':1或0 若为1，则用户修改过头像
'image':用户新上传的头像图片，若用户未修改头像，则不包含此字段
以上信息无论是否发生变动都请传给后端

- curl例子：

- 返回： 

成功：

{‘status’: True, 'id': 当前用户的id}

失败：

{'status': False, 'id': 'update failed'}

## 发送与某人的聊天

- 路径：/chat/send

- 方法：post

- 参数：
chat_sender
chat_receiver
msg

- curl例子：

- 返回： 

成功：

状态码：200 {'status': 'success'} 
{‘status’: success, 

'message':{

'sender':
'receiver':
'message':
'created_time':

}
}
失败：

{'status': False, 'info': 'error'}

## 获取与某人的聊天记录

- 路径：/chat/list

- 方法：post

- 参数：
user
chater

- curl例子：

- 返回： 

成功：

状态码：200 {'status': 'success'} 
{‘status’: success, 

'messageList':[上方的message]
}
失败：

{'status': False, 'info': 'error'}