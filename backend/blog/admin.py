from django.contrib import admin

# Register your models here.
from django.db.models.signals import post_migrate
from blog import models
# 注册模型
admin.site.register(models.Blog)
admin.site.register(models.Comment)
admin.site.register(models.BlogImage)
admin.site.register(models.UserMessage)
# 初始化数据
def init_db(sender, **kwargs):
    # 每个模型都会发这样一个信号，判断准是不是本模型发的
    if sender.name == 'blog':
        if not models.Blog.objects.exists():
            models.InitBlogDatabase() 
    if sender.name == 'comment':
        if not models.Comment.objects.exists():
            models.InitCommentDatabase()
 
post_migrate.connect(init_db)