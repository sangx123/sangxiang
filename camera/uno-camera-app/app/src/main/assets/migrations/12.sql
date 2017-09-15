DROP TABLE IF EXISTS sync_info;

 CREATE TABLE sync_info(Id INTEGER PRIMARY KEY,device_registration_id TEXT,date TEXT,unique_key TEXT UNIQUE,event_type TEXT);