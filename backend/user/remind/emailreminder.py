from email import encoders
from email.header import Header
from email.mime.text import MIMEText
from email.utils import parseaddr, formataddr

import smtplib
    
def format_addr(s):
    name, addr = parseaddr(s)
    return formataddr((Header(name, 'utf-8').encode(), addr))

from_addr = 'juheli_developers@126.com'
password = 'BOQMCRENXVUDBFYI'
smtp_server = 'smtp.126.com'
from_name = '聚核力运营管理团队'

def email_sendmessage(user, message, title):
    try:
        to_addr = user.email
        to_name = user.name
        msg = MIMEText(message, 'plain', 'utf-8')
        msg['From'] = format_addr('%s <%s>' % (from_name ,from_addr))
        msg['To'] = format_addr('%s <%s>' % (to_name, to_addr))
        msg['Subject'] = Header(title, 'utf-8').encode()
        server = smtplib.SMTP(smtp_server, 25)
        server.set_debuglevel(1)
        server.login(from_addr, password)
        server.sendmail(from_addr, [to_addr], msg.as_string())
        server.quit()
        return True
    except:
        return False

if __name__ == '__main__':
    class User(object):
        email = 'tanyf20@mails.tsinghua.edu.cn'
        name = '谭弈凡'
        wx_uid = 'UID_NhUxwJrrkRmHC1K95kuIXWARlKQG'
    user = User()
    
    ans = email_sendmessage(user, '用药提醒:\n12:00了，请按时服药', '您的用药提醒')
    print(ans)