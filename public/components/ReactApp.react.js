import React, { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Header from './Header.react';

function onRouterUpdate() {
  const domainMatch = /^.*\.(?<environment>local|code)\.dev-gutools\.co\.uk$|^.*\.gutools\.co\.uk$/
      .exec(location.hostname);
  if (domainMatch) {
    const stage = (domainMatch.groups?.environment || "PROD");
    const telemetryUrl = stage === "PROD" ? "user-telemetry.gutools.co.uk" : `user-telemetry.${stage}.dev-gutools.co.uk`;
    const image = new Image();
    image.src = `https://${telemetryUrl}/guardian-tool-accessed?app=tag-manager&stage=${stage.toUpperCase()}&path=${window.location.pathname}`;
  }
}

function RouteChangeTracker() {
  const location = useLocation();
  useEffect(() => { onRouterUpdate(); }, [location]);
  return null;
}

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
                <RouteChangeTracker />
                {this.renderErrorBar()}
                <Header />
                <div className="editor">
                    <Outlet />
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
