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
var _react = require('react');
var _react2 = _interopRequireDefault(_react);
var _reactJsonTree = require('react-json-tree');
var _reactJsonTree2 = _interopRequireDefault(_reactJsonTree);
var _LogMonitorEntryAction = require('./LogMonitorEntryAction');
var _LogMonitorEntryAction2 = _interopRequireDefault(_LogMonitorEntryAction);
var styles = {
  entry: {
    display: 'block',
    WebkitUserSelect: 'none'
  },
  tree: {paddingLeft: 0}
};
var LogMonitorEntry = (function(_Component) {
  _inherits(LogMonitorEntry, _Component);
  function LogMonitorEntry() {
    _classCallCheck(this, LogMonitorEntry);
    _Component.apply(this, arguments);
  }
  LogMonitorEntry.prototype.printState = function printState(state, error) {
    var errorText = error;
    if (!errorText) {
      try {
        return _react2['default'].createElement(_reactJsonTree2['default'], {
          theme: this.props.theme,
          keyName: 'state',
          data: this.props.select(state),
          previousData: this.props.select(this.props.previousState),
          style: styles.tree
        });
      } catch (err) {
        errorText = 'Error selecting state.';
      }
    }
    return _react2['default'].createElement('div', {style: {
        color: this.props.theme.base08,
        paddingTop: 20,
        paddingLeft: 30,
        paddingRight: 30,
        paddingBottom: 35
      }}, errorText);
  };
  LogMonitorEntry.prototype.handleActionClick = function handleActionClick() {
    var _props = this.props;
    var index = _props.index;
    var onActionClick = _props.onActionClick;
    if (index > 0) {
      onActionClick(index);
    }
  };
  LogMonitorEntry.prototype.render = function render() {
    var _props2 = this.props;
    var index = _props2.index;
    var error = _props2.error;
    var action = _props2.action;
    var state = _props2.state;
    var collapsed = _props2.collapsed;
    var styleEntry = {
      opacity: collapsed ? 0.5 : 1,
      cursor: index > 0 ? 'pointer' : 'default'
    };
    return _react2['default'].createElement('div', {style: {textDecoration: collapsed ? 'line-through' : 'none'}}, _react2['default'].createElement(_LogMonitorEntryAction2['default'], {
      theme: this.props.theme,
      collapsed: collapsed,
      action: action,
      onClick: this.handleActionClick.bind(this),
      style: _extends({}, styles.entry, styleEntry)
    }), !collapsed && _react2['default'].createElement('div', null, this.printState(state, error)));
  };
  _createClass(LogMonitorEntry, null, [{
    key: 'propTypes',
    value: {
      index: _react.PropTypes.number.isRequired,
      state: _react.PropTypes.object.isRequired,
      action: _react.PropTypes.object.isRequired,
      select: _react.PropTypes.func.isRequired,
      error: _react.PropTypes.string,
      onActionClick: _react.PropTypes.func.isRequired,
      collapsed: _react.PropTypes.bool
    },
    enumerable: true
  }]);
  return LogMonitorEntry;
})(_react.Component);
exports['default'] = LogMonitorEntry;
module.exports = exports['default'];
