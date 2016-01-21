
import React from 'react';

import PublicationSelect from '../../../utils/PublicationSelect.react';

export default class ContributorInfoEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  updatePublicationId(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      publication: parseInt(e.target.value, 10)
    }));
  }

  render () {

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Newspaper Book Information</label>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Publication</label>
          <PublicationSelect selectedId={this.props.tag.publication} onChange={this.updatePublicationId.bind(this)} disabled={!this.props.tagEditable}/>
        </div>
      </div>
    );
  }
}
