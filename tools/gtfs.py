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
    z.extractall()

    with con:
        cur = con.cursor()    

        cur.execute('''CREATE TABLE agency(agency_name TEXT, 
                                           agency_url TEXT NOT NULL, 
                                           agency_timezone TEXT NOT NULL)''')     
        agency = csv.DictReader(open('agency.txt', 'r'), delimiter=',', quotechar='"')                                           
        for record in agency: 
            cur.execute("INSERT INTO agency VALUES(:agency_name, :agency_url, :agency_timezone)", 
                        {'agency_name':     record['agency_name'], 
                         'agency_url':      record['agency_url'], 
                         'agency_timezone': record['agency_timezone']})

        cur.execute('''CREATE TABLE stops(stop_id TEXT PRIMARY KEY, 
                                          stop_name TEXT NOT NULL, 
                                          stop_lat TEXT NOT NULL, 
                                          stop_lon TEXT NOT NULL)''')            
        stops = csv.DictReader(open('stops.txt', 'r'), delimiter=',', quotechar='"')                                           
        for record in stops: 
            cur.execute("INSERT INTO stops VALUES(:stop_id, :stop_name, :stop_lat, :stop_lon)", 
                        {'stop_id':   record['stop_id'], 
                         'stop_name': record['stop_name'], 
                         'stop_lat':  record['stop_lat'], 
                         'stop_lon':  record['stop_lon']})

        cur.execute('''CREATE TABLE routes(route_id TEXT PRIMARY KEY, 
                                           route_short_name TEXT NOT NULL, 
                                           route_long_name TEXT NOT NULL, 
                                           route_type INTEGER NOT NULL)''')            
        routes = csv.DictReader(open('routes.txt', 'r'), delimiter=',', quotechar='"')
        for record in routes: 
            fields = record
            cur.execute("INSERT INTO routes VALUES(:route_id, :route_short_name, :route_long_name, :route_type)", 
                        {'route_id':         record['route_id'], 
                         'route_short_name': record['route_short_name'], 
                         'route_long_name':  record['route_long_name'], 
                         'route_type':       record['route_type']})

        # Move table calender before trips since file calendar.txt should be processed before trips.txt for service_id.
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
        calendar = csv.DictReader(open('calendar.txt', 'r'), delimiter=',', quotechar='"')
        for record in calendar: 
            cur.execute('''INSERT INTO calendar VALUES(:service_id, :monday, :tuesday, :wednesday, :thursday, 
                                                       :friday, :saturday, :sunday, :start_date, :end_date)''',
                        {'service_id': record['service_id'], 
                         'monday':     record['monday'],
                         'tuesday':    record['tuesday'],
                         'wednesday':  record['wednesday'],
                         'thursday':   record['thursday'],
                         'friday':     record['friday'],
                         'saturday':   record['saturday'],
                         'sunday':     record['sunday'],
                         'start_date': record['start_date'],
                         'end_date':   record['end_date']})

                              
        cur.execute('''CREATE TABLE trips(route_id TEXT REFERENCES routes(route_id), 
                                          service_id TEXT REFERENCES calendar(service_id), 
                                          trip_id TEXT PRIMARY KEY)''')
        trips = csv.DictReader(open('trips.txt', 'r'), delimiter=',', quotechar='"')
        for record in trips: 
            cur.execute("INSERT INTO trips VALUES(:route_id, :service_id, :trip_id)", 
                        {'route_id':   record['route_id'],
                         'service_id': record['service_id'],
                         'trip_id':    record['trip_id']})

        cur.execute('''CREATE TABLE stop_times(trip_id TEXT REFERENCES trips(trip_id), 
                                               arrival_time TEXT NOT NULL, 
                                               departure_time TEXT NOT NULL, 
                                               stop_id TEXT REFERENCES stops(stop_id), 
                                               stop_sequence TEXT NOT NULL)''')            
        stop_times = csv.DictReader(open('stop_times.txt', 'r'), delimiter=',', quotechar='"')
        for record in stop_times: 
            cur.execute("INSERT INTO stop_times VALUES(:trip_id, :arrival_time, :departure_time, :stop_id, :stop_sequence)", 
                        {'trip_id':        record['trip_id'],
                         'arrival_time':   record['arrival_time'],
                         'departure_time': record['departure_time'],
                         'stop_id':        record['stop_id'],
                         'stop_sequence':  record['stop_sequence']})

        # Delete extracted files.
                                    
if __name__ == '__main__': 
    city = 'new_york'
    gtfs = 'mta-new-york-city-transit_20140205_0118.zip'
    get_city(city, gtfs)

