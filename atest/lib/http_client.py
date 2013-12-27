# -*- coding: utf-8 -*-

import urllib2, httplib, cookielib
import StringIO
import gzip
from urlparse import urlparse
import logging
import socket
import os
import urllib
import time
import re

# timeout in seconds
timeout = 10
socket.setdefaulttimeout(timeout)

class HTTPClient(object):
    
    def __init__(self, www_root=None):
        self.proxy = None
        self.last_url = None
        self.logger = logging.getLogger("HttpClient")
        
        self.cookies = cookielib.MozillaCookieJar("http_cookie.txt")
        self.cookie_handler = urllib2.HTTPCookieProcessor(self.cookies)
        
    
    def set_proxy(self, proxy, www_root=None):
        if proxy:
            self.proxy = urllib2.ProxyHandler(proxy)
            
    def _http_handlers(self):
        h = [self.cookie_handler, ]
        if self.proxy: h.append(self.proxy)
        return h
    
    def relative_path(self, url):
        
        if url.startswith("http:"):
            self.last_url = urlparse(url)
        elif url.startswith('/'):
            pass
        elif url.startswith('?'):
            pass
        else:
            pass
        
        return self.last_url.geturl()
    
    def download(self, url, save_as):
        
        self.logger.info("download:%s --> %s" % (url, save_as))
        self.post(url, save_as)
        
        return '200'
        
    def post(self, url, save_as, data=None):
        if data and isinstance(data, dict):
            data = urllib.urlencode(data)
        elif data and os.path.isfile(data):
            fd = open(data, 'r')
            data = fd.read()
            fd.close()
            
        if not exists_path(dir_name(save_as)): make_path(dir_name(save_as))
        
        data = self._http_request(url, data)
        
        fd = open(save_as, "wb")
        fd.write(data)
        fd.close()
        
        return '200'
    
    def get(self, url):
        return self._http_request(url, None)
        
    def post_data(self, url, data, headers={}):
        if data and isinstance(data, dict):
            data = urllib.urlencode(data)
        elif data and os.path.isfile(data):
            fd = open(data, 'r')
            data = fd.read()
            fd.close()
        return self._http_request(url, data, headers)

    
    def _http_request(self, url, req_data, headers={}):
    
        #url = self.relative_path(url)
        data = None
        try:
            #httplib.HTTPConnection.debuglevel = 1
            if req_data:
                request = urllib2.Request(url, req_data)
            else:
                request = urllib2.Request(url)
                
            request.add_header('Accept-encoding', 'gzip') 
            for k, v in headers.iteritems():
                request.add_header(k, v)
                            
            opener = urllib2.build_opener(*self._http_handlers())
            
            for i in range(3):
                try:
                    f = opener.open(request)
                    #print f
                    encoding = f.headers.get('Content-Encoding')                    
                    trans_encoding = f.headers.get('Transfer-Encoding')
                    
                    #self.logger.info("header:%s" % f.headers)
                    if encoding and 'gzip' in encoding:
                        #compresseddata = f.read()                              
                        compresseddata = self._read_data(f, trans_encoding=='chunked')
                        compressedstream = StringIO.StringIO(compresseddata)
                        gzipper = gzip.GzipFile(fileobj=compressedstream)   
                        data = gzipper.read()
                        gzipper.close()
                    else:
                        data = self._read_data(f, trans_encoding=='chunked') # f.read()
                        f.close()
                    break
                except Exception, e:
                    if re.search(r"(timed out|reset by peer)", str(e)):
                        self.logger.info("Time out, sleep 5 seconds then retry, url:%s" % url)
                        time.sleep(5)
                    else:
                        raise
            
            #content_size = f.headers.get("Content-Length", 0)
            #if 'gzip' not in encoding and int(content_size) > 0 and int(content_size) != len(data):
            #    raise Exception("Content size error:%s != %s" %(int(content_size), len(data)))
            #else:
            #    #self.logger.info("Data read right:%s == %s" %(int(content_size), len(data)))
            #    pass
       
        except urllib2.HTTPError, e:
            raise   
        
        return data
        
    def _read_data(self, fd, chunked=False):
        data = fd.read()
        while chunked:
            ch = fd.read(1024)
            if not ch:break
            data += ch
        return data
    
    def close(self):
        self.logger.info("save cookies to 'http_cookie.txt'....")
        self.cookies.save("http_cookie.txt", True, True)
    
    