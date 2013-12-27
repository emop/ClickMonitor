# -*- coding: utf-8 -*-

from taodian_api import Taodian as TaodianApi

class Taodian(object):
    def __init__(self, api_id, api_key, api_url):
        self.api = TaodianApi(api_id, api_key, api_url)
    
    def call_taodian_api(self, api_name, **kw):
        api = getattr(self.api, api_name)
        
        return api(**kw)
    