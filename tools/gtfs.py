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

        cur.execute('''CREATE TABLE agency(agency_name TEXT, 
                                           agency_url TEXT NOT NULL, 
                                           agency_timezone TEXT NOT NULL)''')     
        agency = csv.CSV(z.open("agency.txt"))
        records = agency.records
        agency_name_index     = agency.index("agency_name")
        agency_url_index      = agency.index("agency_url")
        agency_timezone_index = agency.index("agency_timezone")
        for record in records: 
            fields = record
            cur.execute("INSERT INTO agency VALUES(:agency_name, :agency_url, :agency_timezone)", 
                        {'agency_name':     fields[agency_name_index], 
                         'agency_url':      fields[agency_url_index], 
                         'agency_timezone': fields[agency_timezone_index]})

        cur.execute('''CREATE TABLE stops(stop_id TEXT PRIMARY KEY, 
                                          stop_name TEXT NOT NULL, 
                                          stop_lat TEXT NOT NULL, 
                                          stop_lon TEXT NOT NULL)''')            
        stops = csv.CSV(z.open("stops.txt"))
        records = stops.records
        stop_id_index   = stops.index("stop_id")
        stop_name_index = stops.index("stop_name")
        stop_lat_index  = stops.index("stop_lat")
        stop_lon_index  = stops.index("stop_lon")
        for record in records: 
            fields = record
            cur.execute("INSERT INTO stops VALUES(:stop_id, :stop_name, :stop_lat, :stop_lon)", 
                        {'stop_id':   fields[stop_id_index], 
                         'stop_name': fields[stop_name_index], 
                         'stop_lat':  fields[stop_lat_index], 
                         'stop_lon':  fields[stop_lon_index]})

        cur.execute('''CREATE TABLE routes(route_id TEXT PRIMARY KEY, 
                                           route_short_name TEXT NOT NULL, 
                                           route_long_name TEXT NOT NULL, 
                                           route_type INTEGER NOT NULL)''')            
        routes = csv.CSV(z.open("routes.txt"))
        records = routes.records
        route_id_index         = routes.index("route_id")
        route_short_name_index = routes.index("route_short_name")
        route_long_name_index  = routes.index("route_long_name")
        route_type_index       = routes.index("route_type")
        for record in records: 
            fields = record
            cur.execute("INSERT INTO routes VALUES(:route_id, :route_short_name, :route_long_name, :route_type)", 
                        {'route_id':         fields[route_id_index], 
                         'route_short_name': fields[route_short_name_index], 
                         'route_long_name':  fields[route_long_name_index], 
                         'route_type':       fields[route_type_index]})

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
        calendar = csv.CSV(z.open("calendar.txt"))
        records = calendar.records
        service_id_index = calendar.index("service_id")
        monday_index     = calendar.index("monday")
        tuesday_index    = calendar.index("tuesday")
        wednesday_index  = calendar.index("wednesday")
        thursday_index   = calendar.index("thursday")
        friday_index     = calendar.index("friday")
        saturday_index   = calendar.index("saturday")
        sunday_index     = calendar.index("sunday")                                                
        start_date_index = calendar.index("start_date")
        end_date_index   = calendar.index("end_date")
        for record in records: 
            fields = record
            cur.execute('''INSERT INTO calendar VALUES(:service_id, :monday, :tuesday, :wednesday, :thursday, 
                                                       :friday, :saturday, :sunday, :start_date, :end_date)''',
                        {'service_id': fields[service_id_index], 
                         'monday':     fields[monday_index],
                         'tuesday':    fields[tuesday_index],
                         'wednesday':  fields[wednesday_index],
                         'thursday':   fields[thursday_index],
                         'friday':     fields[friday_index],
                         'saturday':   fields[saturday_index],
                         'sunday':     fields[sunday_index],
                         'start_date': fields[start_date_index],
                         'end_date':   fields[end_date_index]})

                              
        cur.execute('''CREATE TABLE trips(route_id TEXT REFERENCES routes(route_id), 
                                          service_id TEXT REFERENCES calendar(service_id), 
                                          trip_id TEXT PRIMARY KEY)''')
        trips = csv.CSV(z.open("trips.txt"))
        records = trips.records
        route_id_index   = trips.index("route_id")
        service_id_index = trips.index("service_id")        
        trip_id_index    = trips.index("trip_id")
        for record in records: 
            fields = record
            cur.execute("INSERT INTO trips VALUES(:route_id, :service_id, :trip_id)", 
                        {'route_id':   fields[route_id_index],
                         'service_id': fields[service_id_index],
                         'trip_id':    fields[trip_id_index]})

        cur.execute('''CREATE TABLE stop_times(trip_id TEXT REFERENCES trips(trip_id), 
                                               arrival_time TEXT NOT NULL, 
                                               departure_time TEXT NOT NULL, 
                                               stop_id TEXT REFERENCES stops(stop_id), 
                                               stop_sequence TEXT NOT NULL)''')            
        stop_times = csv.CSV(z.open("stop_times.txt"))
        records = stop_times.records
        trip_id_index        = stop_times.index("trip_id")
        arrival_time_index   = stop_times.index("arrival_time")
        departure_time_index = stop_times.index("departure_time")
        stop_id_index        = stop_times.index("stop_id")
        stop_sequence_index  = stop_times.index("stop_sequence")
        for record in records: 
            fields = record
            cur.execute("INSERT INTO stop_times VALUES(:trip_id, :arrival_time, :departure_time, :stop_id, :stop_sequence)", 
                        {'trip_id':        fields[trip_id_index],
                         'arrival_time':   fields[arrival_time_index],
                         'departure_time': fields[departure_time_index],
                         'stop_id':        fields[stop_id_index],
                         'stop_sequence':  fields[stop_sequence_index]})
                                    
if __name__ == '__main__': 
    city = 'new_york'
    gtfs = 'mta-new-york-city-transit_20140205_0118.zip'
    get_city(city, gtfs)

