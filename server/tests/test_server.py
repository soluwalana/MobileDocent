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

def assert_result(res):
    assert 'success' in res
    assert 'result' in res
    assert 'nodeId' in res['result']

def assert_tour(res, name, nodes):
    assert 'success' in res
    assert 'tour' in res
    assert 'tourId' in res['tour']
    assert 'tourName' in res['tour']
    assert res['tour']['tourName'] == name
    assert 'nodes' in res['tour']
    assert len(res['tour']['nodes']) == nodes

def assert_normal_node(res):
    pass

def assert_brief_only_node(res):
    pass

def assert_pseudo_node(res):
    pass

def test_user_create():
    #success test
    success_test('user', 'User Created Successfully',
                 {'userName' : 'samo',
                  'pass' : 'samo',
                  'passConf' : 'samo',
                  'about' : 'I like CS',
                  'email' : 'soluwalana@gmail.com'}
                 )
    
    #success multi user one phone
    success_test('user', 'User Created Successfully',
                 {'userName' : 'samo1',
                  'pass' : 'samo',
                  'passConf' : 'samo',
                  'about' : 'I like CS'}
                 )
    
    #duplicate user test
    err_test('user', None,
             {'userName' : 'samo',
              'pass' : 'samo',
              'passConf' : 'samo',
              'about' : 'I like CS'})
    

    #missing param test
    err_test('user', 'Request missing basic parameters',
             {'userName' : 'samo',
              'pass' : 'samo',
              'about' : 'I like CS'})
        
    #missing param test
    err_test('user', 'Request missing basic parameters',
             {'pass' : 'samo',
              'passConf' : 'samo',
              'about' : 'I like CS'})
    
        
    #missing param test
    err_test('user', 'Request missing basic parameters',
             {'userName' : 'samo',
              'passConf' : 'samo',
              'about' : 'I like CS'})

    #pass mismatch test
    err_test('user', "Passwords don't match",
             {'userName' : 'samo',
              'pass' : 'samo1',
              'passConf' : 'samo',
              'about' : 'I like CS'})
    
    print 'Successful user_create_test'

def test_user_auth():
    data = {u'city' : u'Stanford',
            u'region' : u'CA',
            u'country' : u'USA'}
    
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
              'pass' : 'wrongPass'
              })

    err_test('login', 'User lookup failed for authentication',
             {'userName' : 'bob',
              'pass' : 'wrongPass'
              })

    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'pass' : 'samo'
                           })

    
    success_test('echo', data, data, cookie)
    
    success_test('logout', 'Logged Out', None, cookie)

    err_test('echo', 'Session is not authenticated', data, cookie)

    cookie = success_test('login', 'Successfully authenticated',
                          {'userId' : 1,
                           'pass' : 'samo'
                           })
    
    
    success_test('echo', data, data, cookie)

        
    print 'Successful user_auth test complete'

def compare_users (res, data):
    for key in data.keys():
        assert key in res
        assert res[key] == data[key]
    
def test_user_get():
    user = {'userName' : 'samo',
            'about' : 'I like CS',
             'email' : 'soluwalana@gmail.com'}
    user1 = {'userName' : 'samo1',
             'about' : 'I like CS',
             'email' : None}
    
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
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

    res = send_request('user?userName=e01ae16e97c13c77', None, cookie)[0]
    assert len(res) == 0
    
    print 'Successful test user get'
            
