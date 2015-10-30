/* */ 
'use strict';

exports.__esModule = true;

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

exports['default'] = persistState;

function persistState(sessionId) {
  var stateDeserializer = arguments.length <= 1 || arguments[1] === undefined ? null : arguments[1];
  var actionDeserializer = arguments.length <= 2 || arguments[2] === undefined ? null : arguments[2];

  if (!sessionId) {
    return function (next) {
      return function () {
        return next.apply(undefined, arguments);
      };
    };
  }

  function deserializeState(fullState) {
    return _extends({}, fullState, {
      committedState: stateDeserializer(fullState.committedState),
      computedStates: fullState.computedStates.map(function (computedState) {
        return _extends({}, computedState, {
          state: stateDeserializer(computedState.state)
        });
      })
    });
  }

  function deserializeActions(fullState) {
    return _extends({}, fullState, {
      stagedActions: fullState.stagedActions.map(function (action) {
        return actionDeserializer(action);
      })
    });
  }

  function deserialize(fullState) {
    if (!fullState) {
      return fullState;
    }
    var deserializedState = fullState;
    if (typeof stateDeserializer === 'function') {
      deserializedState = deserializeState(deserializedState);
    }
    if (typeof actionDeserializer === 'function') {
      deserializedState = deserializeActions(deserializedState);
    }
    return deserializedState;
  }

  return function (next) {
    return function (reducer, initialState) {
      var key = 'redux-dev-session-' + sessionId;

      var finalInitialState = undefined;
      try {
        finalInitialState = deserialize(JSON.parse(localStorage.getItem(key))) || initialState;
        next(reducer, initialState);
      } catch (e) {
        console.warn('Could not read debug session from localStorage:', e);
        try {
          localStorage.removeItem(key);
        } finally {
          finalInitialState = undefined;
        }
      }

      var store = next(reducer, finalInitialState);

      return _extends({}, store, {
        dispatch: function dispatch(action) {
          store.dispatch(action);

          try {
            localStorage.setItem(key, JSON.stringify(store.getState()));
          } catch (e) {
            console.warn('Could not write debug session to localStorage:', e);
          }

          return action;
        }
      });
    };
  };
}

module.exports = exports['default'];