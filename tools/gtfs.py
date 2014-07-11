#!/usr/bin/python
# -*- coding: utf-8 -*-
# Download GTFS data, convert to sqlite3 db per city.
# Reference: https://developers.google.com/transit/gtfs/reference

import sqlite3 as sqlite
import sys
import zipfile
import csv

def get_city(city_name, gtfs_name):
    print(city_name)
    db = city_name + ".db"
    con = sqlite.connect(db)
    z = zipfile.ZipFile(gtfs_name, "r", zipfile.ZIP_DEFLATED)

    with con:
        cur = con.cursor()    

        # Schemas according to mandatory fields of mandatory files from GTFS.
        cur.execute('''CREATE TABLE agency(agency_name TEXT, 
                                           agency_url TEXT NOT NULL, 
                                           agency_timezone TEXT NOT NULL)''')       
        cur.execute('''CREATE TABLE stops(stop_id TEXT PRIMARY KEY, 
                                          stop_name TEXT NOT NULL, 
                                          stop_lat TEXT NOT NULL, 
                                          stop_lon TEXT NOT NULL)''')
        cur.execute('''CREATE TABLE routes(route_id TEXT PRIMARY KEY, 
                                           route_short_name TEXT NOT NULL, 
                                           route_long_name TEXT NOT NULL, 
                                           route_type INTEGER NOT NULL)''')
        # Move table calender before trips since file calendar.txt should be processed first for service_id.
        cur.execute('''CREATE TABLE calendar(service_id TEXT PRIMARY KEY, 
                                             monday INTEGER NOT NULL, 
                                             tuesday INTEGER NOT NULL, 
                                             wednesday INTEGER NOT NULL, 
                                             thursday INTEGER NOT NULL, 
                                             friday INTEGER NOT NULL, 
                                             saturday INTEGER NOT NULL, 
                                             sunday INTEGER NOT NULL, 
                                             start_date TEXT NOT NULL, 
                                             end_date TEXT NOT NULL)''')                                 
        cur.execute('''CREATE TABLE trips(route_id TEXT REFERENCES routes(route_id), 
                                          service_id TEXT REFERENCES calendar(service_id), 
                                          trip_id TEXT PRIMARY KEY)''')
        cur.execute('''CREATE TABLE stop_times(trip_id TEXT REFERENCES trips(trip_id), 
                                               arrival_time TEXT NOT NULL, 
                                               departure_time TEXT NOT NULL, 
                                               stop_id TEXT REFERENCES stops(stop_id), 
                                               stop_sequence TEXT NOT NULL)''')

        stops = csv.CSV(z.open("stops.txt"))
        records = stops.records
        for record in records: 
            fields = record
            cur.execute("INSERT INTO stops VALUES(:stop_id, :stop_name, :stop_lat, :stop_lon)", 
                        {'stop_id': fields[0], 'stop_name': fields[2], 
                         'stop_lat': fields[4], 'stop_lon': fields[5]})

if __name__ == '__main__': 
    city = 'new_york'
    gtfs = 'mta-new-york-city-transit_20140205_0118.zip'
    get_city(city, gtfs)

