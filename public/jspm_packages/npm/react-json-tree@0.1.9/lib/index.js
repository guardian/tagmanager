/* */ 
'use strict';
var _inherits = require('babel-runtime/helpers/inherits')['default'];
var _createClass = require('babel-runtime/helpers/create-class')['default'];
var _classCallCheck = require('babel-runtime/helpers/class-call-check')['default'];
var _extends = require('babel-runtime/helpers/extends')['default'];
var _interopRequireDefault = require('babel-runtime/helpers/interop-require-default')['default'];
exports.__esModule = true;
var _react = require('react');
var _react2 = _interopRequireDefault(_react);
var _grabNode = require('./grab-node');
var _grabNode2 = _interopRequireDefault(_grabNode);
var _themesSolarized = require('./themes/solarized');
var _themesSolarized2 = _interopRequireDefault(_themesSolarized);
var styles = {tree: {
    border: 0,
    padding: 0,
    marginTop: 8,
    marginBottom: 8,
    marginLeft: 2,
    marginRight: 0,
    fontSize: '0.90em',
    listStyle: 'none',
    MozUserSelect: 'none',
    WebkitUserSelect: 'none'
  }};
var JSONTree = (function(_React$Component) {
  _inherits(JSONTree, _React$Component);
  _createClass(JSONTree, null, [{
    key: 'propTypes',
    value: {data: _react2['default'].PropTypes.oneOfType([_react2['default'].PropTypes.array, _react2['default'].PropTypes.object]).isRequired},
    enumerable: true
  }, {
    key: 'defaultProps',
    value: {theme: _themesSolarized2['default']},
    enumerable: true
  }]);
  function JSONTree(props) {
    _classCallCheck(this, JSONTree);
    _React$Component.call(this, props);
  }
  JSONTree.prototype.render = function render() {
    var keyName = this.props.keyName || 'root';
    var rootNode = _grabNode2['default'](keyName, this.props.data, this.props.previousData, this.props.theme, true);
    return _react2['default'].createElement('ul', {style: _extends({}, styles.tree, this.props.style)}, rootNode);
  };
  return JSONTree;
})(_react2['default'].Component);
exports['default'] = JSONTree;
module.exports = exports['default'];
