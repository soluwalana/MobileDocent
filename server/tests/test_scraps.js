var _ = require('../lib/underscore.js')._;

console.log(_.isEmpty(null));
console.log(_.isEmpty(undefined));
console.log(_.isEmpty(''));
console.log(_.isEmpty('String'));
console.log(_.isEmpty(2.2));
console.log(_.isEmpty(-122.2));
console.log(_.isEmpty(true));
console.log(_.isEmpty(false));