def test_create_tour():
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'pass' : 'samo'
                           })

    
    success_test('tour', 'Tour Created', {'tourName' : 'Stanford',
                                          'description' : 'Tour of Memorable Stanford Locations',
                                          }, cookie)
    
    
    """ Latitude and Longitude are only required fields
    
        if only lat/long are specified then the node is a pseudo node

        brief is for the small pop up on the map that shows
        up when users are within a 1/4 mile radius from the latitude
        and longitude it is optional

        prevNode is optional if specified this node will be inserted
            after the node specified by prevNode if prevNode is specified
            and set to null this node will be prepended 
            If missing it will be placed as the last node in the tour
           
        content is optional it is in the format
           [ Page, Page, Page]
           Page = [{section}, {section}, {section}]
           section = {
               /* Required fields */
               xpos, ypos, contentType, contentId||content

               /* Any other key can be defined without affecting
                  server and will be stored to mongo */
                     }

          contentId is the key to the file that is also being uploaded
          content is for raw text content (may also be html)
          contentType is the mime data type for the content
          
          One of content/contentId is required for each section

          ANY ADDITIONAL KEY VALUE PAIR CAN BE INCLUDED
          this makes it convienent to store section specific
          information for rendering or what have you

    """
    node_data = {
        'tourId' : 1,
        'latitude': 37.418,
        'longitude': -122.172,
        'brief' : {
            'title' : 'Test Node',
            'description' : 'This is a test node that has three repetive pics',
            'thumbId' : 'thumb1' },
        'content' : [
            [{'xpos' : 0, 'ypos' : 0,
              'width' : 20, 'height': 20,
              'contentType' : 'image/jpg',
              'contentId' : 'image1',
              'title' : 'A picture'},
             {'xpos' : 20, 'ypos' : 0,
              'width' : 40, 'height': 20,
              'contentType' : 'text/plain',
              'content' : 'Hello World this is plain'
              },
             {'xpos' : 60, 'ypos': 0,
              'width': 20, 'height': 20,
              'contentType' : 'image/jpg',
              'contentId' : 'image2',
              'title' : 'Something Else'
              }
             ],
            [{'xpos' : 0, 'ypos' : 0,
              'width': 60, 'height' : 60,
              'contentType' : 'image/jpg',
              'contentId' : 'image3',
              'random' : 'A random key which should have no effect on anything'
              },
             {'xpos' : 0, 'ypos' : 60,
              'width':60, 'height':60,
              'contentType' : 'text/html',
              'content' : '<h1>Hello</h1><p>Html World</p><p>For pretty formating perhaps</p>',
              'htmlHelpParam': 'something additional that is not used'
              }
             ]
            ]
        }
    data = {'image1' : open('IMG_0137.JPG'),
            'image2' : open('IMG_0137.JPG'),
            'image3' : open('IMG_0137.JPG'),
            'thumb1' : open('IMG_0137.JPG'),
            'nodeData' : json.dumps(node_data)
            }

    #Node 1
    res = send_files('node', data, cookie)
    assert_result(res)

    
    #Tests missing file and missing content, only brief available
    #Node 2
    brief_node = {
        'nodeData' : json.dumps({
                'tourId' : 1,
                'latitude': 37.418,
                'longitude': -122.172,
                'brief' : {
                    'title' : 'Test Node',
                    'description' : 'This is a test node that has three repetive pics',
                    'thumbId' : 'thumb1'
                    }
                })}
    res = send_request('node', brief_node, cookie)[0];
    assert_result(res)


    #Node 3
    pseudo_node = {
        'nodeData' : json.dumps({
                'tourId' : 1,
                'latitude' : 38.23,
                'longitude' : -121.18,
                })
        }

    res = send_request('node', pseudo_node, cookie)[0];
    assert_result(res)

    # Node 4
    node_data['latitude'] = 37.618
    node_data['longitude'] = -122.28;
    data['nodeData'] = json.dumps(node_data)

    res = send_files('node', data, cookie)
    assert_result(res)

    #Node 5
    node_data['latitude'] = 36.618
    node_data['longitude'] = -122.91;
    data['nodeData'] = json.dumps(node_data)
    
    res = send_files('node', data, cookie)
    assert_result(res)
    print 'Successfully created tour with 5 nodes'

