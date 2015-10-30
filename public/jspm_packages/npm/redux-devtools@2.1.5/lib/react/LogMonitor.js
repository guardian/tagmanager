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
function _interopRequireWildcard(obj) {
  if (obj && obj.__esModule) {
    return obj;
  } else {
    var newObj = {};
    if (obj != null) {
      for (var key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key))
          newObj[key] = obj[key];
      }
    }
    newObj['default'] = obj;
    return newObj;
  }
}
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
var _LogMonitorEntry = require('./LogMonitorEntry');
var _LogMonitorEntry2 = _interopRequireDefault(_LogMonitorEntry);
var _LogMonitorButton = require('./LogMonitorButton');
var _LogMonitorButton2 = _interopRequireDefault(_LogMonitorButton);
var _themes = require('./themes/index');
var themes = _interopRequireWildcard(_themes);
var styles = {
  container: {
    fontFamily: 'monaco, Consolas, Lucida Console, monospace',
    position: 'relative',
    overflowY: 'hidden',
    width: '100%',
    height: '100%',
    minWidth: 300
  },
  buttonBar: {
    textAlign: 'center',
    borderBottomWidth: 1,
    borderBottomStyle: 'solid',
    borderColor: 'transparent',
    zIndex: 1,
    display: 'flex',
    flexDirection: 'row'
  },
  elements: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 38,
    bottom: 0,
    overflowX: 'hidden',
    overflowY: 'auto'
  }
};
var LogMonitor = (function(_Component) {
  _inherits(LogMonitor, _Component);
  function LogMonitor(props) {
    _classCallCheck(this, LogMonitor);
    _Component.call(this, props);
    if (typeof window !== 'undefined') {
      window.addEventListener('keydown', this.handleKeyPress.bind(this));
    }
  }
  LogMonitor.prototype.componentWillReceiveProps = function componentWillReceiveProps(nextProps) {
    var node = _react.findDOMNode(this.refs.elements);
    if (!node) {
      this.scrollDown = true;
    } else if (this.props.stagedActions.length < nextProps.stagedActions.length) {
      var scrollTop = node.scrollTop;
      var offsetHeight = node.offsetHeight;
      var scrollHeight = node.scrollHeight;
      this.scrollDown = Math.abs(scrollHeight - (scrollTop + offsetHeight)) < 20;
    } else {
      this.scrollDown = false;
    }
  };
  LogMonitor.prototype.componentDidUpdate = function componentDidUpdate() {
    var node = _react.findDOMNode(this.refs.elements);
    if (!node) {
      return;
    }
    if (this.scrollDown) {
      var offsetHeight = node.offsetHeight;
      var scrollHeight = node.scrollHeight;
      node.scrollTop = scrollHeight - offsetHeight;
      this.scrollDown = false;
    }
  };
  LogMonitor.prototype.componentWillMount = function componentWillMount() {
    var visibleOnLoad = this.props.visibleOnLoad;
    var monitorState = this.props.monitorState;
    this.props.setMonitorState(_extends({}, monitorState, {isVisible: visibleOnLoad}));
  };
  LogMonitor.prototype.handleRollback = function handleRollback() {
    this.props.rollback();
  };
  LogMonitor.prototype.handleSweep = function handleSweep() {
    this.props.sweep();
  };
  LogMonitor.prototype.handleCommit = function handleCommit() {
    this.props.commit();
  };
  LogMonitor.prototype.handleToggleAction = function handleToggleAction(index) {
    this.props.toggleAction(index);
  };
  LogMonitor.prototype.handleReset = function handleReset() {
    this.props.reset();
  };
  LogMonitor.prototype.handleKeyPress = function handleKeyPress(event) {
    var monitorState = this.props.monitorState;
    if (event.ctrlKey && event.keyCode === 72) {
      event.preventDefault();
      this.props.setMonitorState(_extends({}, monitorState, {isVisible: !monitorState.isVisible}));
    }
  };
  LogMonitor.prototype.render = function render() {
    var elements = [];
    var _props = this.props;
    var monitorState = _props.monitorState;
    var skippedActions = _props.skippedActions;
    var stagedActions = _props.stagedActions;
    var computedStates = _props.computedStates;
    var select = _props.select;
    var theme = undefined;
    if (typeof this.props.theme === 'string') {
      if (typeof themes[this.props.theme] !== 'undefined') {
        theme = themes[this.props.theme];
      } else {
        console.warn('DevTools theme ' + this.props.theme + ' not found, defaulting to nicinabox');
        theme = themes.nicinabox;
      }
    } else {
      theme = this.props.theme;
    }
    if (!monitorState.isVisible) {
      return null;
    }
    for (var i = 0; i < stagedActions.length; i++) {
      var action = stagedActions[i];
      var _computedStates$i = computedStates[i];
      var state = _computedStates$i.state;
      var error = _computedStates$i.error;
      var previousState = undefined;
      if (i > 0) {
        previousState = computedStates[i - 1].state;
      }
      elements.push(_react2['default'].createElement(_LogMonitorEntry2['default'], {
        key: i,
        index: i,
        theme: theme,
        select: select,
        action: action,
        state: state,
        previousState: previousState,
        collapsed: skippedActions[i],
        error: error,
        onActionClick: this.handleToggleAction.bind(this)
      }));
    }
    return _react2['default'].createElement('div', {style: _extends({}, styles.container, {backgroundColor: theme.base00})}, _react2['default'].createElement('div', {style: _extends({}, styles.buttonBar, {borderColor: theme.base02})}, _react2['default'].createElement(_LogMonitorButton2['default'], {
      theme: theme,
      onClick: this.handleReset.bind(this)
    }, 'Reset'), _react2['default'].createElement(_LogMonitorButton2['default'], {
      theme: theme,
      onClick: this.handleRollback.bind(this),
      enabled: computedStates.length
    }, 'Revert'), _react2['default'].createElement(_LogMonitorButton2['default'], {
      theme: theme,
      onClick: this.handleSweep.bind(this),
      enabled: Object.keys(skippedActions).some(function(key) {
        return skippedActions[key];
      })
    }, 'Sweep'), _react2['default'].createElement(_LogMonitorButton2['default'], {
      theme: theme,
      onClick: this.handleCommit.bind(this),
      enabled: computedStates.length > 1
    }, 'Commit')), _react2['default'].createElement('div', {
      style: styles.elements,
      ref: 'elements'
    }, elements));
  };
  _createClass(LogMonitor, null, [{
    key: 'propTypes',
    value: {
      computedStates: _react.PropTypes.array.isRequired,
      currentStateIndex: _react.PropTypes.number.isRequired,
      monitorState: _react.PropTypes.object.isRequired,
      stagedActions: _react.PropTypes.array.isRequired,
      skippedActions: _react.PropTypes.object.isRequired,
      reset: _react.PropTypes.func.isRequired,
      commit: _react.PropTypes.func.isRequired,
      rollback: _react.PropTypes.func.isRequired,
      sweep: _react.PropTypes.func.isRequired,
      toggleAction: _react.PropTypes.func.isRequired,
      jumpToState: _react.PropTypes.func.isRequired,
      setMonitorState: _react.PropTypes.func.isRequired,
      select: _react.PropTypes.func.isRequired,
      visibleOnLoad: _react.PropTypes.bool,
      theme: _react.PropTypes.oneOfType([_react.PropTypes.object, _react.PropTypes.string])
    },
    enumerable: true
  }, {
    key: 'defaultProps',
    value: {
      select: function select(state) {
        return state;
      },
      monitorState: {isVisible: true},
      theme: 'nicinabox',
      visibleOnLoad: true
    },
    enumerable: true
  }]);
  return LogMonitor;
})(_react.Component);
exports['default'] = LogMonitor;
module.exports = exports['default'];
