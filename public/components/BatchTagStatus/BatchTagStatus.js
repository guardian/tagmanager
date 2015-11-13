import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';

export default class BatchTagStatus extends React.Component {

    constructor(props) {
        super(props);
    }

    renderButtons() {
      if (this.props.selectedContent.length === 0) {
        return false;
      }

      return (
        <div className="batch-status__container">
          <div className="batch-status">
            <div className="batch-status__info">
              {this.props.selectedContent.length} Selected
            </div>
            <div className="batch-status__button" onClick={this.props.onSaveClick}>
              <i className="i-tick-green"/>Apply
            </div>
          </div>
        </div>
      );
    }

    render () {
      return (
        <ReactCSSTransitionGroup transitionName="batch-status-transition" transitionEnterTimeout={500} transitionLeaveTimeout={500}>
          {this.renderButtons()}
        </ReactCSSTransitionGroup>
      );
    }
}
