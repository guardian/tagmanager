import React from 'react';
import tagManagerApi from '../util/tagManagerApi';
import ReferenceTypeSelect from './utils/ReferenceTypeSelect.react';
import AddTagMapping from './MappingTable/AddTagMapping.react';
import MappingTable from './MappingTable/MappingTable.react';

export class MappingManager extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          selectedReferenceType: undefined,
          tags: []
        };
    }

    componentDidMount() {
      if (!this.props.referenceTypes || !this.props.referenceTypes.length) {
        this.props.referenceTypeActions.getReferenceTypes();
      }
    }

    fetchReferences(referenceType) {
      tagManagerApi.getTagsByReferenceType(referenceType || this.state.selectedReferenceType).then(tags => {
        this.setState({
          tags: tags
        });
      });
    }

    onSelectedReferenceTypeChange(e) {
      this.setState({
        selectedReferenceType: e.target.value
      });

      this.fetchReferences(e.target.value);
    }

    updateTag(newTag) {
      this.setState({
        tags: this.state.tags.map((tag) => tag.id === newTag.id ? newTag : tag)
      });
    }

    saveTag(newTag) {
      this.props.tagActions.saveTag(newTag);

      //Update state if it exists, or add if not.
      if (this.state.tags.filter((tag) => tag.id === newTag.id).length) {
        this.setState({
          tags: this.state.tags.map((tag) => tag.id === newTag.id ? newTag : tag)
        });
      } else {
        this.setState({
          tags: this.state.tags.concat([newTag])
        });
      }
    }

    render () {
      return (
          <div className="mapping">
            <div className="mapping__header">
              <label className="mapping__header__title">Select a Reference Type</label>
              <ReferenceTypeSelect
                referenceTypes={this.props.referenceTypes}
                onChange={this.onSelectedReferenceTypeChange.bind(this)}
                selectedType={this.state.selectedReferenceType}
              />
            </div>
            <AddTagMapping
              selectedType={this.state.selectedReferenceType}
              saveTag={this.saveTag.bind(this)}
            />
            <MappingTable
              selectedType={this.state.selectedReferenceType}
              referenceTypes={this.props.referenceTypes}
              tags={this.state.tags}
              updateTag={this.updateTag.bind(this)}
              saveTag={this.saveTag.bind(this)}
            />
          </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getReferenceTypes from '../actions/ReferenceTypeActions/getReferenceTypes';
import * as saveTag from '../actions/TagActions/saveTag';

function mapStateToProps(state) {
  return {
    referenceTypes: state.referenceTypes
  };
}

function mapDispatchToProps(dispatch) {
  return {
    tagActions: bindActionCreators(Object.assign({}, saveTag), dispatch),
    referenceTypeActions: bindActionCreators(Object.assign({}, getReferenceTypes), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(MappingManager);
