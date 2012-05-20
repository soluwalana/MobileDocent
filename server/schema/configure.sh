#!/bin/bash
PASS=$1
mysql -u root -p$PASS -e "create user docent identified by 'Docent_2012'"
mysql -u root -p$PASS -e "create database docent_db"
mysql -u root -p$PASS -e "grant usage on *.* to docent identified by 'Docent_2012'"
mysql -u root -p$PASS -e "grant all privileges on docent_db.* to docent"


