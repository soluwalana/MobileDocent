var should = require('should');

var mysql = require('db-mysql');

var CassandraSystem = require('cassandra-client').System;
var cassSys = new CassandraSystem('127.0.0.1:9160');


describe('Array', function(){
	describe('#indexOf()', function(){
		it('should return -1 when the value is not present', function(){
			[1,2,3].indexOf(5).should.equal(-1);
			[1,2,3].indexOf(0).should.equal(-1);
		});
	});
});

