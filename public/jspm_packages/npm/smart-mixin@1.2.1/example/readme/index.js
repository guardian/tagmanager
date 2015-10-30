/* */ 
var mixins = require('../../../smart-mixin@1.2.1');
var mixIntoGameObject = mixins({
  render: mixins.ONCE,
  onClick: mixins.MANY,
  getState: mixins.MANY_MERGED,
  getSomething: mixins.MANY_MERGED_LOOSE,
  countChickens: mixins.REDUCE_LEFT,
  countDucks: mixins.REDUCE_RIGHT,
  onKeyPress: function(left, right, key) {
    left = left || function() {};
    right = right || function() {};
    return function(args, thrower) {
      var event = args[0];
      if (!event)
        thrower(TypeError(key + ' called without an event object'));
      var ret = left.apply(this, args);
      if (event && !event.immediatePropagationIsStopped) {
        var ret2 = right.apply(this, args);
      }
      return ret || ret2;
    };
  }
}, {
  unknownFunction: mixins.ONCE,
  nonFunctionProperty: "INTERNAL"
});
var mixin = {getState(foo) {
    return {bar: foo + 1};
  }};
class Duck {
  render() {
    console.log(this.getState(5));
  }
  getState(foo) {
    return {baz: foo - 1};
  }
}
mixIntoGameObject(Duck.prototype, mixin);
new Duck().render();
