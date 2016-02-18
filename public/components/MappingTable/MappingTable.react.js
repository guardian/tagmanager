import React from 'react';
import MappingTableRow from './MappingTableRow.react';

export default class MappingTable extends React.Component {

    constructor(props) {
        super(props);
    }

    updateMappingValue (oldTag, oldReference, newValue) {

      const newTag = Object.assign({}, oldTag, {
        externalReferences: oldTag.externalReferences.map((reference) => {
          if (reference.type === oldReference.type && reference.value === oldReference.value) {
            return Object.assign({}, reference, {
              value: newValue
            });
          }

          return reference;
        })
      });

      this.props.updateTag(newTag);
    }

    saveTag(tag) {
      this.props.saveTag(tag);
    }

    deleteMapping(oldTag, oldReference) {
      const newTag = Object.assign({}, oldTag, {
        externalReferences: oldTag.externalReferences.filter((reference) => {
          if (reference.type === oldReference.type && reference.value === oldReference.value) {
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

          if (this.props.selectedType.typeName !== reference.type) {
            return;
          }

          mappings.push({
            tag: tag,
            reference: reference
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
                  tag={mapping.tag}
                  reference={mapping.reference}
                  updateMapping={this.updateMappingValue.bind(this)}
                  deleteMapping={this.deleteMapping.bind(this)}
                  saveTag={this.saveTag.bind(this)}
                />
            );
            }, this)}
          </tbody>
        </table>
      );
    }
}
