import React from 'react';
import TagSelect from '../../utils/TagSelect';

export default class AddRelationship extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          expanded: false
        };
    }

    toggle() {
      this.setState({
        expanded: !this.state.expanded
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

    renderTagSelect() {
      if(this.state.expanded) {
        return (
          <div className="tag-relationship__add--expanded">
          <TagSelect onTagClick={this.onAddTag.bind(this)} disabled={!this.props.tagEditable}/>
          <i className="i-cross clickable-icon" onClick={this.minimise.bind(this)}></i>
          </div>
        )
      } else {
        return (
          <span></span>
        )
      }
    }

    render () {
      return (
        <span>
          <div className="tag-relationship__add" onClick={this.toggle.bind(this)}>
            <i className="i-plus clickable-icon" /> Add parent
          </div>
          {this.renderTagSelect()}
        </span>
      );
    }
  }
