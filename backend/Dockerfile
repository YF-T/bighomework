FROM python:3.8

MAINTAINER Aaron

#设置环境变量
ENV PYTHONUNBUFFERED 1

#在容器内/var/www/html/下创建mysite1文件夹
RUN mkdir /code

#设置容器内工作目录
WORKDIR /code

#将当前目录文件加入到容器工作目录中(.表示当前宿主机目录)
ADD . /code

#利用pip安装依赖
RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple/ --trusted-host pypi.tuna.tsinghua.edu.cn --default-timeout=60 --no-cache-dir -r requirements.txt

CMD ["/bin/sh", "run.sh"]

EXPOSE 8000