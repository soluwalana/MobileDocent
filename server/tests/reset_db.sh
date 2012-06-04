cd ../schema
mysql -u docent -pDocent_2012 -e 'source schema.sql'
#mysql -u docent -pDocent_2012 docent_db -e 'source locations.sql'
#mysql -u docent -pDocent_2012 docent_db -e 'source ipBlocks.sql'
mongo docent dropMongo.js
cd ../tests