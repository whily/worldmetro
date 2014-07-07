#!/usr/bin/python
# -*- coding: utf-8 -*-
# Download GTFS data, convert to sqlite3 db per city.

import sqlite3 as sqlite
import sys
import zipfile
import csv

def get_city(city_name, gtfs_name):
    db = city_name + ".db"
    con = sqlite.connect(db)
    z = zipfile.ZipFile(gtfs_name, "r", zipfile.ZIP_DEFLATED)

    with con:
        cur = con.cursor()    
        cur.execute("CREATE TABLE stops(stop_id TEXT, stop_name TEXT, stop_lat TEXT, stop_lon TEXT)")
        stops = csv.CSV(z.open("stops.txt"))
        lines = stops.lines
        for line in lines[1:]:     # Skip the caption
            fields = line.decode("utf-8").split(",")
            print(fields[0])
            cur.execute("INSERT INTO stops VALUES('101', 'Van Cortlandt Part - 242 St', '40.889248', '-73.898583')")

if __name__ == '__main__': 
    city = 'new_york'
    gtfs = 'mta-new-york-city-transit_20140205_0118.zip'
    get_city(city, gtfs)

