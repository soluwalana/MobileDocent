#!/usr/bin/python
# -*- coding: utf-8 -*-
import zipfile
import urllib2
from urllib import FancyURLopener
from xml.dom import minidom
import os
import sys
import MySQLdb

def insert_locations(cursor, locations):
    query = 'delete from locations'
    cursor.execute(query)
    for line in locations:
        if (line == "" ): break
        line = line[0:-1]
        line = line.replace('"', '')
        columns = line.split(",")
        query = 'insert ignore into locations (locId, country, region, city, postalCode, latitude, longitude, metroCode, areaCode ) values (%s, %s, %s, %s, %s, %s, %s, %s, %s)'  
        for idx, column in enumerate(columns):
            columns[idx] = columns[idx].decode('latin-1')
            
	cursor.execute(query, columns )   

def insert_blocks(cursor, blocks):
    query = 'delete from ipBlocks'
    cursor.execute(query)
    for line in blocks:
        if (line == "" ): break
        line = line[0:-1]
        line = line.replace('"', '')
        columns = line.split(",")
        query = 'insert into ipBlocks (startIpNum, endIpNum, locId) values (%s, %s, %s)'
        for idx, column in enumerate(columns):
            columns[idx] = columns[idx].decode('utf_8')
        
        cursor.execute(query, columns)

def main():
    db = MySQLdb.connect(host="localhost",
                         user="docent",
                         passwd="Docent_2012",
                         db="docent_db",
                         use_unicode = True,
                         charset = 'utf8')
    cursor = db.cursor()
    
    #zippedfile = urllib2.urlopen('http://geolite.maxmind.com/download/geoip/'+
    #                             'database/GeoLiteCity_CSV/GeoLiteCity_20120504.zip')
    zippedfile = open('GeoLiteCity_20120504.zip')

    tempFile = os.tmpfile()
    tempFile.write(zippedfile.read())
    zippedfile.close()
    unzippedfile = zipfile.ZipFile(tempFile,"r")
    
    blocks = os.tmpfile()
    blocks.write(unzippedfile.read(unzippedfile.namelist()[0]))
    blocks.seek(0)
    
    location = os.tmpfile()
    location.write(unzippedfile.read(unzippedfile.namelist()[1]))
    location.seek(0)
    
    unzippedfile.close()
    tempFile.close()

    for n in range(0,2):
        blocks.readline()
        location.readline()
        
    insert_locations(cursor, location)
    insert_blocks(cursor, blocks)
    
main()
