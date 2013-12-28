#-*- coding: utf8 -*-
import redis

class RunRedisCommand:
	def __init__(self, host="127.0.0.1", port=6379):
		self.cons = dict()
		self.host = host
		self.port = int(port)
		#self.f = redis.StrictRedis(host=host, port=int(port) )
	def get_data(self, key, db=0):
		f = self._get_con(db)
		val = f.get(key)
		return val
		
	def get_sets(self, key, db=0):
		f = self._get_con(db)
		set = f.smembers(key)
		return set

	def run_redis_command(self, command, keys, db=0):
		f = self._get_con(db)

		str = "f.%s($s)" %(command, keys)
		return eval(str)
	
	def cleanup_db_data(self, db=0):
		f = self._get_con(db)
		f.flushdb()
		
	def cleanup_db_data_all(self):
		f = self._get_con(0)
		f.flushall()
	
	def _get_con(self, db):
		db = int(db)
		f = self.cons.get(db)
		if f is None:
			f = redis.StrictRedis(host=self.host, port=self.port, db=int(db) )
			self.cons[db] = f
		return f
