import React from 'react';
import {Link} from 'react-router';
import R from 'ramda';
import tagManagerApi from '../../../util/tagManagerApi';
import AddTagToContext from './AddRelationship.react';
import * as tagTypes from '../../../constants/tagTypes';

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
          return (
            <tr>
              <td colspan="3">Fetching tag #{tagId}</td>
            </tr>
        )
      }

      return (
         <tr>
           <td><Link to={`/tag/${tag.id}`}>{tag.internalName}</Link></td>
           <td>{tag.type}</td>
           <td onClick={this.removeParentTag.bind(this, tag)}><i className="i-delete" /></td>
         </tr>
      );
    }

    renderAddTagToContext() {
      if (this.props.tagEditable) {
        return <AddTagToContext onAddTag={this.addParentTag.bind(this)} tagEditable={this.props.tagEditable}/>
      }
    }

    render () {
      if (!this.props.tag) {
        console.log('ContextDisplay loaded without tag provided');
        return false;
      }

      if (this.props.tag.type !== tagTypes.topic.name) {
        return false;
      }
      return (
          <div className="tag-context__item">
            <div className="tag-context__header">Parents</div>
            <table className="grid-table tag-references">
              <thead className="tag-references__header">
                <tr>
                  <th>
                    Tag
                  </th>
                  <th>
                    Type
                  </th>
                  <th></th>
                </tr>
              </thead>
              <tbody className="tag-references__references">
                {this.props.tag.parents.map(this.renderTag)}
                <tr>
                  <td colSpan="3" className="tag-references__addrow">
                    {this.renderAddTagToContext()}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
      )
    }
}
