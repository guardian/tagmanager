/* */ 
'use strict';
var _inherits = require('babel-runtime/helpers/inherits')['default'];
var _classCallCheck = require('babel-runtime/helpers/class-call-check')['default'];
var _extends = require('babel-runtime/helpers/extends')['default'];
var _Object$keys = require('babel-runtime/core-js/object/keys')['default'];
var _interopRequireDefault = require('babel-runtime/helpers/interop-require-default')['default'];
exports.__esModule = true;
var _react = require('react');
var _react2 = _interopRequireDefault(_react);
var _reactMixin = require('react-mixin');
var _reactMixin2 = _interopRequireDefault(_reactMixin);
var _mixins = require('./mixins/index');
var _JSONArrow = require('./JSONArrow');
var _JSONArrow2 = _interopRequireDefault(_JSONArrow);
var _grabNode = require('./grab-node');
var _grabNode2 = _interopRequireDefault(_grabNode);
var styles = {
  base: {
    position: 'relative',
    paddingTop: 3,
    paddingBottom: 3,
    marginLeft: 14
  },
  label: {
    margin: 0,
    padding: 0,
    display: 'inline-block'
  },
  span: {cursor: 'default'},
  spanType: {
    marginLeft: 5,
    marginRight: 5
  }
};
var JSONObjectNode = (function(_React$Component) {
  _inherits(JSONObjectNode, _React$Component);
  function JSONObjectNode(props) {
    _classCallCheck(this, _JSONObjectNode);
    _React$Component.call(this, props);
    this.defaultProps = {
      data: [],
      initialExpanded: false
    };
    this.itemString = false;
    this.needsChildNodes = true;
    this.renderedChildren = [];
    this.state = {
      expanded: this.props.initialExpanded,
      createdChildNodes: false
    };
  }
  JSONObjectNode.prototype.getChildNodes = function getChildNodes() {
    if (this.state.expanded && this.needsChildNodes) {
      var obj = this.props.data;
      var childNodes = [];
      for (var k in obj) {
        if (obj.hasOwnProperty(k)) {
          var prevData = undefined;
          if (typeof this.props.previousData !== 'undefined' && this.props.previousData !== null) {
            prevData = this.props.previousData[k];
          }
          var node = _grabNode2['default'](k, obj[k], prevData, this.props.theme);
          if (node !== false) {
            childNodes.push(node);
          }
        }
      }
      this.needsChildNodes = false;
      this.renderedChildren = childNodes;
    }
    return this.renderedChildren;
  };
  JSONObjectNode.prototype.getItemString = function getItemString() {
    if (!this.itemString) {
      var len = _Object$keys(this.props.data).length;
      this.itemString = len + ' key' + (len !== 1 ? 's' : '');
    }
    return this.itemString;
  };
  JSONObjectNode.prototype.render = function render() {
    var childListStyle = {
      padding: 0,
      margin: 0,
      listStyle: 'none',
      display: this.state.expanded ? 'block' : 'none'
    };
    var containerStyle = undefined;
    var spanStyle = _extends({}, styles.span, {color: this.props.theme.base0B});
    containerStyle = _extends({}, styles.base);
    if (this.state.expanded) {
      spanStyle = _extends({}, spanStyle, {color: this.props.theme.base03});
    }
    return _react2['default'].createElement('li', {style: containerStyle}, _react2['default'].createElement(_JSONArrow2['default'], {
      theme: this.props.theme,
      open: this.state.expanded,
      onClick: this.handleClick.bind(this)
    }), _react2['default'].createElement('label', {
      style: _extends({}, styles.label, {color: this.props.theme.base0D}),
      onClick: this.handleClick.bind(this)
    }, this.props.keyName, ':'), _react2['default'].createElement('span', {
      style: spanStyle,
      onClick: this.handleClick.bind(this)
    }, _react2['default'].createElement('span', {style: styles.spanType}, '{}'), this.getItemString()), _react2['default'].createElement('ul', {style: childListStyle}, this.getChildNodes()));
  };
  var _JSONObjectNode = JSONObjectNode;
  JSONObjectNode = _reactMixin2['default'].decorate(_mixins.ExpandedStateHandlerMixin)(JSONObjectNode) || JSONObjectNode;
  return JSONObjectNode;
})(_react2['default'].Component);
exports['default'] = JSONObjectNode;
module.exports = exports['default'];
