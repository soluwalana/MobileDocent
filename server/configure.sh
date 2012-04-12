#!/bin/bash
mysql -u root -pStanford125461 -e "create user docent identified by 'Docent_2012'"
mysql -u root -pStanford125461 -e "create database docent_db"
mysql -u root -pStanford125461 -e "grant usage on *.* to docent identified by 'Docent_2012'"
mysql -u root -pStanford125461 -e "grant all privileges on docent_db.* to docent"

