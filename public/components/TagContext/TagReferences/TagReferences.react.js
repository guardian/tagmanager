import React from 'react';
import AddReference from './AddReference.react';
import R from 'ramda';

export default class TagReferences extends React.Component {

    constructor(props) {
        super(props);
    }

    renderReference(reference) {
      return (
        <tr className="tag-references__item" key={reference.type + '/' + reference.value}>
          <td>{reference.type}</td>
          <td>{reference.value}</td>
          <td>
            <i className="i-cross" onClick={this.removeReference.bind(this, reference)} />
          </td>
        </tr>
      );
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

    render() {
      return (
        <div className="tag-context__item">
          <div className="tag-context__header">External References</div>
          <table className="tag-references">
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
              {this.props.tag.externalReferences.map(this.renderReference, this)}
              <tr>
                <td colSpan="3" className="tag-references__addrow">
                  <AddReference onAddReference={this.addReference.bind(this)} />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      );
    }
}
