
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
