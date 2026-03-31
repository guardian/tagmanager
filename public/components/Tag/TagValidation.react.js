import React from 'react';
import { TransitionGroup, CSSTransition } from 'react-transition-group';

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
        <TransitionGroup>
          {this.props.validations.map((validation) => (
            <CSSTransition key={validation.message} classNames="validation-transition" timeout={{ enter: 0, exit: 500 }}>
              <div className="tag-validation__result">
                <i className="tag-validation__result__dot"/><span className="tag-validation__result__message">{validation.message}</span>
              </div>
            </CSSTransition>
          ))}
        </TransitionGroup>
      </div>
    );
  }
}
