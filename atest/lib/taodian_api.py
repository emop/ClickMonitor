# -*- coding: utf8 -*-

'''
Created on 2013-7-10

@author: deonwu
'''

from time import localtime, strftime
import os
try:
    import json
except:
    import simplejson as json
import urllib
import logging
import hashlib

import os

os.environ['TZ'] = "Asia/Shanghai"

class Taodian(object):
    def __init__(self, app_id = None, app_secret = None, host='http://api.zaol.cn/api/route'):
        #http://gw.api.taobao.com/router/rest
        #'http://gw.api.tbsandbox.com/router/rest?'
        self.app_id = app_id
        self.app_secret = app_secret or '995a5637240add612dfd30945adf1c99'
        self.host = host
        self.http = self._default_http()

    def __getattr__(self, name):
        if not name.startswith("__"):
            return CallProxy(name, self.http, self.app_secret, self.host, self.app_id)
        else:
            raise AttributeError("Not found attr '%s'" % name)


    def _default_http(self, ):
        from http_client import HTTPClient
        
        return HTTPClient()
            
class CallProxy(object):

    def __init__(self, name, http, secret, end_point_url, app_id):
        self.name = name
        self.http = http
        self.app_id = app_id
        self.secret = secret
        self.host = end_point_url

    def __call__(self, **kw):
        param = {'app_id': self.app_id, 'name': self.name, 'params': json.dumps(kw),
        }
        import datetime, os
        if os.environ.get('API_URL'):
        	param['service_url'] = os.environ['API_URL']
        
        timestamp = datetime.datetime.now().strftime('%Y%m%d%H%M%S')
        print "timestamp:%s" % timestamp
        
        key = "%s,%s,%s" % (self.app_id, timestamp, self.secret)
        
        sign = hashlib.md5(key).hexdigest().lower()
        param['time'] = timestamp
        param['sign'] = sign
        
        resp = self.http.post_data(self.host, param, {}) 
        print "resp:%s" % resp
        
        data = json.loads(resp, 'utf-8')
        return data

            
