import React from 'react';
import history from '../../routes/history';
import * as tagTypes from '../../constants/tagTypes';

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

      return (
          <tr key={tag.id} className="taglist__results-item" onClick={this.onTagClick.bind(this, tag)}>
            <td>{this.tagTypesMap[tag.type] ? this.tagTypesMap[tag.type] : tag.type}</td>
            <td>{tag.internalName}</td>
            <td>{sectionName}</td>
            <td>{tag.path}</td>
          </tr>
      );
    }

    sortBy(fieldName) {
      this.props.sortBy(fieldName);
    }

    onTagClick(tag) {
      history.pushState(null, '/tag/' + tag.id);
    }

    render () {

        if (!this.props.tags || !this.props.tags.length) {
          return false;
        }

        return (
            <table className="grid-table taglist">
              <thead className="taglist__header">
                <tr>
                  <th onClick={this.sortBy.bind(this, 'type')}>Type</th>
                  <th onClick={this.sortBy.bind(this, 'internalName')}>Tag Name</th>
                  <th onClick={this.sortBy.bind(this, 'path')}>Section</th>
                  <th onClick={this.sortBy.bind(this, 'path')}>Path</th>
                </tr>
              </thead>
              <tbody className="taglist__results">
                {this.props.tags.map(this.renderListItem)}
              </tbody>
            </table>
        );
    }
}
