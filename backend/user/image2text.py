import requests
import base64
import re
from collections import deque
import json
import io
import pandas as pd
from tencentcloud.common import credential
from tencentcloud.common.profile.client_profile import ClientProfile
from tencentcloud.common.profile.http_profile import HttpProfile
from tencentcloud.common.exception.tencent_cloud_sdk_exception import TencentCloudSDKException
from tencentcloud.ocr.v20181119 import ocr_client, models

chinese2int = {'一':1, '每':1, '二':2, '两':2, '三':3, '四':4, '五':5, '六':6, '壹':1, '贰':2, '叁':3, '肆':4, '伍':5, '陆':6}

def ocr(img_file) -> list:
    '''
    根据图片路径，将图片转为文字，返回识别到的字符串列表

    '''
    # 请求头
    headers = {
        'Host': 'cloud.baidu.com',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36 Edg/89.0.774.76',
        'Accept': '*/*',
        'Origin': 'https://cloud.baidu.com',
        'Sec-Fetch-Site': 'same-origin',
        'Sec-Fetch-Mode': 'cors',
        'Sec-Fetch-Dest': 'empty',
        'Referer': 'https://cloud.baidu.com/product/ocr/general',
        'Accept-Language': 'zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6',
    }
    # 对图片使用 base64 编码
    img = base64.b64encode(img_file.read())
    data = {
        'image': 'data:image/jpeg;base64,'+str(img)[2:-1],
        'image_url': '',
        'type': 'https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic',
        'detect_direction': 'false'
    }
    # 开始调用 ocr 的 api
    response = requests.post(
        'https://cloud.baidu.com/aidemo', headers=headers, data=data)

    # 设置一个空的列表，后面用来存储识别到的字符串
    ocr_text = []
    result = response.json()['data']
    if isinstance(result, str):
        print(result)
    if not 'words_result' in result or not result.get('words_result'):
        print('百度的返回数据:', result)
        return []

    # 将识别的字符串添加到列表里面
    for r in result['words_result']:
        text = r['words'].strip()
        ocr_text.append(text)
    # 返回字符串列表
    return ocr_text

# 腾讯的文字识别
def ocrtencent(img_file):
    # 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
    # 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
    cred = credential.Credential("AKIDZx72h5D6nEOw23oMgkaBYgZCdtwlLp6C", "yjHadUBgx1udCnCNJ2XoqdBg4JMDoBWk")
    # 实例化一个http选项，可选的，没有特殊需求可以跳过
    httpProfile = HttpProfile()
    httpProfile.endpoint = "ocr.tencentcloudapi.com"

    # 实例化一个client选项，可选的，没有特殊需求可以跳过
    clientProfile = ClientProfile()
    clientProfile.httpProfile = httpProfile
    # 实例化要请求产品的client对象,clientProfile是可选的
    client = ocr_client.OcrClient(cred, "ap-beijing", clientProfile)

    # 实例化一个请求对象,每个接口都会对应一个request对象
    req = models.RecognizeTableOCRRequest()
    # 转base64
    img = base64.b64encode(img_file.read())
    params = {
        'ImageBase64': 'data:image/jpeg;base64,'+str(img)[2:-1],
    }
    req.from_json_string(json.dumps(params))

    # 返回的resp是一个RecognizeTableOCRResponse的实例，与请求对象对应
    resp = client.GeneralBasicOCR(req)
    # 输出json格式的字符串回包
    words = list(map(lambda x:x.DetectedText, resp.TextDetections))
    return words

