from datetime import datetime, timezone, timedelta
import jwt
from user.models import User
from django.http import JsonResponse

secret = 'juheli_antituberculosis'
expire_minutes = 2000000

def generate_jwt(payload):
    """
    :param payload: dict 载荷
    :return: 生成jwt
    """
    expiry = datetime.now(tz=timezone.utc) + timedelta(minutes=expire_minutes)
    payload['exp'] = expiry
    token = jwt.encode(payload, secret, algorithm='HS256')
    return token
    
def verify_jwt(token):
    """
    校验jwt
    :param token: jwt
    :return: dict: payload
    """
    try:
        payload = jwt.decode(token, secret, algorithm=['HS256'])
    except (jwt.PyJWTError, jwt.ExpiredSignatureError):
        payload = None
    return payload

def check_login(request):
    '''
    验证是否登录
    输入request
    输出user
    '''
    try:
        token = request.META.get("HTTP_AUTHORIZATION")
        payload = verify_jwt(token)
        user = User.objects.get(id = payload['id'])
        return user, True
    except:
        return "jwt error", False
    
def login_required(func):
    """
    用户必须登录装饰器
    使用方法：放在 method_decorators 中
    """
    def wrapper(request):
        # 检查jwt是否符合要求
        user, flag = check_login(request)
        if not flag:
            # 若不符合要求则返回
            response = JsonResponse({'status':'jwt error'})
            response.status_code = 200
            return response
        else:
            # 若符合要求，则将user绑在传入参数request中调用函数
            # 这里的user类型为user.models.User的那个类
            request.user = user
            return func(request)
    wrapper.__name__ = "wrapper" + func.__name__
    return wrapper
    