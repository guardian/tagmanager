import React from 'react';
import * as tagTypes from '../../constants/tagTypes';
import { browserHistory } from '../../router';

export default class TagList extends React.Component {

    constructor(props) {
        super(props);
        this.renderListItem = this.renderListItem.bind(this);

        //Much more convenient form of this information, removes need to calculate per row below.
        this.tagTypesMap = Object.keys(tagTypes).reduce((previous, tagTypeKey) => {
          const newObject = {};
          newObject[tagTypes[tagTypeKey].name] = tagTypes[tagTypeKey].displayName;
          return Object.assign({}, previous, newObject);
        }, {});
    }

    renderListItem(tag) {

      const sections = this.props.sections ? this.props.sections.filter((section) => {
        return tag.section === section.id;
      }) : [];

      const sectionName = sections[0] ? sections[0].name : '';

      const tagClickHandler = (e) => {
        e.preventDefault();
        this.onTagClick(tag);
      };

      return (
        <a key={tag.id} href={'/tag/' + tag.id} onClick={tagClickHandler}>
          <div className="taglist__row">
            <div className="taglist__type">{this.tagTypesMap[tag.type] ? this.tagTypesMap[tag.type] : tag.type}</div>
            <div className="taglist__internalName">{tag.internalName}</div>
            <div className="taglist__sectionName">{sectionName}</div>
            <div className="taglist__path">{tag.path}</div>
          </div>
        </a>
      );
    }

    sortBy(fieldName) {
      this.props.sortBy(fieldName);
    }

    onTagClick(tag) {
      browserHistory.push('/tag/' + tag.id);
    }

    render () {

        if (!this.props.tags || !this.props.tags.length) {
          return false;
        }

        return (
            <div className="taglist">
              <div className="taglist__row">
                  <div className="taglist__type--header taglist__item" onClick={this.sortBy.bind(this, 'type')}>Type</div>
                  <div className="taglist__internalName--header taglist__item" onClick={this.sortBy.bind(this, 'internalName')}>Tag Name</div>
                  <div className="taglist__sectionName--header taglist__item" onClick={this.sortBy.bind(this, 'path')}>Section</div>
                  <div className="taglist__path--header taglist__item" onClick={this.sortBy.bind(this, 'path')}>Path</div>
              </div>
              {this.props.tags.map(this.renderListItem)}
            </div>
        );
    }
}
