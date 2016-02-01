import React from 'react';
import SectionSelect from '../utils/SectionSelect.react';
import TagSelect from '../utils/TagSelect.js';

export default class TargetingEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  onTagSelected(tag) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      tag: tag
    }));
  }

  onUpdateSection(e) {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      section: parseInt(e.target.value, 10)
    }));
  }

  renderTag(tag, setTagFn) {
    if (!tag) {
      return <TagSelect onTagClick={setTagFn} />;
    }

    return (
      <div className="merge__tag">
        {tag.internalName}
        <i className="i-cross" onClick={setTagFn.bind(this, undefined)} />
      </div>
    );
  }

  sectionId() {
    if(this.props.sponsorship.section) {
      return this.props.sponsorship.section.id;
    } else {
      return undefined;
    }
  }

  hasTag() {
    return this.props.sponsorship.tag && this.props.sponsorship.tag.id;
  }

  render () {

    if (!this.props.sponsorship) {
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Targeting</label>

        <div className="tag-edit__field" >
          <label className="tag-edit__input-group__header">Tag</label>
          {this.renderTag(this.props.sponsorship.tag, this.onTagSelected.bind(this))}
        </div>

        <div className="tag-edit__field" >
          <label className="tag-edit__input-group__header">Section</label>
          <SectionSelect
            selectedId={this.sectionId()}
            sections={this.props.sections}
            isMicrosite={false}
            onChange={this.onUpdateSection.bind(this)}
            disabled={this.hasTag()}
            />
        </div>

      </div>
    );
  }
}
/*
 id: Long,
 validFrom: Option[DateTime],
 validTo: Option[DateTime],
 status: String,
 sponsorshipType: String,
 sponsorName: String,
 sponsorLogo: String,
 sponsorLink: String,
 tag: Option[Long],
 section: Option[Long],
 targetting: Option[SponsorshipTargeting])
 */
