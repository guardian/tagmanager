import React from 'react';
import Header from './Header.react';

class ReactApp extends React.Component {

    constructor(props) {
        super(props);

        this.renderErrorBar = this.renderErrorBar.bind(this);
    }

    renderErrorBar() {
      if (!this.props.error) {
        return false;
      }

      return (
        <div className="error-bar">
          {this.props.error || 'An error has occured, please refresh your browser. If this problem persists please contact Central Production'}
        </div>
      );
    }

    render () {
        return (
            <div className="wrapper">
                {this.renderErrorBar()}
                <Header />
                <div className="editor">
                    <h1>React is running.</h1>
                    {this.props.children}
                </div>
            </div>
        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';

function mapStateToProps(state) {
  return { error: state.error };
}

export default connect(mapStateToProps)(ReactApp);
