#!/usr/bin/python
# -*- coding: utf-8 -*-
# Download GTFS data, convert to sqlite3 db per city.

import sqlite3 as sqlite
import sys
import zipfile

city = 'new_york'
db = city + '.db'
con = sqlite.connect(db)

with con:
    cur = con.cursor()    
    cur.execute("CREATE TABLE stops(stop_id TEXT, stop_name TEXT, stop_lat TEXT, stop_lon TEXT)")
    cur.execute("INSERT INTO stops VALUES('101', 'Van Cortlandt Part - 242 St', '40.889248', '-73.898583')")
