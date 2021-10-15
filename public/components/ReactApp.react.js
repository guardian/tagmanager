import React from 'react';
import Header from './Header.react';

class ReactApp extends React.Component {

    constructor(props) {
        super(props);

        this.renderErrorBar = this.renderErrorBar.bind(this);
    }

    clearError() {
      this.props.uiActions.clearError();
    }

    renderErrorBar() {
      if (!this.props.error) {
        return false;
      }

      return (
        <div className="error-bar">
          {this.props.error || 'An error has occured, please refresh your browser. If this problem persists please contact Central Production'}
          <span className="error-bar__dismiss clickable-icon" onClick={this.clearError.bind(this)}>
            <i className="i-cross-white"></i>
          </span>
        </div>
      );
    }

    render () {
        return (
            <div className="wrapper">
                {this.renderErrorBar()}
                <Header />
                <div className="editor">
                    {this.props.children}
                </div>
            </div>
        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as clearError from '../actions/UIActions/clearError';

function mapStateToProps(state) {
  return {
      error: state.error
  };
}

function mapDispatchToProps(dispatch) {
  return {
    uiActions: bindActionCreators(Object.assign({}, clearError), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(ReactApp);
