import React from 'react';
import TagBasicInfo from './TagBasicInfo.react';
import SectionSelect from '../utils/SectionSelect.react';

import * as tagTypes from '../../constants/tagTypes';

export default class TagEdit extends React.Component {

    constructor(props) {
        super(props);

        this.renderTagTypeSpecificFields.bind(this);
    }

    onUpdateSection(e) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        section: parseInt(e.target.value, 10)
      }));
    }

    //This will contain the logic for different tag forms (keyword vs contributor etc...)
    renderTagTypeSpecificFields() {
      if (this.props.tagType === tagTypes.keyword) {

      }
    }

    render () {
      if (!this.props.tag) {
        console.log('TagEdit loaded without tag provided');
        return false;
      }

      return (
        <div className="tag-edit">
          <div className="tag-edit__form">

            <TagBasicInfo {...this.props}/>

            <div className="tag-edit__input-group">
              <label className="tag-edit__input-group__header">Section</label>
              <SectionSelect selectedId={this.props.tag.section} sections={this.props.sections} onChange={this.onUpdateSection.bind(this)}/>
            </div>

            {this.renderTagTypeSpecificFields()}
          </div>
        </div>
      );
    }
}
