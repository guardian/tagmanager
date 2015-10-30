/* */ 
var equal = require("../../deep-equal@1.0.1");
console.dir([equal({
  a: [2, 3],
  b: [4]
}, {
  a: [2, 3],
  b: [4]
}), equal({
  x: 5,
  y: [6]
}, {
  x: 5,
  y: 6
})]);
