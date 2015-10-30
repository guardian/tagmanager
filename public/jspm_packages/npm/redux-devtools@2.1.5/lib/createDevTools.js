/* */ 
'use strict';
exports.__esModule = true;
var _extends = Object.assign || function(target) {
  for (var i = 1; i < arguments.length; i++) {
    var source = arguments[i];
    for (var key in source) {
      if (Object.prototype.hasOwnProperty.call(source, key)) {
        target[key] = source[key];
      }
    }
  }
  return target;
};
var _createClass = (function() {
  function defineProperties(target, props) {
    for (var i = 0; i < props.length; i++) {
      var descriptor = props[i];
      descriptor.enumerable = descriptor.enumerable || false;
      descriptor.configurable = true;
      if ('value' in descriptor)
        descriptor.writable = true;
      Object.defineProperty(target, descriptor.key, descriptor);
    }
  }
  return function(Constructor, protoProps, staticProps) {
    if (protoProps)
      defineProperties(Constructor.prototype, protoProps);
    if (staticProps)
      defineProperties(Constructor, staticProps);
    return Constructor;
  };
})();
exports['default'] = createDevTools;
function _interopRequireDefault(obj) {
  return obj && obj.__esModule ? obj : {'default': obj};
}
function _classCallCheck(instance, Constructor) {
  if (!(instance instanceof Constructor)) {
    throw new TypeError('Cannot call a class as a function');
  }
}
function _inherits(subClass, superClass) {
  if (typeof superClass !== 'function' && superClass !== null) {
    throw new TypeError('Super expression must either be null or a function, not ' + typeof superClass);
  }
  subClass.prototype = Object.create(superClass && superClass.prototype, {constructor: {
      value: subClass,
      enumerable: false,
      writable: true,
      configurable: true
    }});
  if (superClass)
    Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
}
var _reactReduxLibComponentsCreateAll = require('react-redux/lib/components/createAll');
var _reactReduxLibComponentsCreateAll2 = _interopRequireDefault(_reactReduxLibComponentsCreateAll);
var _devTools = require('./devTools');
function createDevTools(React) {
  var PropTypes = React.PropTypes;
  var Component = React.Component;
  var _createAll = _reactReduxLibComponentsCreateAll2['default'](React);
  var connect = _createAll.connect;
  var DevTools = (function(_Component) {
    _inherits(DevTools, _Component);
    function DevTools() {
      _classCallCheck(this, _DevTools);
      _Component.apply(this, arguments);
    }
    DevTools.prototype.render = function render() {
      var Monitor = this.props.monitor;
      return React.createElement(Monitor, this.props);
    };
    var _DevTools = DevTools;
    DevTools = connect(function(state) {
      return state;
    }, _devTools.ActionCreators)(DevTools) || DevTools;
    return DevTools;
  })(Component);
  return (function(_Component2) {
    _inherits(DevToolsWrapper, _Component2);
    _createClass(DevToolsWrapper, null, [{
      key: 'propTypes',
      value: {
        monitor: PropTypes.func.isRequired,
        store: PropTypes.shape({devToolsStore: PropTypes.shape({dispatch: PropTypes.func.isRequired}).isRequired}).isRequired
      },
      enumerable: true
    }]);
    function DevToolsWrapper(props, context) {
      _classCallCheck(this, DevToolsWrapper);
      if (props.store && !props.store.devToolsStore) {
        console.error('Could not find the devTools store inside your store. ' + 'Have you applied devTools() store enhancer?');
      }
      _Component2.call(this, props, context);
    }
    DevToolsWrapper.prototype.render = function render() {
      return React.createElement(DevTools, _extends({}, this.props, {store: this.props.store.devToolsStore}));
    };
    return DevToolsWrapper;
  })(Component);
}
module.exports = exports['default'];
