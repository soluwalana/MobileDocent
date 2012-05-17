#!/usr/local/bin/python2.7
import urllib2, json, time, os, re

from poster.encode import multipart_encode
from poster.streaminghttp import register_openers

os.system('./reset_db.sh')

register_openers()

SERVER_ADDR = 'http://samo.stanford.edu:8787/'

def send_files(link = '', file_map = {}, cookie = None):
    datagen, headers = multipart_encode(file_map)
    if cookie:
        headers['Cookie'] = cookie
    url = SERVER_ADDR+link
    req = urllib2.Request(url, datagen, headers)
    print urllib2.urlopen(req).read()
    

def send_request(link = '', data = None, cookie = None):
    
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
        res_data = json.loads(res_data)
        return (res_data, cookie)
    except Exception as err:
        print err
        return None


def err_test (link, msg=None, data=None, cookie=None):
    res, cookie = send_request(link, data, cookie)
    assert 'error' in res
    if msg:
        assert res['error'] == msg
    return cookie

def success_test (link, msg=None, data=None, cookie=None):
    res, cookie = send_request(link, data, cookie)
    assert 'success' in res
    if msg:
        assert res['success'] == msg
    return cookie
    
def test_user_create():
    #success test
    success_test('user', 'User Created Successfully',
                 {'userName' : 'samo',
                  'deviceId' : 'X28934',
                  'pass' : 'samo',
                  'passConf' : 'samo',
                  'about' : 'I like CS',
                  'email' : 'soluwalana@gmail.com'}
                 )
    
    #success multi user one phone
    success_test('user', 'User Created Successfully',
                 {'userName' : 'samo1',
                  'deviceId' : 'X28934',
                  'pass' : 'samo',
                  'passConf' : 'samo',
                  'about' : 'I like CS'}
                 )
    
    #duplicate user test
    err_test('user', None,
             {'userName' : 'samo',
              'deviceId' : 'X28934',
              'pass' : 'samo',
              'passConf' : 'samo',
              'about' : 'I like CS'})
    
    #missing param test
    err_test('user', 'Request missing basic parameters',
             {'userName' : 'samo',
              'pass' : 'samo',
              'passConf' : 'samo',
              'about' : 'I like CS'})
    
    #missing param test
    err_test('user', 'Request missing basic parameters',
             {'userName' : 'samo',
              'deviceId' : 'X28934',
              'pass' : 'samo',
              'about' : 'I like CS'})
        
    #missing param test
    err_test('user', 'Request missing basic parameters',
             {'deviceId' : 'X28934',
              'pass' : 'samo',
              'passConf' : 'samo',
              'about' : 'I like CS'})
    
        
    #missing param test
    err_test('user', 'Request missing basic parameters',
             {'userName' : 'samo',
              'deviceId' : 'X28934',
              'passConf' : 'samo',
              'about' : 'I like CS'})

    #pass mismatch test
    err_test('user', "Passwords don't match",
             {'userName' : 'samo',
              'deviceId' : 'X28934',
              'pass' : 'samo1',
              'passConf' : 'samo',
              'about' : 'I like CS'})
    
    print 'Successful user_create_test'

def test_user_auth():
    data = {'city' : 'Stanford',
            'region' : 'CA',
            'country' : 'USA'}
    
    err_test('echo', 'Session is not authenticated', data)
    
    #Ensure the Rest API isn't accessible without authenticating
    err_test('test', 'Session is not authenticated')
    err_test('user/1', 'Session is not authenticated')
    err_test('tour/1', 'Session is not authenticated')
    err_test('node/1', 'Session is not authenticated')
    err_test('ipLocation', 'Session is not authenticated')
    err_test('location', 'Session is not authenticated',
             {'city' : 'Stanford',
              'region' : 'CA',
              'country' : 'USA'})

    err_test('echo', 'Session is not authenticated',
             {'city' : 'Stanford',
              'region' : 'CA',
              'country' : 'USA'})
        
    err_test('tour/1', 'Session is not authenticated',
             {'tourName' : 'Stanford',
              'description' : 'Tour of Memorable Stanford Locations',
              'locId' : 11382,
              'walkingDistance' : '1.5'
              })

    err_test('login', 'Authentication failure',
             {'userName' : 'samo',
              'deviceId' : 'X28934',
              'pass' : 'wrongPass'
              })

    err_test('login', 'User lookup failed for authentication',
             {'userName' : 'bob',
              'deviceId' : 'X28934',
              'pass' : 'wrongPass'
              })

    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'deviceId' : 'X28934',
                           'pass' : 'samo'
                           })

    
    success_test('echo', data, data, cookie)
    
    success_test('logout', 'Logged Out', None, cookie)

    err_test('echo', 'Session is not authenticated', data, cookie)
        
    print 'Successful user_auth test complete'

