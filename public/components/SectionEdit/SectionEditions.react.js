import React from 'react';
import R from 'ramda';

export default class SectionEdit extends React.Component {

    constructor(props) {
      super(props);
    }

    removeEdition(editionRegion) {
      this.props.updateEditions(R.omit([editionRegion], this.props.editions));
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

    render () {

      return (
        <table className="section-edit__editions">
          <thead className="section-edit__editions__header">
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
          <tbody className="section-edit__editions__body">
            {Object.keys(this.props.editions).map(this.renderEdition, this)}
          </tbody>
        </table>
      );
    }
}
