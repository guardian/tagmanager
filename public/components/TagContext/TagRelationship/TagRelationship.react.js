import React from 'react';
import {Link} from 'react-router';
import R from 'ramda';
import tagManagerApi from '../../../util/tagManagerApi';
import AddTagToContext from './AddRelationship.react';

export default class TagRelationship extends React.Component {

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
      const addParentTagFn = R.append(tagId);

      //Add check if it's already added
      this.props.updateTag(Object.assign({}, this.props.tag, {
        parents: addParentTagFn(this.props.tag.parents)
      }));
    }

    removeParentTag(tag) {
      const removeParentTagFn = R.reject(R.equals(tag.id));

      this.props.updateTag(Object.assign({}, this.props.tag, {
        parents: removeParentTagFn(this.props.tag.parents)
      }));
    }

    renderTag(tagId) {

      const tag = this.state.cachedTags[tagId];

      if (!tag) {
        return 'Fetching: ' + tagId;
      }

      return (
        <div key={tag.id} className="tag-relationship__tag">
          <Link to={`/tag/${tag.id}`}>{tag.internalName}</Link>
          <div className="tag-relationship__tag__remove" onClick={this.removeParentTag.bind(this, tag)}>
            <i className="i-delete" />
          </div>
        </div>
      );
    }

    renderAddTagToContext() {
      if (this.props.tagEditable) {
        return <AddTagToContext onAddTag={this.addParentTag.bind(this)} tagEditable={this.props.tagEditable}/>
      }
      return false;
    }

    render () {
      if (!this.props.tag) {
        console.log('ContextDisplay loaded without tag provided');
        return false;
      }

      if (!this.props.tag.parents) {
        return (
          <div className="tag-context__item">
            No parent tags found
          </div>
        );
      }

      return (
        <div className="tag-context__item">
          <div className="tag-context__header">Parents</div>
          <div className="tag-relationship">
              {this.props.tag.parents.map(this.renderTag)}
              {this.renderAddTagToContext()}
          </div>
        </div>
      );
    }
}
