import React from 'react';
import R from 'ramda';
import { Link } from 'react-router-dom';
import TagSelect from '../../../utils/TagSelect.js';
import TagReferenceList from '../../../TagContext/TagReferenceList.react'

export default class PublicationInfoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  removeMainNewspaperBookSectionId() {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      publicationInformation: R.omit(['mainNewspaperBookSectionId'], this.props.tag.publicationInformation)
    }));
  }

  removeNewspaperBook(tagIdToRemove) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      publicationInformation: Object.assign({}, this.props.tag.publicationInformation, {
        newspaperBooks: this.props.tag.publicationInformation.newspaperBooks.filter((tagId) => tagIdToRemove !== tagId)
      })
    }));
  }

  addMainNewspaperBookSectionTag(tag) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      publicationInformation: Object.assign({}, this.props.tag.publicationInformation, {
        mainNewspaperBookSectionId: tag.id
      })
    }));
  }

  addNewspaperBookTag(tag) {

    const publicationInfo = this.props.tag.publicationInformation;

    this.props.updateTag(Object.assign({}, this.props.tag, {
      publicationInformation: Object.assign({}, publicationInfo, {
        newspaperBooks: publicationInfo.newspaperBooks ? publicationInfo.newspaperBooks.concat([tag.id]) : [tag.id]
      })
    }));
  }

  renderMainNewspaperSectionSelect() {
    if (this.props.tag.publicationInformation && this.props.tag.publicationInformation.mainNewspaperBookSectionId) {
      return (
        <div className="tag-edit__field">
          <label className="tag-edit__label">Main Newspaper Book Section</label>
          <input type="text" disabled="true" className="tag-edit__input" value={this.props.tag.publicationInformation.mainNewspaperBookSectionId}/>
          <i className="i-cross clickable-icon" onClick={this.removeMainNewspaperBookSectionId.bind(this)} />
        </div>
      );
    }

    return (
      <div className="tag-edit__field">
        <label className="tag-edit__label">Main Newspaper Book Section</label> <br />
        <TagSelect onTagClick={this.addMainNewspaperBookSectionTag.bind(this)} tagType="NewspaperBookSection" disabled={!this.props.tagEditable}/>
      </div>
    );
  }

  renderRow(tag, i) {
      const tagName = tag.internalName || tag
      const tagType = tag.type || " "
      const tagId = tag.id || tag

      return (
          <tr className="tag-references__item" key={i}>
            <td><Link to={`/tag/${tagId}`}>{tagId}</Link></td>
            <td>{tagType}</td>
            <td>
              <i className="i-delete clickable-icon" onClick={this.removeNewspaperBook.bind(this, tagId)} />
            </td>
          </tr>
      )
  }

  renderNewspaperBooks() {
    const publicationInfo = this.props.tag.publicationInformation;
    const newspaperBooks = publicationInfo && publicationInfo.newspaperBooks ? publicationInfo.newspaperBooks : [];
    const addTag = (
      <span>
        Select new tag: <TagSelect onTagClick={this.addNewspaperBookTag.bind(this)} tagType="NewspaperBook" disabled={!this.props.tagEditable}/>
      </span>
    )

    return (
        <TagReferenceList title="Newspaper Books" headers={["Name", "Type", ""]} actionButton={addTag} tableClassName="grid-table--light">
            {newspaperBooks.map(this.renderRow.bind(this))}
        </TagReferenceList>
    )
  }

  render () {

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Publication Information</label>
        {this.renderMainNewspaperSectionSelect()}
        {this.renderNewspaperBooks()}
      </div>
    );
  }
}
