import React from 'react';
import tagManagerApi from '../util/tagManagerApi';
import ReferenceTypeSelect from './utils/ReferenceTypeSelect.react';

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

    updateMappingValue (tagId, externalReferencesType, oldValue, newValue) {

      const oldTag = this.state.tags.filter((tag) => tag.id === tagId)[0];
      const newTag = Object.assign({}, oldTag, {
        externalReferences: oldTag.externalReferences.map((reference) => {
          if (reference.type === externalReferencesType && reference.value === oldValue) {
            return {
              type: externalReferencesType,
              value: newValue
            };
          }

          return reference;
        })
      });

      this.setState({
        tags: this.state.tags.map((tag) => tag.id === newTag.id ? newTag : tag)
      });
    }

    saveTag(tagId) {
      const tag = this.state.tags.filter((tag) => tag.id === tagId)[0];
      this.props.tagActions.saveTag(tag);
    }

    render () {

      const mappings = [];

      this.state.tags.forEach((tag) => {
        tag.externalReferences.forEach((reference) => {
          mappings.push({
            tagId: tag.id,
            tagInternalName: tag.internalName,
            referenceType: reference.type,
            referenceValue: reference.value
          });
        });
      });

      return (
          <div className="mapping">
            <ReferenceTypeSelect
              referenceTypes={this.props.referenceTypes}
              onChange={this.onSelectedReferenceTypeChange.bind(this)}
              selectedType={this.state.selectedReferenceType}
            />
            <table>
              <thead>
                <tr>
                  <th>Tag Name</th>
                  <th>Mapping Value</th>
                </tr>
              </thead>
              <tbody>
                {mappings.map((mapping) => {

                  const changeFunction = (e) => {
                    this.updateMappingValue(mapping.tagId, mapping.referenceType, mapping.referenceValue, e.target.value);
                  };

                  // This can't have a key on it or input becomes defocused onChange
                  return (
                    <tr>
                      <td>{mapping.tagInternalName}</td>
                      <td>
                        <input value={mapping.referenceValue} onChange={changeFunction}/>
                      </td>
                      <td>
                        <input type="submit" onClick={this.saveTag.bind(this, mapping.tagId)} />
                      </td>
                    </tr>
                  );
                }, this)}
              </tbody>
            </table>
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
