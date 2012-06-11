var fs = require('fs'), gm = require('gm');

//var maxHeight = 700;
//var maxWidth = 500;
var maxHeight = 170;
var maxWidth = 170;
var maxThumbDimension = 170;

var inStream = fs.createReadStream('./IMG_0137.JPG');


var writeStream = fs.createWriteStream('./IMG_SCALED.JPG');
gm(inStream).size({bufferStream: true}, function(err, size){
    var heightRatio = 1;
    var widthRatio = 1;
    var totalRatio = 1;
    if (size.width > maxWidth){
        widthRatio = maxWidth / size.width;
    }
    if (size.height > maxHeight){
        heightRatio = maxHeight / size.height;
    }
    totalRatio = heightRatio < widthRatio ? heightRatio : widthRatio;
    console.log(heightRatio);
    console.log(widthRatio);
    if (totalRatio !== 1){
        this.resize(size.width * totalRatio, size.height * totalRatio);
        this.stream(function(err, outStream, errStream){
            if (err){ console.log('error'); }
            outStream.on('data', function (data){ writeStream.write(data) });
            outStream.on('end', function(data) {
                console.log('Finished');
                writeStream.end()
            });
            outStream.on('error', function(err){
                console.log('Was an error');
                writeStream.end()
            });
                
        });;
    }
});


/*'./IMG_SCALED.JPG', function(err){
            if (err) console.log(err);
            else console.log("Finished");
        }*/