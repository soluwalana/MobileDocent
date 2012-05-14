#!/usr/local/bin/python2.7
import urllib2, json, time

SERVER_ADDR = 'http://samo.stanford.edu:8787/'

def send_request(link='', data=None):
    try:
        if data:
            data = json.dumps(data)
        url = SERVER_ADDR+link
        req = urllib2.Request(url, data, {"Origin" : "My House",
                                          "Content-Type" : "application/json"})
        res_data =  urllib2.urlopen(req).read()
        print res_data
        res_data = json.loads(res_data)
        return res_data
    except Exception as err:
        print err
        return None

print send_request('test')
print send_request('echo', {'data' : 'same'})
print send_request('user/1')
print send_request('tour/1')
print send_request('node/1')
print send_request('user', {'userName' : 'samo',
                            'device_id' : 'X28934',
                            'password' : 'samo',
                            'passwordConf' : 'samo',
                            'about' : 'I like CS'})

print send_request('ipLocation')

print send_request('location', {'city' : 'Stanford',
                                'region' : 'CA',
                                'country' : 'USA'})

print send_request('tour/1', {'tourName' : 'Stanford',
                              'description' : 'Tour of Memorable Stanford Locations',
                              'locId' : 11382,
                              'walkingDistance' : '1.5'
                              })


