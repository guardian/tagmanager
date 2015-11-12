import React from 'react';
import history from '../../routes/history';

export default class TagList extends React.Component {

    constructor(props) {
        super(props);
        this.renderListItem = this.renderListItem.bind(this);
    }

    renderListItem(tag) {

      const sections = this.props.sections ? this.props.sections.filter((section) => {
        return tag.section === section.id;
      }) : [];

      const sectionName = sections[0] ? sections[0].name : '';

      return (
          <tr key={tag.id} className="taglist__results-item" onClick={this.onTagClick.bind(this, tag)}>
            <td>{tag.type}</td>
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
      history.replaceState(null, '/tag/' + tag.id);
    }

    render () {

        if (!this.props.tags || !this.props.tags.length) {
          return false;
        }

        return (
            <table className="taglist">
              <thead className="taglist__header">
                <tr>
                  <th onClick={this.sortBy.bind(this, 'type')}>Type</th>
                  <th onClick={this.sortBy.bind(this, 'internalName')}>Tag Name</th>
                  <th onClick={this.sortBy.bind(this, 'section')}>Sections</th>
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
