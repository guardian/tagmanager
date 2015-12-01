import React from 'react';
import TagRelationship from './TagRelationship/TagRelationship.react';
import TagReferences from './TagReferences/TagReferences.react';

export default class TagContext extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
      if (!this.props.tag) {
        console.log('TagContext loaded without tag provided');
        return false;
      }

      return (
        <div className="tag-context">
          <TagRelationship {...this.props} />
          <TagReferences {...this.props} />
        </div>
      );
    }
}
