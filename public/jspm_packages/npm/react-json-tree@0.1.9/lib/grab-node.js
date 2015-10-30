/* */ 
'use strict';
var _interopRequireDefault = require('babel-runtime/helpers/interop-require-default')['default'];
exports.__esModule = true;
var _react = require('react');
var _react2 = _interopRequireDefault(_react);
var _objType = require('./obj-type');
var _objType2 = _interopRequireDefault(_objType);
var _JSONObjectNode = require('./JSONObjectNode');
var _JSONObjectNode2 = _interopRequireDefault(_JSONObjectNode);
var _JSONArrayNode = require('./JSONArrayNode');
var _JSONArrayNode2 = _interopRequireDefault(_JSONArrayNode);
var _JSONIterableNode = require('./JSONIterableNode');
var _JSONIterableNode2 = _interopRequireDefault(_JSONIterableNode);
var _JSONStringNode = require('./JSONStringNode');
var _JSONStringNode2 = _interopRequireDefault(_JSONStringNode);
var _JSONNumberNode = require('./JSONNumberNode');
var _JSONNumberNode2 = _interopRequireDefault(_JSONNumberNode);
var _JSONBooleanNode = require('./JSONBooleanNode');
var _JSONBooleanNode2 = _interopRequireDefault(_JSONBooleanNode);
var _JSONNullNode = require('./JSONNullNode');
var _JSONNullNode2 = _interopRequireDefault(_JSONNullNode);
var _JSONDateNode = require('./JSONDateNode');
var _JSONDateNode2 = _interopRequireDefault(_JSONDateNode);
exports['default'] = function(key, value, prevValue, theme) {
  var initialExpanded = arguments.length <= 4 || arguments[4] === undefined ? false : arguments[4];
  var nodeType = _objType2['default'](value);
  if (nodeType === 'Object') {
    return _react2['default'].createElement(_JSONObjectNode2['default'], {
      data: value,
      previousData: prevValue,
      theme: theme,
      initialExpanded: initialExpanded,
      keyName: key,
      key: key
    });
  } else if (nodeType === 'Array') {
    return _react2['default'].createElement(_JSONArrayNode2['default'], {
      data: value,
      previousData: prevValue,
      theme: theme,
      initialExpanded: initialExpanded,
      keyName: key,
      key: key
    });
  } else if (nodeType === 'Iterable') {
    return _react2['default'].createElement(_JSONIterableNode2['default'], {
      data: value,
      previousData: prevValue,
      theme: theme,
      initialExpanded: initialExpanded,
      keyName: key,
      key: key
    });
  } else if (nodeType === 'String') {
    return _react2['default'].createElement(_JSONStringNode2['default'], {
      keyName: key,
      previousValue: prevValue,
      theme: theme,
      value: value,
      key: key
    });
  } else if (nodeType === 'Number') {
    return _react2['default'].createElement(_JSONNumberNode2['default'], {
      keyName: key,
      previousValue: prevValue,
      theme: theme,
      value: value,
      key: key
    });
  } else if (nodeType === 'Boolean') {
    return _react2['default'].createElement(_JSONBooleanNode2['default'], {
      keyName: key,
      previousValue: prevValue,
      theme: theme,
      value: value,
      key: key
    });
  } else if (nodeType === 'Date') {
    return _react2['default'].createElement(_JSONDateNode2['default'], {
      keyName: key,
      previousValue: prevValue,
      theme: theme,
      value: value,
      key: key
    });
  } else if (nodeType === 'Null') {
    return _react2['default'].createElement(_JSONNullNode2['default'], {
      keyName: key,
      previousValue: prevValue,
      theme: theme,
      value: value,
      key: key
    });
  }
  return false;
};
module.exports = exports['default'];
