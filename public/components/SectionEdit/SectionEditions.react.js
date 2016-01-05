import React from 'react';
import R from 'ramda';

import {allowedEditions} from '../../constants/allowedEditions';

export default class SectionEdit extends React.Component {

    constructor(props) {
      super(props);
    }

    removeEdition(editionRegion) {
      this.props.updateEditions(R.omit([editionRegion], this.props.editions));
    }

    renderAddEdition() {
      const nonSelectedEditions = allowedEditions.filter(edition => this.props.editions[edition] === undefined);

      if (!nonSelectedEditions.length) {
        return false;
      }

      return (
        <div>
          Possible to Add... {nonSelectedEditions.join(' ')}
        </div>
      );
    }

    renderEdition(editionRegion) {

      const edition = this.props.editions[editionRegion];

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

      if (!Object.keys(this.props.editions).length) {
        return false;
      }

      return (
        <table>
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
            {Object.keys(this.props.editions).map(this.renderEdition, this)}
          </tbody>
        </table>
      );
    }

    render () {

      return (
        <div className="section-edit__editions">
          <div className="section-edit__header">
            International Editions
          </div>
          {this.renderEditions()}
          <div className="section-edit__edition__add">
            {this.renderAddEdition()}
          </div>
        </div>
      );
    }
}
