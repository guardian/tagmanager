import React from 'react';
import TagNameEdit from './formComponents/TagName.react';
import TagDescriptionEdit from './formComponents/TagDescription.react';

import TagVisibility from './formComponents/TagVisibility.react';
import SectionSelect from '../utils/SectionSelect.react';

import TopicCategories from './formComponents/topic/TopicCategories.js';
import PodcastMetadata from  './formComponents/series/PodcastMetadata.react';
import ContributorInfoEdit from './formComponents/contributor/ContributorInfoEdit.react';
import PublicationInfoEdit from './formComponents/publication/PublicationInfoEdit.react';
import NewspaperBookInfoEdit from './formComponents/newspaperbook/NewspaperBookInfoEdit.react';

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

    onUpdateCategory(selectedCategories) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        categories: selectedCategories
      }));
    }

    renderTopicFields() {
      return (
        <div>
          <div className="tag-edit__input-group" key="topic-section">
            <label className="tag-edit__input-group__header">Section</label>
              <SectionSelect selectedId={this.props.tag.section} sections={this.props.sections} onChange={this.onUpdateSection.bind(this)}/>
          </div>
          <div className="tag-edit__input-group" key="topic-category">
            <label className="tag-edit__input-group__header">Category</label>
              <TopicCategories selectedCategories={this.props.tag.categories} onChange={this.onUpdateCategory.bind(this)}/>
          </div>
        </div>
      );
    }

    renderSeriesFields() {
      return (
        <div>
          <div className="tag-edit__input-group" key="series-section">
            <label className="tag-edit__input-group__header">Section</label>
              <SectionSelect selectedId={this.props.tag.section} sections={this.props.sections} onChange={this.onUpdateSection.bind(this)}/>
          </div>
          <PodcastMetadata tag={this.props.tag} updateTag={this.props.updateTag}/>
        </div>
      );
    }

    renderContributorFields() {
      return <ContributorInfoEdit tag={this.props.tag} updateTag={this.props.updateTag}/>;
    }

    renderPublicationFields() {
      return <PublicationInfoEdit tag={this.props.tag} updateTag={this.props.updateTag} />;
    }

    renderNewspaperBookFields() {
      return <NewspaperBookInfoEdit tag={this.props.tag} updateTag={this.props.updateTag} />;
    }

    renderTagTypeSpecificFields() {

      if (!this.props.tag.type) {
        return false;
      }

      if (this.props.tag.type === tagTypes.topic.name) {
        return this.renderTopicFields();
      }

      if (this.props.tag.type === tagTypes.series.name) {
        return this.renderSeriesFields();
      }

      if (this.props.tag.type === tagTypes.contributor.name) {
        return this.renderContributorFields();
      }

      if (this.props.tag.type === tagTypes.publication.name) {
        return this.renderPublicationFields();
      }

      if (this.props.tag.type === tagTypes.newspaperBook.name) {
        return this.renderNewspaperBookFields();
      }

      return false;
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
