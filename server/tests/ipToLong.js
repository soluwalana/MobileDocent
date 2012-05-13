var addr = '128.12.189.193';
addr = addr.split('.');

var ipNumber = 16777216 * parseInt(addr[0]) + 65536 * parseInt(addr[1]) +
    256 * parseInt(addr[2]) + parseInt(addr[3]);
console.log(ipNumber);