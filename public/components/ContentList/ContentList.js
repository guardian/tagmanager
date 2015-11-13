import React from 'react';

export default class ContentList extends React.Component {

    constructor(props) {
        super(props);
        this.renderListItem = this.renderListItem.bind(this);
    }

    renderListItem(content) {

      var isChecked = this.props.selectedContent.indexOf(content.id) !== -1;

      return (
          <tr key={content.id} className="taglist__results-item" onClick={this.onContentClick.bind(this, content)}>
            <td>
              <input type="checkbox" checked={isChecked}/>
            </td>
            <td>{content.type}</td>
            <td>{content.webTitle}</td>
            <td>{content.webPublicationDate}</td>
            <td>{content.id}</td>
          </tr>
      );
    }

    onContentClick(content) {
      this.props.contentClicked(content);
    }

    render () {

        if (!this.props.content || !this.props.content.length) {
          return false;
        }

        return (
            <table className="taglist">
              <thead className="taglist__header">
                <tr>
                  <th></th>
                  <th>Type</th>
                  <th>Headline</th>
                  <th>Pub. Date</th>
                  <th>Path</th>
                </tr>
              </thead>
              <tbody className="taglist__results">
                {this.props.content.map(this.renderListItem)}
              </tbody>
            </table>
        );
    }
}
