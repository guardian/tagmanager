import React from 'react';
import { TransitionGroup, CSSTransition } from 'react-transition-group';
import {saveState} from "../../reducers/rootReducer"
import ProgressSpinner from "../utils/ProgressSpinner.react"

export default class SaveButton extends React.Component {

    constructor(props) {
        super(props);
    }

    renderSaveStateIndicator() {
      if (this.props.saveState == saveState.inprogress) {
          return (
            <div className="save__button--indicator">
              <ProgressSpinner />
            </div>
          );
      }

      return false;
    }

    renderButtons() {
      return (
        <div className="save">
          <div className="save__button clickable-icon" onClick={this.props.onSaveClick}>
            <i className="i-tick-green"/>Save
          </div>
          <div className="save__button--reset clickable-icon" onClick={this.props.onResetClick}>
            <i className="i-cross-red"/>Cancel
          </div>
          {this.renderSaveStateIndicator()}
        </div>
      );
    }

    render () {
      return (
      <TransitionGroup>
        {!this.props.isHidden &&
          <CSSTransition key="save-button" classNames="save-transition" timeout={{ enter: 500, exit: 500 }}>
            {this.renderButtons()}
          </CSSTransition>
        }
      </TransitionGroup>
      );
    }
}
