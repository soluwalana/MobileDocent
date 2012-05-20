cd ../schema
mysql -u docent -pDocent_2012 -e 'source schema.sql'
mongo docent dropMongo.js
cd ../tests