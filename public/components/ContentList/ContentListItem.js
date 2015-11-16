import React from 'react';
import moment from 'moment';

const TAG_LIMIT = 3;

export default class ContentListItem extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          showAllTags: false
        };
    }

    renderTags(content) {

      const tags = this.state.showAllTags ?

      return tags.map((tag) => {
        return (<div key={tag.id} className="taglist__results-item__tag">
          {tag.webTitle},
        </div>);
      });
    }

    render() {
      return (
          <tr key={this.props.content.id} className="taglist__results-item" onClick={this.props.contentClicked}>
            <td>
              <input type="checkbox" checked={this.props.isChecked} readOnly={true}/>
            </td>
            <td>{this.props.content.type}</td>
            <td>{this.props.content.webTitle}</td>
            <td>{moment(this.props.content.webPublicationDate).format('DD/MM/YYYY')}</td>
            <td>{this.props.content.id}</td>
            <td>{this.renderTags(this.props.content)}</td>
            <td>
              <a href={this.props.content.webUrl} target="_blank">
                <i className="i-info-grey"></i>
              </a>
            </td>
          </tr>
      );
    }
}
