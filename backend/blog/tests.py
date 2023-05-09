from django.test import TestCase
from django.test import Client
from django.http import QueryDict

# Create your tests here.
class BlogTestCase(TestCase):
    def setUp(self):
        self.app = Client()

    def test_blog(self):
        # 登录
        response = self.app.get('/user/login', {'username': 'default', 'password': 'default'})
        data = response.json()
        self.assertEqual(data['status'], 'success')
        jwt = data['jwt']
        # 上传图片
        with open('abc.jpg','rb') as file:
            response = self.app.post('/blog/image/upload', {'image': file}, HTTP_Authorization=jwt)
        data = response.json()
        self.assertEqual(data['status'], 'success')
        # 新建博客
        blog = {'title': ['test'], 
                'content': ['content'], 
                'tag': ['']}
        blog = 'title=test&content=content&tag=&id_image=1'
        response = self.app.put('/blog/create', blog, HTTP_Authorization=jwt)
        data = response.json()
        self.assertEqual(data['status'], 'success')
        # 搜索博客
        search = {'key': '', 
                  'tag': 'all'}
        response = self.app.get('/blog/search', search)
        data = response.json()
        self.assertEqual(data['status'], 'success')
        self.assertEqual(len(data['blogs_thumb']), 1)
        blog = data['blogs_thumb'][0]
        # 打开博客
        response = self.app.get('/blog/blog', {'id': 1}, HTTP_Authorization=jwt)
        data = response.json()
        self.assertEqual(data['status'], 'success')
        # 写评论
        response = self.app.put('/blog/comment/create', 'content=good!&id=1', HTTP_Authorization=jwt)
        data = response.json()
        self.assertEqual(data['status'], 'success')
        comment_id = data['id']
        # 点赞评论
        response = self.app.post('/blog/comment/approve', {'id': comment_id}, HTTP_Authorization=jwt)
        data = response.json()
        self.assertEqual(data['status'], 'success')
        self.assertEqual(data['bool_support'], True)