def compare_users (res, data):
    for key in data.keys():
        assert key in res
        assert res[key] == data[key]
    
def test_user_get():
    user = {'userName' : 'samo',
             'deviceId' : 'X28934',
             'about' : 'I like CS',
             'email' : 'soluwalana@gmail.com'}
    user1 = {'userName' : 'samo1',
             'deviceId' : 'X28934',
             'about' : 'I like CS',
             'email' : None}
    
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'deviceId' : 'X28934',
                           'pass' : 'samo'
                           })
    
    res = send_request('user?userId=1', None, cookie)[0]
    compare_users(res, user)
    
    res = send_request('user?userId=2', None, cookie)[0]
    compare_users(res, user1)

    res = send_request('user?userName=samo', None, cookie)[0]
    compare_users(res, user)

    res = send_request('user?userName=samo1', None, cookie)[0]
    compare_users(res, user1)

    res = send_request('user?deviceId=X28934', None, cookie)[0]
    assert len(res) == 2

    res = send_request('user?userName=X28934', None, cookie)[0]
    assert len(res) == 0
    
    print 'Successful test user get'
            
def test_create_tour():
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'deviceId' : 'X28934',
                           'pass' : 'samo'
                           })

    send_request('tour', {'tourName' : 'Stanford',
                          'description' : 'Tour of Memorable Stanford Locations',
                          'locId' : 11382,
                          'walkingDistance' : '1.5'
                          }, cookie)

def test_file_upload():
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'deviceId' : 'X28934',
                           'pass' : 'samo'
                           })


    node_data = {
        'latitude': 123.43,
        'longitude': 42.42,
        'brief' : {
            'title' : 'Test Node',
            'description' : 'This is a test node that has three repetive pics',
            'thumb_id' : 'thumb1' },
        'content' : [
            [{'xpos' : 0, 'ypos' : 0,
              'width' : 20, 'height': 20,
              'content_type' : 'image/jpg',
              'content_id' : 'image1'},
             {'xpos' : 20, 'ypos' : 0,
              'width' : 40, 'height': 20,
              'content_type' : 'text/plain',
              'content' : 'Hello World this is plain'
              },
             {'xpos' : 60, 'ypos': 0,
              'width': 20, 'height': 20,
              'content_type' : 'image/jpg',
              'content_id' : 'image2'
              }
             ],
            [{'xpos' : 0, 'ypos' : 0,
              'width': 60, 'height' : 60,
              'content_type' : 'image/jpg',
              'content_id' : 'image3'
              },
             {'xpos' : 0, 'ypos' : 60,
              'width':60, 'height':60,
              'content_type' : 'text/html',
              'content' : '<h1>Hello</h1><p>Html World</p><p>For pretty formating perhaps</p>'
              }
             ]
            ]
        }
    data = {'image1' : open('IMG_0137.JPG'),
            'image2' : open('IMG_0137.JPG'),
            'image3' : open('IMG_0137.JPG'),
            'thumb1' : open('IMG_0137.JPG'),
            'nodeData' : node_data
            }
    send_files('uploadTest', data, cookie)
    
test_user_create()
#test_user_auth()
#test_user_get()
test_file_upload()

"""
res, cookie = send_request('echo', {'data' : 'same'})
res, cookie = send_request('user/1')
res, cookie = send_request('tour/1')
res, cookie = send_request('node/1')

res, cookie = send_request('ipLocation')

res, cookie = send_request('location', {'city' : 'Stanford',
                                'region' : 'CA',
                                'country' : 'USA'})

"""



