import React from 'react';
import ReferenceTypeSelect from '../../utils/ReferenceTypeSelect.react';
import MappingsWarning from '../../utils/MappingsWarning.react';

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

      const refType = this.props.referenceTypes.filter((r) => r.typeName === this.state.newType)[0];

      this.props.onAddReference({
        type: refType.typeName,
        capiType: refType.capiType,
        value: this.state.newValue
      });

      this.setState(BLANK_STATE);
    }

    render () {

      if (!this.state.expanded) {
        return (
          <div onClick={this.expand.bind(this)}>
            <i className="i-plus clickable-icon" /> Add Reference
          </div>
        );
      }

      return (
        <div>
          <form onSubmit={this.addReference.bind(this)}>
            <ReferenceTypeSelect referenceTypes={this.props.referenceTypes} onChange={this.onTypeChange.bind(this)} selectedType={this.state.newType} />
            <input type="text" placeholder="Value" onChange={this.onValueChange.bind(this)} value={this.state.newValue}/>
            <input type="submit" value="Add" />
          </form>
          <MappingsWarning capiUsages={this.props.tagUsages}/>
        </div>
      );
    }
}
