import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';

export default class SaveButton extends React.Component {

    constructor(props) {
        super(props);
    }

    renderButtons() {
      if (this.props.isHidden) {
        return false;
      }

      return (
        <div className="save">
          <div className="save__button" onClick={this.props.onSaveClick}>
            <i className="i-tick-green"/>Save
          </div>
          <div className="save__button--reset" onClick={this.props.onResetClick}>
            <i className="i-cross-red"/>Reset
          </div>
        </div>
      );
    }

    render () {
      return (
        <ReactCSSTransitionGroup transitionName="save-transition" transitionEnterTimeout={500} transitionLeaveTimeout={500}>
          {this.renderButtons()}
        </ReactCSSTransitionGroup>
      );
    }
}