def ocrtable(img_file):
    # 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
    # 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
    cred = credential.Credential("AKIDZx72h5D6nEOw23oMgkaBYgZCdtwlLp6C", "yjHadUBgx1udCnCNJ2XoqdBg4JMDoBWk")
    # 实例化一个http选项，可选的，没有特殊需求可以跳过
    httpProfile = HttpProfile()
    httpProfile.endpoint = "ocr.tencentcloudapi.com"

    # 实例化一个client选项，可选的，没有特殊需求可以跳过
    clientProfile = ClientProfile()
    clientProfile.httpProfile = httpProfile
    # 实例化要请求产品的client对象,clientProfile是可选的
    client = ocr_client.OcrClient(cred, "ap-beijing", clientProfile)

    # 实例化一个请求对象,每个接口都会对应一个request对象
    req = models.RecognizeTableOCRRequest()
    # 转base64
    img = base64.b64encode(img_file.read())
    params = {
        'ImageBase64': 'data:image/jpeg;base64,'+str(img)[2:-1],
    }
    req.from_json_string(json.dumps(params))

    # 返回的resp是一个RecognizeTableOCRResponse的实例，与请求对象对应
    resp = client.RecognizeTableOCR(req)
    # 输出json格式的字符串回包
    data = resp.Data
    res= io.BytesIO(base64.b64decode(data))
    return pd.read_excel(res)

def ocrprescription(img_file):
    try:
        text = ocrtencent(img_file)
    except Exception as e:
        print('百度出问题乐')
        print(e)
        return [], False
    begin = [x for x in text if ('R' in x and len(x) < 4)]
    end = [x for x in text if ('以下空白' in x)]
    if begin:
        startindex = text.index(begin[0])
        text = text[startindex+1:]
    else:
        return [], False
    if end:
        endindex = text.index(end[0])
        text = text[:endindex]
    # 制造一个状态机，状态分别为'name'，'specification'，'amount'，'medication_records'
    state = 'name'
    # 使用迭代器不断迭代直到出错
    text = iter(text)
    medicines = []
    d = deque()
    d.extendleft(text)
    try:
        while state != 'name' or d:
            if state == 'name':
                medicines.append({'name':'', 
                                  'specification':'', 
                                  'amount':'', 
                                  'medication_records':[]})
                name = d.pop()
                if re.search(r'^\d\.?$', name):
                    name += d.pop()
                name = re.sub(r'^\d\.?', '', name)
                medicines[-1][state] = name
                state = 'specification'
            elif state == 'specification':
                specification = d.pop()
                if re.search(r'×', specification):
                    d.append(re.search(r'×.*$', specification)[0])
                    specification = re.sub(r'×.*$', '', specification)
                medicines[-1][state] = specification
                state = 'amount'
            elif state == 'amount':
                amount = d.pop()
                if amount[0] in ['×', '*']:
                    amount = amount[1:]
                medicines[-1][state] = amount
                state = 'medication_records'
            elif state == 'medication_records':
                usage = d.pop()
                assert ('用法' in usage)
                while not re.search(r'\w日\w次', usage):
                    usage += ('，' + d.pop())
                result = re.search(r'(\w)日(\w)次', usage)
                x = chinese2int[result[1]]
                y = chinese2int[result[2]]
                requirements = re.sub(r'(，?\w日\w次)|(用法:?：?)', '', usage)
                for i in range(y):
                    medicines[-1][state].append({'frequency': x, 
                                                     'time': '', 
                                                     'requirements': requirements})
                if y == 1:
                    medicines[-1][state][0]['time'] = '10:00'
                elif y == 2:
                    medicines[-1][state][0]['time'] = '9:00'
                    medicines[-1][state][1]['time'] = '18:00'
                elif y == 3:
                    medicines[-1][state][0]['time'] = '9:00'
                    medicines[-1][state][1]['time'] = '13:00'
                    medicines[-1][state][2]['time'] = '18:00'
                state = 'name'
    except:
        return medicines, False
    return medicines, True
    
