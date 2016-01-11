
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

  removeNewspaperBook(removeTag) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      publicationInformation: Object.assign({}, this.props.tag.publicationInformation, {
        newspaperBooks: this.props.tag.publicationInformation.newspaperBooks.filter((tag) => tag.id !== removeTag.id)
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
        <TagSelect onTagClick={this.addMainNewspaperBookSectionTag.bind(this)} tagType="NewspaperBookSection"/>
      </div>
    );
  }

  renderNewspaperBooks() {

    const publicationInfo = this.props.tag.publicationInformation;
    const newspaperBooks = publicationInfo && publicationInfo.newspaperBooks ? publicationInfo.newspaperBooks : [];

    return (
      <div className="tag-edit__field">
        <label className="tag-edit__label">Newspaper Books</label> <br />
        {newspaperBooks.map((tag) => {
          return (
            <div key={tag.id} className="tag-edit__tag">
              <Link to={`/tag/${tag.id}`}>{tag.internalName}</Link>
              <div className="tag-edit__tag__remove" onClick={this.removeNewspaperBook.bind(this, tag)}>
                <i className="i-delete" />
              </div>
            </div>
          );
        }, this)}
        <TagSelect onTagClick={this.addNewspaperBookTag.bind(this)} tagType="NewspaperBook"/>
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
