#run as root

# mysql
apt-get install mysql-common mysql-client mysql-server libmysqld-dev -y

# apache
apt-get install apache2 -y

#python2.6
apt-get install python2.6 -y

# mongodb
apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' /etc/apt/sources.list

apt-get update
apt-get remove mongo*
apt-get install mongodb-10gen

#Install Node.JS
wget 'http://nodejs.org/dist/v0.6.15/node-v0.6.15.tar.gz' -O node.tar.gz
tar -xvzf node.tar.gz
cd node-v0.6.15
./configure
make
make install
cd ..
rm -rf node-v0.6.15
rm node.tar.gz

# Mysql C connector
wget 'http://mysql.he.net/Downloads/Connector-C/mysql-connector-c-6.0.2.tar.gz' -O mysql-connector-c-6.0.2.tar.gz
tar -xvf mysql-connector-c-6.0.2.tar.gz
cd mysql-connector-c-6.0.2
cmake -G "Unix Makefiles"
make
make install
cd ..
rm -rf mysql-connector-c-6.0.2
rm -rf mysql-connector-c-6.0.2.tar.gz

curl http://npmjs.org/install.sh | sh

npm install -g express
npm install -g mocha
npm install -g should
npm install -g helenus
npm install -g db-mysql
npm install -g GridFS
npm install -g mongodb --mongodb:native
npm install -g mime

su samo
npm install express
npm install mocha
npm install should
npm install helenus
npm install db-mysql
npm install GridFS
npm install mongodb --mongodb:native
npm install mime
logout