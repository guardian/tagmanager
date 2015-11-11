import React from 'react';
import {Link} from 'react-router';
import tagManagerApi from '../../util/tagManagerApi';
import AddTagToContext from './AddTagToContext.react';

export default class ContextDisplay extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
          cachedTags: {}
        };

        this.renderTag = this.renderTag.bind(this);
    }

    componentDidMount() {
      if (!this.props.tag.parents || !this.props.tag.parents.length) {
        return;
      }

      this.props.tag.parents.forEach(parentId => {
        this.fetchTagInformation(parentId);
      });
    }

    componentWillReceiveProps(props) {
      props.tag.parents.forEach(parentId => {
        this.fetchTagInformation(parentId);
      });
    }

    fetchTagInformation(id) {

      if (this.state.cachedTags[id]) {
          return;
      }

      tagManagerApi.getTag(id)
          .then(tag => {
            var newState = {};
            newState[id] = tag;

            this.setState({
              cachedTags: Object.assign({}, this.state.cachedTags, newState)
            });

          })
          .fail(error => {
            console.log('Could not fetch parent tag details, id: ', id, error);
          });
    }

    addParentTag(tagId) {
      //Add check if it's already added

      this.props.updateTag(Object.assign({}, this.props.tag, {
        parents: this.props.tag.parents.concat([tagId])
      }));
    }

    removeParentTag(tag) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        parents: this.props.tag.parents.filter(tagId => tagId !== tag.id)
      }));
    }

    renderTag(tagId) {

      const tag = this.state.cachedTags[tagId];

      if (!tag) {
        return 'Fetching: ' + tagId;
      }

      return (
        <div key={tag.id} className="context__tag">
          <Link to={`/tag/${tag.id}`}>{tag.internalName}</Link>
          <div className="context__tag__remove" onClick={this.removeParentTag.bind(this, tag)}>
            <i className="i-delete" />
          </div>
        </div>
      );
    }

    render () {
      if (!this.props.tag) {
        console.log('ContextDisplay loaded without tag provided');
        return false;
      }

      if (!this.props.tag.parents.length) {
        return (
          <div className="context--nocontext">
            No parent tags found
          </div>
        );
      }

      return (
        <div className="context">
          <div className="context__header">Parents</div>
          <div className="context__parents">
              {this.props.tag.parents.map(this.renderTag)}
              <AddTagToContext onAddTag={this.addParentTag.bind(this)}/>
          </div>
        </div>
      );
    }
}
