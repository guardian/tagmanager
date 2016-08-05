import React from 'react';

export default class ColourPicker extends React.Component {

  isValidColour(hexColour) {
    const hexCodeRegex = /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/;

    return hexCodeRegex.test(hexColour);
  }

  onInputChange(e) {
    let newColourValue = e.target.value;

    if (newColourValue && newColourValue[0] !== "#") {
      newColourValue = '#' + newColourValue;
    }

    this.props.onChange(newColourValue);
  }

  render() {

    const isValidColour = this.isValidColour(this.props.value)
    return (
      <div className="colour-picker" style={{position: "relative"}} >
        <input
          type="text"
          className="tag-edit__input"
          onChange={this.onInputChange.bind(this)}
          value={this.props.value || ''}
        />
        <div
          className={isValidColour ? 'colour-picker__swatch' : 'colour-picker__swatch--invalid'}
          style={{backgroundColor: isValidColour ? this.props.value : "#FFF"}}></div>
      </div>
    );
  }
}
