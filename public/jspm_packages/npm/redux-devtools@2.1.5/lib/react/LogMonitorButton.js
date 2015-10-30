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
var _utilsBrighten = require('../utils/brighten');
var _utilsBrighten2 = _interopRequireDefault(_utilsBrighten);
var styles = {base: {
    cursor: 'pointer',
    fontWeight: 'bold',
    borderRadius: 3,
    padding: 4,
    marginLeft: 3,
    marginRight: 3,
    marginTop: 5,
    marginBottom: 5,
    flexGrow: 1,
    display: 'inline-block',
    fontSize: '0.8em',
    color: 'white',
    textDecoration: 'none'
  }};
var LogMonitorButton = (function(_React$Component) {
  _inherits(LogMonitorButton, _React$Component);
  function LogMonitorButton(props) {
    _classCallCheck(this, LogMonitorButton);
    _React$Component.call(this, props);
    this.state = {
      hovered: false,
      active: false
    };
  }
  LogMonitorButton.prototype.handleMouseEnter = function handleMouseEnter() {
    this.setState({hovered: true});
  };
  LogMonitorButton.prototype.handleMouseLeave = function handleMouseLeave() {
    this.setState({hovered: false});
  };
  LogMonitorButton.prototype.handleMouseDown = function handleMouseDown() {
    this.setState({active: true});
  };
  LogMonitorButton.prototype.handleMouseUp = function handleMouseUp() {
    this.setState({active: false});
  };
  LogMonitorButton.prototype.onClick = function onClick() {
    if (!this.props.enabled) {
      return;
    }
    if (this.props.onClick) {
      this.props.onClick();
    }
  };
  LogMonitorButton.prototype.render = function render() {
    var style = _extends({}, styles.base, {backgroundColor: this.props.theme.base02});
    if (this.props.enabled && this.state.hovered) {
      style = _extends({}, style, {backgroundColor: _utilsBrighten2['default'](this.props.theme.base02, 0.2)});
    }
    if (!this.props.enabled) {
      style = _extends({}, style, {
        opacity: 0.2,
        cursor: 'text',
        backgroundColor: 'transparent'
      });
    }
    return _react2['default'].createElement('a', {
      onMouseEnter: this.handleMouseEnter.bind(this),
      onMouseLeave: this.handleMouseLeave.bind(this),
      onMouseDown: this.handleMouseDown.bind(this),
      onMouseUp: this.handleMouseUp.bind(this),
      style: style,
      onClick: this.onClick.bind(this)
    }, this.props.children);
  };
  return LogMonitorButton;
})(_react2['default'].Component);
exports['default'] = LogMonitorButton;
module.exports = exports['default'];
