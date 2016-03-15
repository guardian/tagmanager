import React from 'react';

export default class ConfirmButton extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      clicked: false
    };

    this.resetClick = this.resetClick.bind(this);
  }

  onFirstClick() {
    this.setState({
      clicked: true
    });

    setTimeout(this.resetClick, 2000);
  }

  onSecondClick(e) {
    this.props.onClick(e);

    clearTimeout(this.resetClick);
  }

  resetClick() {
    this.setState({
      clicked: false
    });
  }

  render () {

    if (this.state.clicked) {
      return (
        <button className={this.props.className} disabled={this.props.disabled} onClick={this.onSecondClick.bind(this)}>Confirm?</button>
      );
    }

    return (
      <button className={this.props.className} disabled={this.props.disabled} onClick={this.onFirstClick.bind(this)}>{this.props.buttonText}</button>
    );

  }

}
