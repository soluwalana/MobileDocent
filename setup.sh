#run as root

# mysql
apt-get install mysql-common mysql-client mysql-server libmysqld-dev -y

# apache
apt-get install apache2 -y

#python2.6
apt-get install python2.6 -y

# cassandra
if [ `cat /etc/apt/sources.list | grep -c cassandra` -eq 0 ]
then
	echo 'deb http://www.apache.org/dist/cassandra/debian 10x main' >> /etc/apt/sources.list
	echo 'deb-src http://www.apache.org/dist/cassandra/debian 10x main' >> /etc/apt/sources.list
fi
gpg --keyserver pgp.mit.edu --recv-keys F758CE318D77295D
gpg --export --armor F758CE318D77295D | sudo apt-key add -
gpg --keyserver pgp.mit.edu --recv-keys 2B5C1B00
gpg --export --armor 2B5C1B00 | sudo apt-key add -
apt-get update
apt-get install cassandra

# mongodb
apt-get install mongodb 

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