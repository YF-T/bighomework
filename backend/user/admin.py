from django.contrib import admin

# Register your models here.
from django.db.models.signals import post_migrate
from user import models
# 注册模型
admin.site.register(models.User)
admin.site.register(models.Prescription)
admin.site.register(models.Medicine)
admin.site.register(models.MedicationRecord)
admin.site.register(models.Inspection)
admin.site.register(models.Indicator)
admin.site.register(models.Diary)
admin.site.register(models.DiaryImage)
# 初始化数据
def init_db(sender, **kwargs):
    # 每个模型都会发这样一个信号，判断准是不是本模型发的
    if sender.name == 'user':
        if not models.User.objects.exists():
            models.InitDatabase() 
 
post_migrate.connect(init_db)