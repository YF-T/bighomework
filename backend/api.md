
四种方法的传参方式的前后端示例：

https://blog.csdn.net/weixin_42289273/article/details/113180723

## 模板

- 路径：

- 方法：

- 参数：

>



- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 

jwt过期/失效：
>>

{'status': 'jwt error'}
失败：

>>



## 注册

- 路径：/user/register

- 方法：post

- 参数：

>

username：通过formdata传输
password：通过formdata传输

- curl例子：

>

curl -X POST -d "username=admin122121&password=admin" "http://127.0.0.1:8000/user/register"
这样相当于注册一个用户，用户名为tyf密码为123456

- 返回： 

>

成功：
>>
状态码：200 {'status': 'success'} 
状态码：200
返回数据：{'status': 'success'}
失败：
>>

状态码：200
返回数据：{'status': 'error'} 

## 登录 

- 路径：/user/login

- 方法：get

- 参数：

>

username：通过路径传输
password：通过路径传输

- curl例子：

>

curl -X GET "http://127.0.0.1:8000/user/login?username=default&password=default"
这样相当于登录，用户名和密码都是default

- 返回： 

>

成功：
>>
状态码：200 {'status': 'success'} 
状态码：200
返回数据：{'status': 'success', 'jwt': <jwt>, 'image': <头像url>}
用户不存在：
>>

状态码：200
返回数据：{'status': 'user not found'}
密码错误
>>

状态码：200
返回数据：{'status': 'invalid password'}
失败：
>>

状态码：200
返回数据：{'status': 'error'} 

## 新建动态

- 路径：/dongtai/create

- 方法：put

- 参数：

>

jwt：通过http请求头Authorization字段传输
title：通过formdata传输
content：通过formdata传输，markdown字符串
tag：通过formdata传输
url_images: 动态图片的id列表

- curl例子：

>



- 返回： 

>

成功：
>>
状态码：200 {'status': 'success'} 
{'status': 'success', 'id': <文章id>}
jwt过期/失效：
>>

{'status': 'jwt error'}
失败：
>>

{'status': 'error'} 

## 搜索动态（获取所有动态）

- 路径：/dongtai/search

- 方法：get

- 参数：

>

key：通过路径传输，字符串（搜索词，若获取所有文章则为空）
type：'all'或'mydongtai'（自己写的动态）或'favorite'（点赞动态）
tag：通过路径传输，字符串（表示搜索的类别，可选项如下）
>>

all：全部
其他待定

- curl例子：

>

curl -X GET "http://127.0.0.1:8000/dongtai/search?key=&tag=all"

- 返回： 

>

状态码都是200
成功：
>>状态码：200 {'status': 'success'} 
>>{True, 
>><四个Dataframe，里面有若干个dic形式的动态>
>> 'dongtais_time': [ <时间排序>
>> {'id': <动态id，由数据库自动生成>
>>  'title': <文章标题>, 
>>  'author_id': <作者id>, 
>>  'author_name': <作者名字>，
>>  'author_image'：<作者头像url>, 
>>  'tag'：<文章标签>，
>>  'created_time'：<创建时间>,
>>  'num_thumbs'：<点赞数量>, 
>>  'browse'：<阅读数>, 
>>  'num_collects'：<评论数>，
>>
>>'url_images':<图片的url>
>>
>>'num_comments'：<评论数>，
>>（如果需要其它参数请参考dongtai类的变量名）}
>> ]
>>dongtais_*thumb  <点赞数排序>*
>>dongtais_browse  <浏览量排序>
>>dongtais_comment  <评论数排序>
>>}
>>失败：

{False, 'status': 'error'} 

## 打开动态

- 路径：/dongtai/dongtai

- 方法：get

- 参数：

>

jwt(可选)：通过http请求头Authorization字段传输
id：通过路径传输

- curl例子：

>

curl -X GET "http://127.0.0.1:8000/dongtai/dongtai?id=1"

- 返回： 

>

