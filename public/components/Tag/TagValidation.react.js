import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';

export default class TagValidation extends React.Component {

  constructor(props) {
    super(props);
  }

  renderValidationResult(validation) {
    return (
      <div className="tag-validation__result" key={validation.message}>
        <i className="tag-validation__result__dot"/><span className="tag-validation__result__message">{validation.message}</span>
      </div>
    );
  }

  render () {
    if (!this.props.validations.length) {
      return false;
    }

    return (
      <div className="tag-validation">
        <div className="tag-validation__header">Validation</div>
        <ReactCSSTransitionGroup transitionName="validation-transition" transitionEnter={false} transitionLeaveTimeout={500}>
          {this.props.validations.map(this.renderValidationResult, this)}
        </ReactCSSTransitionGroup>
      </div>
    );
  }
}
