import React from 'react';
import SectionAddEdition from './SectionAddEdition.react';
import tagManagerApi from '../../util/tagManagerApi';

export default class SectionEdit extends React.Component {

    constructor(props) {
      super(props);
    }

    removeEdition(editionRegion) {
      tagManagerApi.removeEditionFromSection(this.props.section.id, editionRegion).then((resp) => {
        this.props.refreshSection();
      });
    }

    renderEdition(editionRegion) {

      const edition = this.props.section.editions[editionRegion];

      return (
        <tr className="section-edit__editions__item" key={edition.path}>
          <td>{editionRegion}</td>
          <td>{edition.path}</td>
          <td>
            <i className="i-delete" onClick={this.removeEdition.bind(this, editionRegion)} />
          </td>
        </tr>
      );
    }

    renderEditions() {

      if (!Object.keys(this.props.section.editions).length) {
        return false;
      }

      return (
        <table className="grid-table">
          <thead>
            <tr>
              <th>
                Edition
              </th>
              <th>
                Path
              </th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {Object.keys(this.props.section.editions).map(this.renderEdition, this)}
          </tbody>
        </table>
      );
    }

    render () {

      if (this.props.section.isMicrosite) {
        return false;
      }

      return (
        <div className="section-edit__editions">
          <div className="section-edit__header">
            International Editions
          </div>
          {this.renderEditions()}
          <SectionAddEdition section={this.props.section} disabled={this.props.saveState === 'SAVE_STATE_DIRTY'} refreshSection={this.props.refreshSection}/>
        </div>
      );
    }
}
