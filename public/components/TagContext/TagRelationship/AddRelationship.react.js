import React from 'react';
import TagSelect from '../../utils/TagSelect';

export default class AddRelationship extends React.Component {

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

    minimise() {
      this.setState({
        expanded: false
      });
    }

    onAddTag(tag) {
      this.props.onAddTag(tag.id);
      this.minimise();
    }

    render () {

      if (!this.state.expanded) {
        return (
          <div className="tag-relationship__add" onClick={this.expand.bind(this)}>
            <i className="i-plus" />
          </div>
        );
      }

      return (
        <div className="tag-relationship__add--expanded">
          <TagSelect onTagClick={this.onAddTag.bind(this)}/>
          <i className="i-cross" onClick={this.minimise.bind(this)}></i>
        </div>
      );
    }
}