状态码都是200
成功：
>>状态码：200 {'status': 'success'} 
{'status': 'success', 
 'dongtai': {
  'id': <动态id，由数据库自动生成>
  'title': <文章标题>, 
  'author': <作者>, 
  'author_image'：<作者头像url>, 
  'tag'：<文章标签>
  'created_time'：<创建时间>,
  'content': <内容>
  'num_thumbing_users'：<点赞数量>, 
  'browse'：<阅读数>, 
  ‘num_comment’：<评论数>，
  'bool_thumb': <当前用户是否点赞>
    'bool_follow': <当前用户是否关注作者>}, 
  'comments': [ 一个列表，放着所有形式如下的评论
  {
'id': <评论id>,
'author': <作者>, 
'author_image'：<作者头像url>, 
'content': <内容>
'created_time'：<创建时间>,
'num_thumbing_users'：<点赞数量>, 
'bool_thumb': <当前用户是否点赞>
  }
  ]
 }
失败：

{'status': 'fail'} 

## 点赞/撤赞动态

- 路径：/dongtai/support

- 方法：post

- 参数：

>

jwt：通过http请求头Authorization字段传输
id：通过路径传输

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 
{
'status': 'success', 
'bool_support': <是否点赞>, 
'num_thumbing_users'：<点赞数量>
}
jwt过期/失效：
>>

{'status': 'jwt error'}
失败：
>>

{'status': 'error'}

## 写评论

- 路径：/dongtai/comment/create

- 方法：put

- 参数：

>

jwt：通过http请求头Authorization字段传输
content：通过formdata传输，字符串
id：通过formdata传输，字符串，动态的id

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 
{'status': 'success', 
‘num_comment’: <更新后的评论数>, 
'comment': {
 'id': <评论id>,
 'author': <作者>,
 'author_image'：<作者头像url>,
 'content': <内容>
 'created_time'：<创建时间>,
 'num_thumbing_users'：<点赞数量>,
 'bool_thumb': <当前用户是否点赞>
 } 
}
jwt过期/失效：
>>

{'status': 'jwt error'}
失败：
>>

{'status': 'error'}

## 点赞/撤赞评论

- 路径：/dongtai/comment/approve

- 方法：post

- 参数：

>

jwt：通过http请求头Authorization字段传输
id：评论id

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 
{
'status': 'success', 
'bool_support': <是否点赞>, 
'num_thumbing_users'：<点赞数量>
}
jwt过期/无效：
>>

{'status': 'jwt error'}
失败：
>>

{'status': 'error'}

## 上传动态图片

- 路径：/dongtai/image/upload

- 方法：post

- 参数：

>

jwt：通过http请求头Authorization字段传输
image：通过请求体传输，图片文件

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 
{'status': 'success', 'url': <图片的url，不包括base的ip>, 'id': <图片的id>}
jwt过期/失效：
>>

{'status': 'jwt error'}
失败：
>>

{'status': 'error'}

## 查看关注列表

- 路径：/user/myfollows

- 方法：get

- 参数：

>

无

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 
{'status': True, 'list': 一个列表（无关注用户时为空列表），列表内每一项为如下字典：
dict{'id': <关注用户id>,
'name': <关注用户的用户名>,
'identity': <关注用户的身份认证信息>,
'description': <关注用户的个人简介>,
'image': url<关注用户的头像>
}
}
失败：
>>

{'status': False, 'list': []}


## 关注/取关用户

- 路径：/user/follow

- 方法：post

- 参数：

>

id：被关注的用户的id

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 
{'status': 'success', 'bool_follow': <当前是否关注该用户的状态>}
失败：
>>

{'status': 'author not found'}


## 个人信息

- 路径：/user/homepage

- 方法：get

- 参数：

>

