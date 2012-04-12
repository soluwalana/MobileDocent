#Install Node.JS
wget 'http://nodejs.org/dist/node-v0.6.15.tar.gz' -O node.tar.gz
tar -xvzf node.tar.gz
cd node-v0.4.12
./configure
make
make install
cd ..
rm -rf node
rm node.tar.gz

#install NPM
curl http://npmjs.org/install.sh | sh

npm install mocha
npm install should
npm install cassandra-cleint
npm install db-mysql

