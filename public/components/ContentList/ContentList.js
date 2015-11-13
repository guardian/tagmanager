import React from 'react';
import ContentListItem from './ContentListItem';

export default class ContentList extends React.Component {

    constructor(props) {
        super(props);
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
                  <th>Tags</th>
                  <th></th>
                </tr>
              </thead>
              <tbody className="taglist__results">
                {this.props.content.map((content) => {
                  const isChecked = this.props.selectedContent.indexOf(content.id) !== -1;
                  return (
                    <ContentListItem key={content.id + '_' + isChecked} content={content} isChecked={isChecked} contentClicked={this.onContentClick.bind(this, content)} />
                  );
                })}
              </tbody>
            </table>
        );
    }
}
