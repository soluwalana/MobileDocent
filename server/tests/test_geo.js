var _ = require ('../lib/underscore.js')._;
var geo = require('../lib/geo.js');
var should = require('should').should;

describe('Geolocation Test', function (){

    it ('Tests', function (){
        
        var NYToSLC = geo.gcDistDeg(43, -110, 45, -75);
        var NYToSF = geo.gcDistDeg(38, -120, 45, -75);
        NYToSLC.should.be.ok
        NYToSF.should.be.ok;
        
        (NYToSF - NYToSLC).should.be.above(0);
        
        /* Reverse the order should be the same */
        var NYToSLC1 = geo.gcDistDeg(45, -75, 43, -110);
        var NYToSF1 = geo.gcDistDeg(45, -75, 38, -120);
        NYToSLC.should.be.ok
        NYToSF.should.be.ok;
        
        (NYToSF1 - NYToSLC1).should.be.above(0);
        
        NYToSLC.should.be.equal(NYToSLC1);
        NYToSF.should.be.equal(NYToSF1);
        
        var arr = [
            {'latitude' : 45, 'longitude' : -75, 'name' : 'NY'},
            {'latitude' : 38, 'longitude' : -120, 'name' : 'SF'},
            {'latitude' : 43, 'longitude' : -110, 'name' : 'SLC'},
            {'latitude' : 23, 'longitude' : -75,  'name' : 'Cancun'},
            {'latitude' : 23, 'longitude' : -160,  'name' : 'Hawaii'},
            {'latitude' : -40, 'longitude' : -180, 'name' : 'New Zealand'},
            {'latitude' : 38, 'longitude' : -15, 'name' : 'Spain'},
            {'latitude' : 47, 'longitude' : 0, 'name' : 'France'},
            {'latitude' : 40, 'longitude' : 135, 'name' : 'Japan'}
            
        ];

        /* lat 45, long -75 is NY */
        var expected = ['NY', 'Cancun', 'SLC', 'SF', 'Spain', 'France',
                        'Hawaii', 'Japan', 'New Zealand'];
        arr.sort(geo.sortGenerator(45, -75));
        
        var verify = [];
        arr.forEach(function(elem){
            verify.push(elem.name);
        });
        expected.should.be.eql(verify);

        /* lat 30, long -30 middle of atlantic */
        var expected = ['Spain', 'France', 'NY', 'Cancun', 'SLC', 'SF', 'Hawaii',
                        'Japan', 'New Zealand'];
        arr.sort(geo.sortGenerator(30, -30 ));

        var verify = [];
        arr.forEach(function(elem){
            verify.push(elem.name);
        });
        expected.should.be.eql(verify);


        /* lat 0, long 180 and lat 0 long -180 middle of pacific */
        var expected = ['Hawaii', 'New Zealand', 'Japan', 'SF', 'SLC', 'NY',
                        'Cancun', 'France', 'Spain'];
        arr.sort(geo.sortGenerator(0, 180));
        
        var verify = [];
        arr.forEach(function(elem){
            verify.push(elem.name);
        });
        expected.should.be.eql(verify);

        arr.sort(geo.sortGenerator(0, -180));
        
        var verify = [];
        arr.forEach(function(elem){
            verify.push(elem.name);
        });
        expected.should.be.eql(verify);
        
    });
});
      