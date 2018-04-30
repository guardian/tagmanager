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
import PaidContentInfoEdit from './formComponents/paidcontent/PaidContentInfoEdit.react';
import TrackingInfoEdit from './formComponents/tracking/TrackingInformation.react.js';
import CampaignInfoEdit from './formComponents/campaigns/CampaignInformation.react.js';


import * as tagTypes from '../../constants/tagTypes';

export default class TagEdit extends React.Component {

    constructor(props) {
        super(props);

        this.renderTagTypeSpecificFields.bind(this);
    }

    onUpdateSection(e) {

      const sectionId = parseInt(e.target.value, 10);
      const section = this.props.sections.filter((section) => section.id === sectionId)[0];

      this.props.updateTag(Object.assign({}, this.props.tag, {
        section: sectionId,
        capiSectionId: section.path
      }));
    }

    onUpdateCategory(selectedCategories) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        categories: selectedCategories
      }));
    }

    onUpdateIsMicrosite(e) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        isMicrosite: e.target.checked,
        section: undefined,
        capiSectionId: undefined
      }));
    }

    renderTopicFields() {
      return (
        <div>
          <div className="tag-edit__input-group" key="topic-section">
            <label className="tag-edit__input-group__header">Section</label>
              <input type="checkbox" checked={this.props.tag.isMicrosite} onChange={this.onUpdateIsMicrosite.bind(this)} disabled={this.props.pathLocked || !this.props.tagEditable}/>
              <label className="tag-edit__label">is Microsite</label>
              <SectionSelect
                selectedId={this.props.tag.section}
                sections={this.props.sections}
                isMicrosite={this.props.tag.isMicrosite}
                onChange={this.onUpdateSection.bind(this)}
                disabled={this.props.pathLocked || !this.props.tagEditable}
              />
          </div>
        </div>
      );

      //TODO RE-ADD THIS COMPONENT:
      //<div className="tag-edit__input-group" key="topic-category">
      //   <label className="tag-edit__input-group__header">Category</label>
      //     <TopicCategories
      //       selectedCategories={this.props.tag.categories}
      //       onChange={this.onUpdateCategory.bind(this)}
      //       tagEditable={this.props.tagEditable}/>
      // </div>
    }

    renderSeriesFields() {
      return (
        <div>
          <div className="tag-edit__input-group" key="series-section">
            <label className="tag-edit__input-group__header">Section</label>
              <input type="checkbox" checked={this.props.tag.isMicrosite} onChange={this.onUpdateIsMicrosite.bind(this)} disabled={this.props.pathLocked || !this.props.tagEditable}/> Show Microsites
              <SectionSelect
                selectedId={this.props.tag.section}
                sections={this.props.sections}
                isMicrosite={this.props.tag.isMicrosite}
                onChange={this.onUpdateSection.bind(this)}
                disabled={this.props.pathLocked || !this.props.tagEditable}
              />
            </div>
          <PodcastMetadata tag={this.props.tag} updateTag={this.props.updateTag} tagEditable={this.props.tagEditable}/>
        </div>
      );
    }

    renderContributorFields() {
      return <ContributorInfoEdit tag={this.props.tag} updateTag={this.props.updateTag} tagEditable={this.props.tagEditable}/>;
    }

    renderPublicationFields() {
      return <PublicationInfoEdit tag={this.props.tag} updateTag={this.props.updateTag} tagEditable={this.props.tagEditable}/>;
    }

    renderNewspaperBookFields() {
      return <NewspaperBookInfoEdit tag={this.props.tag} updateTag={this.props.updateTag} tagEditable={this.props.tagEditable}/>;
    }

    renderPaidContentFields() {
      return <PaidContentInfoEdit tag={this.props.tag} updateTag={this.props.updateTag} tagEditable={this.props.tagEditable} pathLocked={this.props.pathLocked} sections={this.props.sections} />;
    }

    renderTrackingFields() {
      return <TrackingInfoEdit tag={this.props.tag} updateTag={this.props.updateTag} tagEditable={this.props.tagEditable}/>;
    }

    renderCampaignFields() {
      return <CampaignInfoEdit tag={this.props.tag} updateTag={this.props.updateTag} tagEditable={this.props.tagEditable} />;
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

      if (this.props.tag.type === tagTypes.paidContent.name) {
        return this.renderPaidContentFields();
      }

      if (this.props.tag.type === tagTypes.tracking.name) {
        return this.renderTrackingFields();
      }

      if (this.props.tag.type === tagTypes.campaign.name) {
        return this.renderCampaignFields();
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
