import React from 'react';
import ContentListItem from './ContentListItem';
import R from 'ramda';

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

        const contentIds = this.props.content.map(content => content.id);
        const unselectedContent = R.difference(contentIds, this.props.selectedContent);

        return (
            <table className="grid-table taglist">
              <thead className="taglist__header">
                <tr>
                  <th>
                    <input type="checkbox" checked={unselectedContent.length === 0} onChange={this.props.toggleAllSelected} />
                  </th>
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
