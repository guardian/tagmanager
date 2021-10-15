import React from 'react';
import AddReference from './AddReference.react';
import TagReferenceList from '../TagReferenceList.react'
import R from 'ramda';
import {getByTag} from '../../../util/capiClient';

export default class TagReferences extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tagUsages: 0
        };
    }

    componentDidMount() {
      getByTag(this.props.tag, {
        'page-size': 0
      }).then(res => {
        this.setState({
          tagUsages: res.response.total
        });
      });
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
            <i className="i-delete clickable-icon" onClick={this.removeReference.bind(this, reference)} />
          </td>
        </tr>
      );
    }

    renderAddReferenceButton() {
      if (this.props.tagEditable) {
        return <AddReference onAddReference={this.addReference.bind(this)} referenceTypes={this.props.referenceTypes} tagUsages={this.state.tagUsages}/>
      }
      return false;
    }

    render() {
      return (
        <TagReferenceList title="External References" headers={["Type", "Value", ""]} actionButton={this.renderAddReferenceButton()}>
          {this.props.tag.externalReferences.sort((a, b) => a.type > b.type ? 1 : -1).map(this.renderReference, this)}
        </TagReferenceList>
      )
    }
}
