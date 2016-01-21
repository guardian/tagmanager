import React from 'react';
import TagNameEdit from './formComponents/TagName.react';
import TagDescriptionEdit from './formComponents/TagDescription.react';

import TagVisibility from './formComponents/TagVisibility.react';
import SectionSelect from '../utils/SectionSelect.react';

import TopicCategories from './formComponents/topic/TopicCategories.js';
import PodcastMetadata from  './formComponents/series/PodcastMetadata.react';

import * as tagTypes from '../../constants/tagTypes';

export default class TagTypeCustomFields extends React.Component {

    constructor(props) {
        super(props);

        this.renderTagTypeSpecificFields.bind(this);
    }

    onUpdateSection(e) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        section: parseInt(e.target.value, 10)
      }));
    }

    onUpdateCategory(selectedCategories) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        categories: selectedCategories
      }));
    }

    //This will contain the logic for different tag forms (keyword vs contributor etc...)
    renderTagTypeSpecificFields() {

      if (!this.props.tag.type) {
        return false;
      }

      if (this.props.tag.type === tagTypes.topic.name) {
        return [
          (<div className="tag-edit__input-group" key="keyword-section">
            <label className="tag-edit__input-group__header">Section</label>
              <SectionSelect
                selectedId={this.props.tag.section}
                sections={this.props.sections}
                onChange={this.onUpdateSection.bind(this)}
                disabled={!this.props.tagEditable}/>
          </div>),
          (<div className="tag-edit__input-group" key="keyword-category">
            <label className="tag-edit__input-group__header">Category</label>
              <TopicCategories
                selectedCategories={this.props.tag.categories}
                onChange={this.onUpdateCategory.bind(this)}
                tagEditable={this.props.tagEditable}/>
          </div>)
        ];
      } else if (this.props.tag.type === tagTypes.series.name) {
        return <PodcastMetadata
                 tag={this.props.tag}
                 updateTag={this.props.updateTag}
                 tagEditable={this.props.tagEditable}/>;
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

            <TagNameEdit {...this.props}/>
            <TagDescriptionEdit {...this.props}/>

            {this.renderTagTypeSpecificFields()}

            <TagVisibility {...this.props}/>

          </div>
        </div>
      );
    }
}
