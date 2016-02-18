import React from 'react';
import AddReference from './AddReference.react';
import R from 'ramda';

export default class TagReferences extends React.Component {

    constructor(props) {
        super(props);
    }

    removeReference(reference) {
      const removeReferenceFn = R.reject(R.equals(reference));

      const updatedTag = Object.assign({}, this.props.tag, {
        externalReferences: removeReferenceFn(this.props.tag.externalReferences)
      });

      this.props.updateTag(updatedTag);
    }

    addReference(reference) {
      const addReferenceFn = R.append(reference);

      const updatedTag = Object.assign({}, this.props.tag, {
        externalReferences: addReferenceFn(this.props.tag.externalReferences)
      });

      this.props.updateTag(updatedTag);
    }

    renderReference(reference) {

      const matchingReferenceType = this.props.referenceTypes ? this.props.referenceTypes.filter((type) => type.typeName === reference.type) : [];

      return (
        <tr className="tag-references__item" key={reference.type + '/' + reference.value}>
          <td>{matchingReferenceType.length ? matchingReferenceType[0].displayName : reference.type}</td>
          <td>{reference.value}</td>
          <td>
            <i className="i-delete" onClick={this.removeReference.bind(this, reference)} />
          </td>
        </tr>
      );
    }

    renderAddReferenceButton() {
      if (this.props.tagEditable) {
        return <AddReference onAddReference={this.addReference.bind(this)} referenceTypes={this.props.referenceTypes} />
      }
      return false;
    }

    render() {
      return (
        <div className="tag-context__item">
          <div className="tag-context__header">External References</div>
          <table className="grid-table tag-references">
            <thead className="tag-references__header">
              <tr>
                <th>
                  Name
                </th>
                <th>
                  Value
                </th>
                <th></th>
              </tr>
            </thead>
            <tbody className="tag-references__references">
              {this.props.tag.externalReferences.sort((a, b) => a.type > b.type ? 1 : -1).map(this.renderReference, this)}
              <tr>
                <td colSpan="3" className="tag-references__addrow">
                  {this.renderAddReferenceButton()}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      );
    }
}
