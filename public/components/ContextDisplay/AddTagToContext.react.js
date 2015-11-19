import React from 'react';
import TagSelect from '../utils/TagSelect';

export default class AddTagToContext extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          expanded: false
        };
    }

    expand() {
      this.setState({
        expanded: true
      });
    }

    onAddTag(tag) {
      this.props.onAddTag(tag.id);
      this.contract();
    }

    render () {

      if (!this.state.expanded) {
        return (
          <div className="context__add" onClick={this.expand.bind(this)}>
            <i className="i-plus" />
          </div>
        );
      }

      return (
        <div className="context__add--expanded">
          <TagSelect onTagClick={this.onAddTag.bind(this)}/>
          <i className="i-cross" onClick={this.contract.bind(this)}></i>
        </div>
      );
    }
}
