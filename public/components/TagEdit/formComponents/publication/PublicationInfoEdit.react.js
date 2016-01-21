
import React from 'react';
import R from 'ramda';
import {Link} from 'react-router';

import TagSelect from '../../../utils/TagSelect.js';

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
          <i className="i-cross" onClick={this.removeMainNewspaperBookSectionId.bind(this)} />
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

  renderNewspaperBooks() {

    const publicationInfo = this.props.tag.publicationInformation;
    const newspaperBooks = publicationInfo && publicationInfo.newspaperBooks ? publicationInfo.newspaperBooks : [];

    return (
      <div className="tag-edit__field">
        <label className="tag-edit__label">Newspaper Books</label> <br />
        {newspaperBooks.map((tagId) => {
          return (
            <div key={tagId} className="tag-edit__tag">
              <Link to={`/tag/${tagId}`}>{tagId}</Link>
              <span className="tag-edit__tag__remove" onClick={this.removeNewspaperBook.bind(this, tagId)}>
                <i className="i-delete" />
              </span>
            </div>
          );
        }, this)}
        <TagSelect onTagClick={this.addNewspaperBookTag.bind(this)} tagType="NewspaperBook" disabled={!this.props.tagEditable}/>
      </div>
    );
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