def test_create_tour2 ():
    """ This test ensures that the extra options for prevNode work correctly
        ensuring that ordering is correct when things are prepended, inserted and
        appended """
    
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'pass' : 'samo'
                           })

    
    success_test('tour', 'Tour Created', {'tourName' : 'Stanford2',
                                          'description' : 'Tour of Memorable Stanford Locations',
                                          }, cookie)
    
    node_data = {'tourId' : 2,
                'latitude' : 999,
                'longitude' : 999,
                'brief' : {
                    'title' : 'Test Node',
                    'description' : 'Whatever'
                    }
                 }

    # create 2 nodes 
    node_data['content'] = [[{'content' : 2}]]
    pseudo_node = { 'nodeData' : json.dumps(node_data)}
    res = send_request('node', pseudo_node, cookie)[0];
    assert_result(res)
    first_node = res['result']['nodeId']

    
    node_data['content'] = [[{'content' : 4}]]
    pseudo_node = { 'nodeData' : json.dumps(node_data)}
    res = send_request('node', pseudo_node, cookie)[0];
    assert_result(res)

    node_data['prevNode'] = None
    node_data['latitude'] = 111
    node_data['longitude'] = 111
    node_data['content'] = [[{'content' : 1}]]
    pseudo_node = { 'nodeData' : json.dumps(node_data)}
    res = send_request('node', pseudo_node, cookie)[0];
    assert_result(res)

    node_data['content'] = [[{'content' : 0}]]
    pseudo_node = { 'nodeData' : json.dumps(node_data)}
    res = send_request('node', pseudo_node, cookie)[0];
    assert_result(res)

    
    #insert 1 node after the first original node
    node_data['prevNode'] = first_node
    node_data['latitude'] = 200
    node_data['longitude'] = 200
    node_data['content'] = [[{'content' : 3}]]
    pseudo_node = { 'nodeData' : json.dumps(node_data)}
    res = send_request('node', pseudo_node, cookie)[0];
    assert_result(res)

    res = send_request('tour?tourId=2', None, cookie)[0]
    assert_tour(res, 'Stanford2', 5)
    
    expected_order = [111, 111, 999, 200, 999, 999]

    for idx, val in enumerate(res['tour']['nodes']):
        assert 'latitude' in val
        assert 'longitude' in val
        assert 'mongoId' in val
        assert val['mongoId'] != None
        assert val['latitude'] == expected_order[idx]
        assert val['longitude'] == expected_order[idx]

    print 'Successfully Inserted Prepended and appended nodes'
        

def test_get_tour():
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'pass' : 'samo'
                           })

    res = send_request('tour?tourId=1', None, cookie)[0]
    assert_tour(res, 'Stanford', 5)
    
    res = send_request('tour?tourName=Stanford', None, cookie)[0]
    assert_tour(res, 'Stanford', 5)

    print 'Successfully Retrieved tour with 5 nodes'

def assert_small_node(res, mongo_id, idx):
    assert '_id' in res
    assert res['_id'] == unicode(mongo_id)
    assert 'content' in res
    assert len(res['content']) == 1
    assert 'page' in res['content'][0]
    assert len(res['content'][0]['page']) == 1
    assert 'content' in res['content'][0]['page'][0]
    assert res['content'][0]['page'][0]['content'] == idx
    
def test_get_nodes():
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'pass' : 'samo'
                           })

    tour = send_request('tour?tourId=2', None, cookie)[0]
    assert_tour(tour, 'Stanford2', 5)
    for idx, node in enumerate(tour['tour']['nodes']):
        assert 'nodeId' in node
        assert 'mongoId' in node
        node_id = node['nodeId']
        mongo_id = node['mongoId']
        res = send_request('nodeContent?nodeId='+str(node_id), None, cookie)[0]
        assert_small_node(res, mongo_id, idx)

    for idx, node in enumerate(tour['tour']['nodes']):
        mongo_id = node['mongoId']
        res = send_request('nodeContent?mongoId='+str(mongo_id), None, cookie)[0]
        assert_small_node(res, mongo_id, idx)

    print 'Node Retrieval Tests Successful'

def test_file_retreive():
    cookie = success_test('login', 'Successfully authenticated',
                          {'userName' : 'samo',
                           'pass' : 'samo'
                           })
    tour = send_request('tour?tourId=1', None, cookie)[0]
    assert_tour(tour, 'Stanford', 5);
    
    node_id = tour['tour']['nodes'][0]['nodeId']
    mongo_id = tour['tour']['nodes'][0]['mongoId']
    res = send_request('nodeContent?nodeId='+str(node_id), None, cookie)[0]
    content_id = res['content'][0]['page'][0]['contentId']
    res = send_request('mongoFile?mongoFile='+content_id, None, cookie, True)[0]
    
    compare = open('IMG_0137.JPG').read()
    x = open('out1.jpg', 'w')
    x.write(res)
    x.close()
    assert res == compare
    print 'File Retrieval Test Successful'
    
    
        
test_user_create()
test_user_auth()
test_user_get()
test_create_tour()
test_create_tour2()
test_get_tour()
test_get_nodes()
test_file_retreive()

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



