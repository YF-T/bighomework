from apscheduler.schedulers.background import BackgroundScheduler
from django.http import HttpResponse, JsonResponse
from django_apscheduler.jobstores import DjangoJobStore, register_events
import datetime

from user import models
from user.remind.emailreminder import email_sendmessage
from user.remind.wechatreminder import wx_sendmessage

GetMedicationRecordById = None


scheduler = BackgroundScheduler()
scheduler.add_jobstore(DjangoJobStore(), 'default')
register_events(scheduler)
scheduler.start()

def send_address_required(func):
    def wrapper(request):
        user = request.user
        if user.email or user.wx_uid:
            return func(request)
        else:
            response = JsonResponse({'status':'address error'})
            response.status_code = 200
            return response
    wrapper.__name__ = func.__name__
    return wrapper
    
def build_message_and_title(medication_record):
    message = '尊敬的用户：\n现在是%s，记得按时用药哦\n应服用药物：%s\n用药小贴士：%s' % (medication_record.time, medication_record.medicine.name, medication_record.requirements)
    title = '您有一个新的用药提醒'
    return message, title
    
def run_remind(id):
    print('send remind')
    medication_record, flag = models.GetMedicationRecordById(id)
    if not flag:
        return False
    created_time = datetime.datetime.strptime(medication_record.created_time, '%Y-%m-%d %H:%M:%S')
    days = (datetime.datetime.now() - created_time).days
    if days % medication_record.frequency != 0:
        return True
    message, title = build_message_and_title(medication_record)
    flag1 = email_sendmessage(medication_record.user, message, title)
    flag2 = wx_sendmessage(medication_record.user, message, title)
    return flag1 or flag2
    
def add_remind(medication_record):
    start_time = medication_record.time.split(':')
    hour = int(start_time[0])
    minute = int(start_time[1])
    scheduler.add_job(run_remind, 'cron', hour=hour, minute=minute, args=[medication_record.id], id=str(medication_record.id))
    return True
    
def update_remind(medication_record):
    start_time = medication_record.time.split(':')
    hour = int(start_time[0])
    minute = int(start_time[1])
    scheduler.reschedule_job(str(medication_record.id), trigger='cron', hour=hour, minute=minute)
    return True
    
def delete_remind(medication_record):
    scheduler.remove_job(str(medication_record.id))
    return True
    
