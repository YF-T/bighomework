from wxpusher import WxPusher

token = 'AT_wkFatPbChoFrl1rXAaSB1G4D1QzdfgeB'

def wx_createqrcode(user):
    res = WxPusher.create_qrcode(str(user.id), 1800, token)
    if res['success']:
        return res['data']['url'], True
    else:
        return '', False
    
def wx_sendmessage(user, message, title):
    if user.wx_uid == '':
        return False
    res = WxPusher.send_message(message, uids=[user.wx_uid], token=token)
    if res['success']:
        return True
    else:
        return False

if __name__ == '__main__':
    class User(object):
        id = 1
        email = 'tanyf20@mails.tsinghua.edu.cn'
        name = '谭弈凡'
        wx_uid = 'UID_NhUxwJrrkRmHC1K95kuIXWARlKQG'
    user = User()
        
    res = wx_createqrcode(user)
    print(res)
    
