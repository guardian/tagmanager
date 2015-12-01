import React from 'react';

const BLANK_STATE = {
  expanded: false,
  newType: '',
  newValue: ''
};

export default class AddReference extends React.Component {

    constructor(props) {
      super(props);

      this.state = BLANK_STATE;
    }

    expand() {
      this.setState({
        expanded: true
      });
    }

    minimise() {
      this.setState({
        expanded: false
      });
    }

    onTypeChange(e) {
      this.setState({
        newType: e.target.value
      });
    }

    onValueChange(e) {
      this.setState({
        newValue: e.target.value
      });
    }

    addReference(e) {
      e.preventDefault();

      if (!this.state.newType || !this.state.newValue) {
        //Show error
        return;
      }

      this.props.onAddReference({
        type: this.state.newType,
        value: this.state.newValue
      });

      this.setState(BLANK_STATE);
    }

    render () {

      if (!this.state.expanded) {
        return (
          <div className="tag-reference__add" onClick={this.expand.bind(this)}>
            <i className="i-plus" /> Add Reference
          </div>
        );
      }

      return (
        <div className="tag-reference__add">
          <form onSubmit={this.addReference.bind(this)}>
            <input type="text" placeholder="Type" onChange={this.onTypeChange.bind(this)} value={this.state.newType}/>
            <input type="text" placeholder="Value" onChange={this.onValueChange.bind(this)} value={this.state.newValue}/>
            <input type="submit" value="Add" />
          </form>
        </div>
      );
    }
}
