import React from 'react';
import MappingTableRow from './MappingTableRow.react';

export default class MappingTable extends React.Component {

    constructor(props) {
        super(props);
    }

    updateMappingValue (tagId, externalReferencesType, oldValue, newValue) {

      const oldTag = this.props.tags.filter((tag) => tag.id === tagId)[0];
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

      this.props.updateTag(newTag);
    }

    saveTag(tagId) {
      const tag = this.props.tags.filter((tag) => tag.id === tagId)[0];
      this.props.saveTag(tag);
    }

    deleteMapping(tagId, externalReferencesType, value) {
      const oldTag = this.props.tags.filter((tag) => tag.id === tagId)[0];
      const newTag = Object.assign({}, oldTag, {
        externalReferences: oldTag.externalReferences.filter((reference) => {
          if (reference.type === externalReferencesType && reference.value === value) {
            return false;
          }

          return true;
        })
      });

      this.props.saveTag(newTag);
    }

    render() {
      if (!this.props.tags.length) {
        return false;
      }

      const mappings = [];

      this.props.tags.forEach((tag) => {
        tag.externalReferences.forEach((reference, i) => {

          if (this.props.selectedType !== reference.type) {
            return
          }

          mappings.push({
            tagId: tag.id,
            tagInternalName: tag.internalName,
            referenceType: reference.type,
            referenceValue: reference.value,
            referenceIndex: i //this is used to provide a key for this item in table below
          });
        });
      });

      return (
        <table>
          <thead>
            <tr>
              <th>Tag Name</th>
              <th>Mapping Value</th>
              <th>Delete</th>
            </tr>
          </thead>
          <tbody>
            {mappings.sort((a, b) => a.tagInternalName > b.tagInternalName ? 1 : -1).map((mapping) => {
              return (
                <MappingTableRow
                  tagId={mapping.tagId}
                  tagInternalName={mapping.tagInternalName}
                  referenceType={mapping.referenceType}
                  referenceValue={mapping.referenceValue}
                  updateMapping={this.updateMappingValue.bind(this)}
                  deleteMapping={this.deleteMapping.bind(this)}
                  saveTag={this.saveTag.bind(this)}
                  key={mapping.tagId + mapping.referenceType + mapping.referenceIndex}
                />
            );
            }, this)}
          </tbody>
        </table>
      );
    }
}
