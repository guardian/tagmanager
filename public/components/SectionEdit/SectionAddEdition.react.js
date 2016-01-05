import React from 'react';

import {allowedEditions} from '../../constants/allowedEditions';
import tagManagerApi from '../../util/tagManagerApi';

export default class SectionAddEdition extends React.Component {

    constructor(props) {
      super(props);

      this.state = {
        adding: false
      }
    }

    getNonSelectedEditions() {
      return allowedEditions.filter(edition => this.props.section.editions[edition] === undefined);
    }

    addEdition(editionRegion) {
      tagManagerApi.addEditionToSection(this.props.section.id, editionRegion).then((resp) => {
        this.props.refreshSection();
      });
    }

    render() {

      if (this.props.disabled) {
        return (
          <div>
            Save (or Reset) section updates before adding a region.
          </div>
        )
      }

      if (!this.getNonSelectedEditions().length) {
        return false;
      }

      return (
        <div className="section-edit__edition__add">
          {this.getNonSelectedEditions().map((edition) => {
            return (
              <button onClick={this.addEdition.bind(this, edition)} key={edition}>Add a {edition} edition</button>
            );
          }, this)}
        </div>
      );
    }
}
