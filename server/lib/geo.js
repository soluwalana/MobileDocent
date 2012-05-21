var _ = require('./underscore.js')._;

/* Earths radius in miles */
var EARTH_RADIUS = 3959;

var degreesToRadians = function (input){
    return (Math.PI/180) * input;
};
var radiansToDegrees = function (input){
    return (180/Math.PI) * input;
};

/* Computes the great circle distance between two points in latitude
   longitude coordinates expressed in radians

   2 \cdot arcsin (\sqrt{\sin{\delta Latitude / 2}^2 +
                         cos(long1)\cdot cos(long2) \cdot \sin{\delta long / 2}^2}
                   ) * radius == distance between two points on a sphere
*/
var gcDist = function (lat1, long1, lat2, long2){
    var deltaLat = lat2 - lat1;
    var deltaLong = long2 - long1;
    var centralAngle = 2 * Math.asin(
        Math.sqrt(
            Math.pow(Math.sin(deltaLat/2), 2) + 
                (Math.cos(lat1)*Math.cos(lat2)*
                 Math.pow(Math.sin(deltaLong/2), 2))
        )
    );
    return EARTH_RADIUS * centralAngle;

};

/* Wrapper that converts degrees to radians then calls the central function */
   
var gcDistDeg = function(lat1, long1, lat2, long2){
    return gcDist(degreesToRadians(lat1), degreesToRadians(long1),
                  degreesToRadians(lat2), degreesToRadians(long2));
};

var sortGenerator = function (orgLat, orgLong){
    return function (obj1, obj2){
        var hasLoc1 = (_.isNumber(obj1.latitude) && _.isNumber(obj1.longitude));
        var hasLoc2 = (_.isNumber(obj2.latitude) && _.isNumber(obj2.longitude));
        
        if (!hasLoc1 && !hasLoc2) return 0;
        if (hasLoc1 && !hasLoc2) return 1;
        if (!hasLoc1 && hasLoc2) return -1;
        
        /* Actually compare distances */        
        var dist1 = gcDistDeg(orgLat, orgLong,
                              obj1.latitude, obj1.longitude);
        
        var dist2 = gcDistDeg(orgLat, orgLong,
                              obj2.latitude, obj2.longitude);

        return dist1 - dist2;
    };
};

exports.sortGenerator = sortGenerator;
exports.gcDistDeg = gcDistDeg;
exports.gcDist = gcDist;