暂时不需要

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 
{‘status’: True, 'info': {
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
>>

{'status': False, 'info': 'errror'}


## 其他用户信息

- 路径：/user/foreigninfo

- 方法：post

- 参数：

>

待查看的用户id

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
状态码：200 {'status': 'success'} 
{‘status’: True, 'info': {
'name': char 用户名，
 'age': int 用户年龄，
 'sex': char 性别，
 'identity': char 用户身份（患者/医生），
 'description': char 个人简介，
 'image': url 用户头像
}
}
失败：
>>

{'status': False, 'info': 'errror'}


## 返回待修改个人信息

- 路径：/user/tobeupdated

- 方法：get

- 参数：无

- curl例子：

>



- 返回： 

>

状态码都是200
成功：
>>
>>状态码：200 {'status': 'success'} 
>>{‘status’: True, 'info':{
>>
>>>

'username': 用户名 
 'password': 密码，默认为******
'age': 年龄
 'sex': 性别
 'email': 邮箱地址
 'uid': 微信uid
 'description': 个人简介
'image': 用户头像url}
}
失败：
>>

{'status': False, 'info': 'error'}


## 修改个人信息

- 路径：/user/updatemyinfo

- 方法：post

- 参数：

>

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

>



- 返回： 

>

状态码都是200
成功：
>>状态码：200 {'status': 'success'} 
{‘status’: True, 'id': 当前用户的id}
失败：

{'status': False, 'id': 'update failed'}

## 搜索用户点赞的动态 

- 路径：/dongtai/user/favorites 

- 方法：get 

- 参数： 

>

jwt 

- curl例子： 

>

a 

- 返回： 

>

状态码都是200 
成功： 
>>

{'status': 'success' 
'dongtais': [一个字典的列表{ 
​      'id': <动态id，由数据库自动生成> 
​      'title': <文章标题>, 
​      'author_id': <作者id>, 
​      'author_name': <作者名字>， 
​      'author_image'：<作者头像url>, 
​      'tag'：<文章标签>， 
​      'created_time'：<创建时间>, 
​      'num_thumbs'：<点赞数量>, 
​      'browse'：<阅读数>, 
​      'num_comments'：<评论数>， 
​      ‘cover_image’：<动态封面图url> 
}]} 
jwt过期/失效： 

>>

{'status': 'jwt error'} 
失败： 
>>

{'status': 'error'} 

## 搜索用户创作的动态 

- 路径：/dongtai/user/dongtais 

- 方法：get 

- 参数： 

>

jwt 

- curl例子： 

>

a 

- 返回： 

>

状态码都是200 
成功： 
>>

{'status': 'success' 
'dongtais': [一个字典的列表{ 
​      'id': <动态id，由数据库自动生成> 
​      'title': <文章标题>, 
​      'author_id': <作者id>, 
​      'author_name': <作者名字>， 
​      'author_image'：<作者头像url>, 
​      'tag'：<文章标签>， 
​      'created_time'：<创建时间>, 
​      'num_thumbs'：<点赞数量>, 
​      'browse'：<阅读数>, 
​      'num_comments'：<评论数>， 
​      ‘cover_image’：<动态封面图url> 
}]} 
jwt过期/失效： 
>>

{'status': 'jwt error'} 
失败： 
>>

{'status': 'error'} 

## 查看通知消息 

- 路径：/dongtai/message 

- 方法：get 

- 参数： 

>

jwt 

- curl例子： 

>

a 

- 返回： 

>

状态码都是200 
成功： 

>>

{'status': 'success', 'messages':[一个字典的列表，按时间顺序倒序 
{ 

```null
'id':<消息id>,

'user':<发送用户姓名>,

'user_image':<发送用户头像url>,

'dongtai_title':<动态标题>,
'dongtailog_tag':<动态的tag>,
'dongtai_id':<动态id>

'new':<布尔值，表示是否为新消息，new=True则是新消息>,

'message':<消息体的text>,

'message_type':'Comment'（dongtai的消息）或'Blog'（新建动态的消息）

'created_time':<时间，'2022-12-20 15:07:00'>

```
} 
] 
} 
jwt过期/失效： 
>>

{'status': 'jwt error'} 
失败： 
>>

{'status': 'error'} 

## 获取用户关注列表

- 路径：user/getfollowings

- 方法：get

- 参数：

  > jwt

- 返回：

  > 成功：
  >
  > {
  >
  > 'status': True, 
  >
  > 'followings': [一个列表，元素为
  >
  > ​	{
  >
  > ​		'id': 数据库id，int
  >
  > ​		'name': 用户名，str
  >
  > ​		'description': 用户描述，str
  >
  > ​		'image_url': 头像url，str
  >
  > ​	}
  >
  > ]
  >
  > }