def ocrinspection(img_file):
    method = False
    order = None
    try:
        text = ocrtencent(img_file)
    except Exception as e:
        print('百度出问题乐')
        print(e)
        return [], False
    try:
        print(text, '456')
        alltext = ''.join(text)
        assert ('项目' in alltext)
        assert ('结果' in alltext)
        assert ('参考' in alltext)
        reference = '参考区间' if '参考区间' in alltext else None
        reference = '参考范围' if '参考范围' in alltext else reference
        reference = '参考值' if '参考值' in alltext else reference
        assert reference
        if '检验方法' in alltext:
            method = True
        if '单位' in alltext:
            if alltext.find('单位') < alltext.find(reference):
                order = 'perunit first'
            else:
                order = 'theoreticalvalue first'
        else:
            order = 'no perunit'
    except:
        return [], False
    if method:
        end = '检验方法'
    elif order == 'theoreticalvalue first':
        end = '单位'
    else:
        end = reference
    begin = [x for x in text if (end in x)]
    while text.count(begin[-1]):
        startindex = text.index(begin[-1])
        text = text[startindex+1:]
    # 制造一个状态机，状态分别为'name'，'value'，'perunit'，'theoreticalvalue'
    state = 'name'
    # 使用迭代器不断迭代直到出错
    print(text)
    text = iter(text)
    indicators = []
    d = deque()
    d.extendleft(text)
    try:
        while state != 'name' or d:
            if state == 'name':
                indicators.append({'name':'', 
                                   'value':'', 
                                   'perunit':'',
                                   'theoreticalvalue':''})
                name = d.pop()
                if re.search(r'^\d+$', name):
                    name = d.pop()
                indicators[-1][state] = name
                state = 'value'
            elif state == 'value':
                value = d.pop()
                while not re.search(r'(^[\d\.]+$)|(阴性)|(阳性)', value):
                    value = d.pop()
                indicators[-1][state] = value
                state = 'perunit' if order == 'perunit first' else 'theoreticalvalue'
            elif state == 'perunit':
                perunit = d.pop()
                while not re.search(r'%|([a-zA-Z]|个)*1?/[a-zA-Z1]*', perunit):
                    perunit = d.pop()
                indicators[-1][state] = perunit
                state = 'theoreticalvalue' if order == 'perunit first' else 'name'
                if state == 'name' and method:
                    d.pop()
            elif state == 'theoreticalvalue':
                theoreticalvalue = d.pop()
                unit = re.search(r'%|([a-zA-Z]|个)*1?/[a-zA-Z1]*', theoreticalvalue)
                if unit:
                    if indicators[-1]['perunit'] == '':
                        indicators[-1]['perunit'] = unit[0]
                    theoreticalvalue = re.sub(r'%|([a-zA-Z]|个)*1?/[a-zA-Z1]*', '', theoreticalvalue)
                while not re.search(r'(^[\d\.]*.[\d\.]+$)|(阴性)|(阳性)', theoreticalvalue):
                    theoreticalvalue = d.pop()
                indicators[-1][state] = theoreticalvalue
                state = 'perunit' if order == 'theoreticalvalue first' else 'name'
                if state == 'name' and method:
                    d.pop()
    except:
        return indicators, False
    return indicators, True

def ocrtableinspection(img_file):
    try:
        table = ocrtable(img_file).fillna('')
        keys = list(table)
        keys_new = []
        for key in keys:
            if ('项' in key or '目' in key) and not '简' in key:
                key = '项目.1' if '.1' in key else '项目'
            elif '结果' in key:
                key = '结果.1' if '.1' in key else '结果'
            elif '参考' in key:
                key = '参考范围.1' if '.1' in key else '参考范围'
            keys_new.append(key)
        table.columns = keys_new
        #table.rename(columns=rename_sheet)
        keys = list(table)
        if not '单位' in keys:
            table.insert(loc=len(keys), column='单位', value='')
        focus_table = table[['项目','结果','单位','参考范围']]
        focus_table.columns = ['name', 'value', 'perunit', 'theoreticalvalue']
        result = focus_table.to_dict('records')
        if '项目.1' in keys:
            if not '单位.1' in keys:
                table.insert(loc=len(keys), column='单位.1', value='')
            focus_table = table[['项目.1','结果.1','单位.1','参考范围.1']]
            focus_table.columns = ['name', 'value', 'perunit', 'theoreticalvalue']
            result += focus_table.to_dict('records')
        return result, True
    except:
        return [], False
        
if __name__ == '__main__':
    with open(r'D:\大学\计算机\软件工程\大作业草稿\image2text\testinspection.jpg','rb') as f:
        #res = ocrtencent(f)
        #print(res, '123')
        res = ocrinspection(f)
        print(res)
    
