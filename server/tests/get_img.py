#!/usr/local/bin/python2.7
import urllib2, json, time, os, re, sys

from poster.encode import multipart_encode
from poster.streaminghttp import register_openers

register_openers()

SERVER_ADDR = 'http://samo.stanford.edu:8787/'

def send_files(link = '', file_map = {}, cookie = None):
    datagen, headers = multipart_encode(file_map)
    if cookie:
        headers['Cookie'] = cookie
    url = SERVER_ADDR+link
    req = urllib2.Request(url, datagen, headers)
    try:
        res = json.loads(urllib2.urlopen(req).read())
        return res
    except Exception as err:
        print err
        return None
    

def send_request(link = '', data = None, cookie = None, raw = False):
    try:
        if data:
            data = json.dumps(data)
        url = SERVER_ADDR+link
        headers =  {"Origin" : "My House"}
        
        if data:
            headers['Content-Type'] = 'application/json'
                                
        if cookie:
            headers['Cookie'] = cookie
                
        req = urllib2.Request(url, data, headers)
        url_req = urllib2.urlopen(req)
        info = url_req.info()

        for header in info.headers:
            match = re.search('.*Set-Cookie: (connect.sid=.*?);', header, re.I|re.S)
            if match:
                cookie = match.group(1)
                 
        res_data =  url_req.read()
        if not raw:
            res_data = json.loads(res_data)
            return (res_data, cookie)
        else:
            return (res_data, cookie)
    except Exception as err:
        print err
        return None

def success_test (link, msg=None, data=None, cookie=None):
    res, cookie = send_request(link, data, cookie)
    print res
    assert 'success' in res
    if msg:
        assert res['success'] == msg
    return cookie

cookie = success_test('login', 'Successfully authenticated',
                      {'userName' : 'samo',
                       'pass' : 'samo'
                       })

content_id = sys.argv[1]
res = send_request('mongoFile?mongoFileId='+content_id, None, cookie, True)[0]
x = open('img.png', 'w')
x.write(res)
x.close()


