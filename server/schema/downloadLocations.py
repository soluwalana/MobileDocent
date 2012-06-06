#!/usr/bin/python
# -*- coding: utf-8 -*-
import zipfile
import urllib2
from urllib import FancyURLopener
from xml.dom import minidom
import os
import sys
import MySQLdb

CHUNK_SIZE = 5000

def valueChunks (inputFile, size):
    outputValues = []
    count = 0
    
    while True:
        count += 1
        line = inputFile.readline()
        outputValues.append(line)
        if line == '' or count == size:
            yield outputValues
            count = 0
            outputValues = []
        if line == '':
            return 
            
def insert_locations(db, cursor, locations):
    
    for lines in valueChunks(locations, CHUNK_SIZE):
        
        query = 'insert ignore into locations (locId, country, region, city, postalCode, latitude, longitude, metroCode, areaCode ) values '
        first = True
        print 1
        for line in lines:
            if (line == "" ): break
            line = line[0:-1]
            line = line.replace('"', '')
            columns = line.split(",")
            
            columns = [column.decode('latin-1') for column in columns]
            columns = '( "'+('", "'.join(columns))+'" )';
            if first:
                query += columns
                first = False
            else :
                query += ', '+columns
        """query += ' on duplicate key update country=values(country), region=values(region), '
        query += ' city=values(city), postalCode=values(postalCode), latitude=values(latitude), '
        query += ' longitude=values(longitude), metroCode=values(metroCode), areaCode=values(areaCode)'"""
        cursor.execute(query)   
        db.commit()
        
def insert_blocks(db, cursor, blocks):
    
    for lines in valueChunks(blocks, CHUNK_SIZE):
        
        query0 = 'insert ignore into locations (locId) values '
        query = 'insert into ipBlocks (startIpNum, endIpNum, locId) values '
        first = True
        for line in lines:
            if (line == "" ): break
            line = line[0:-1]
            line = line.replace('"', '')
            columns = line.split(",")

            missing = '( '+columns[2] +' )'
            columns = [column.decode('utf_8') for column in columns]
            columns = '( "'+('", "'.join(columns))+'" )';

            if first:
                query += columns
                query0 += missing
                first = False
            else :
                query += ', '+columns
                query0 += ', '+missing
                
        cursor.execute(query0)
        db.commit()
        
        cursor.execute(query)
        db.commit()
                        
        
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
    
    insert_locations(db, cursor, location)
    insert_blocks(db, cursor, blocks)
    
    
    
main()
