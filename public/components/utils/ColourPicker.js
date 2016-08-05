import React from 'react';
import { ChromePicker } from 'react-color';

export default class ColourPicker extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      showColourPicker: false
    };
  }

  showPicker() {
    this.setState({
      showColourPicker: true
    });
  }

  closePicker() {
    this.setState({
      showColourPicker: false
    });
  }

  onColorSelect(colour) {
    this.props.onChange(colour.hex);
  }

  renderColourPicker() {
    if (!this.state.showColourPicker) {
      return false;
    }

    return (
      <div>
        <div className="colour-picker__popover" onClick={this.closePicker.bind(this)}></div>
        <div className="colour-picker__picker">
          <ChromePicker
            color={this.props.value}
            onChange={this.onColorSelect.bind(this)}
            />
        </div>
      </div>
    );
  }

  render() {
    return (
      <div className="colour-picker" style={{position: "relative"}} >
        <div onClick={this.showPicker.bind(this)}>
          <input
            disabled={true}
            type="text"
            className="tag-edit__input"
            value={this.props.value || "No Colour Selected"}
          />
          <div className="colour-picker__swatch" style={{backgroundColor: this.props.value || "#FFF"}}></div>
        </div>
        {this.renderColourPicker()}
      </div>
    );
  }
}
