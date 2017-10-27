import React from 'react';

function slugify(text) {
    return text ? 'pillar/'+ text.toLowerCase().replace(/[^a-z0-9-]/g, '-') : '';
}

export default class PillarEdit extends React.Component {

    constructor(props) {
      super(props);
    }

    onUpdateName(e) {
        this.props.updatePillar(Object.assign({}, this.props.pillar, {
            name: e.target.value,
            path: this.props.pillar.id ? this.props.pillar.path : slugify(e.target.value),
            sectionIds: this.props.pillar.sectionIds ? this.props.pillar.sectionIds : []
        }));
    }

    render () {

      if (!this.props.pillar) {
        return false;
      }

      return (
          <div className="tag-edit__input-group">
              <div className="tag-edit__name">
                  <label className="tag-edit__input-group__header">Name</label>
                  <input className="tag-edit__input" type="text" value={this.props.pillar.name} onChange={this.onUpdateName.bind(this)}/>
              </div>
          </div>
      );
    }
}
