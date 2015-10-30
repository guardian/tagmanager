import React from 'react';

export default class SaveButton extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {

      return (
        <div className="save">
          <div className="save__button" onClick={this.props.onSaveClick}>
            SAVE
          </div>
          <div className="save__button--reset" onClick={this.props.onResetClick}>
            RESET
          </div>
        </div>
      );
    }
}
