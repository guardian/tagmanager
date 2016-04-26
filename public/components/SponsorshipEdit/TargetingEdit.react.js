import React from 'react';
import R from 'ramda';
import moment from 'moment';

import DateTimePicker from 'react-widgets/lib/DateTimePicker';
import momentLocalizer from 'react-widgets/lib/localizers/moment';

import {allowedEditions} from '../../constants/allowedEditions';
import SectionSelect from '../utils/SectionSelect.react';
import TagSelect from '../utils/TagSelect.js';

export default class TargetingEdit extends React.Component {

  constructor(props) {
    super(props);
  }

  onTagSelected(tag) {
    var tags = this.props.sponsorship.tags || [];
    if(!this.hasTag(tag)) {
      tags.push(tag);
      this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
        tags: tags
      }));
    }
  }

  onTagRemoved(tag) {
    var tags = this.props.sponsorship.tags || [];
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      tags: R.filter(t => tag.id !== t.id, tags)
    }));
  }

  onUpdateSection(e) {
    const sectionId = parseInt(e.target.value, 10);
    const section = R.find(R.propEq('id', sectionId))(this.props.sections);
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      section: section
    }));
  }

  clearSection() {
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      section: undefined
    }));
  }

  renderTags(tags) {
    if(!!this.props.sponsorship.section) {
      return (<div>Remove the targeted section to target by tag.</div>);
    }
    tags = tags || [];
    var selectTagFn = this.onTagSelected.bind(this);
    var removeTagFn = this.onTagRemoved.bind(this);

    return (
        <div>
          {tags.map(function (tag) {
            return (
              <div className="merge__tag" key={tag.id}>
                {tag.internalName}
                <i className="i-cross" onClick={removeTagFn.bind(null, tag)}/>
              </div>
            )
          })}
          <TagSelect onTagClick={selectTagFn}/>
        </div>
    );
  }

  renderSection() {
    if(this.hasTag()) {
      return (<div>Remove the targeted tags to target by section.</div>);
    }

    if(!this.sectionId()) {
      return (
        <SectionSelect
          selectedId={this.sectionId()}
          sections={this.props.sections}
          isMicrosite={false}
          onChange={this.onUpdateSection.bind(this)}
          />
      );
    }

    return (
      <div className="merge__tag">
        {this.props.sponsorship.section.name}
        <i className="i-cross" onClick={this.clearSection.bind(this)} />
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

  hasTag(tag) {
    var tags = this.props.sponsorship.tags;
      if(!tags || !tags.length) {
      return false;
    }
    return tag ? !!R.find(t => t.id === tag.id, tags) : true;
  }

  isTargettingAllEditions() {
    return !this.props.sponsorship.targeting || !this.props.sponsorship.targeting.validEditions;
  }

  isTargettingEdition(ed) {
    return !!this.props.sponsorship.targeting &&
      !!this.props.sponsorship.targeting.validEditions &&
      this.props.sponsorship.targeting.validEditions.includes(ed);
  }

  targetAllEditions() {
    if(this.props.sponsorship.targeting) {
      const targetingWithoutEditions = R.omit(['validEditions'], this.props.sponsorship.targeting);
      if (Object.keys(targetingWithoutEditions).length === 0) {
        this.props.updateSponsorship(R.omit(['targeting'], this.props.sponsorship));
      } else {
        this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
          targeting: targetingWithoutEditions
        }));
      }
    }
  }

  toggleTargetEdition(ed) {

    var editions = [];
    if(this.props.sponsorship.targeting && this.props.sponsorship.targeting.validEditions) {
      editions = this.props.sponsorship.targeting.validEditions;
    }

    if(editions.includes(ed)) {
      editions = editions.filter(function(e){ return e !== ed; });
    } else {
      editions.push(ed);
    }

    if(editions.length === 0 || editions.length === allowedEditions.length) {
      this.targetAllEditions();

    } else {
      const t = this.props.sponsorship.targeting ? this.props.sponsorship.targeting : {};
      const updatedTargeting = R.merge(t, {validEditions: editions});

      this.props.updateSponsorship(R.merge(this.props.sponsorship, {targeting: updatedTargeting}));
    }
  }

  publishedSince() {
    if(!!this.props.sponsorship.targeting && !!this.props.sponsorship.targeting.publishedSince) {
      return new Date(this.props.sponsorship.targeting.publishedSince);
    } else {
      return null;
    }
  }

  setPublishedSince(date) {
    if (date) {
      const t = this.props.sponsorship.targeting ? this.props.sponsorship.targeting : {};
      const updatedTargeting = R.merge(t, {publishedSince: moment(date).valueOf()});

      this.props.updateSponsorship(R.merge(this.props.sponsorship, {targeting: updatedTargeting}));
    } else {
      if(this.props.sponsorship.targeting) {
        const targetingWithoutPublishedSince = R.omit(['publishedSince'], this.props.sponsorship.targeting);
        if (Object.keys(targetingWithoutPublishedSince).length === 0) {
          this.props.updateSponsorship(R.omit(['targeting'], this.props.sponsorship));
        } else {
          this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
            targeting: targetingWithoutPublishedSince
          }));
        }
      }
    }
  }

  renderEditionToggles() {
    return allowedEditions.map((ed) => {
      return (
        <span key={ed}>
          <input type="checkbox"
                 onChange={this.toggleTargetEdition.bind(this, ed)}
                 checked={this.isTargettingEdition(ed)} />
          <label className="tag-edit__label"> {ed}</label>
        </span>
      );
    });
  }

  render () {

    if (!this.props.sponsorship) {
      return false;
    }

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Targeting</label>

        <div className="tag-edit__field" >
          <label className="tag-edit__input-group__header">Tags</label>
          {this.renderTags(this.props.sponsorship.tags)}
        </div>

        <div className="tag-edit__field" >
          <label className="tag-edit__input-group__header">Section</label>
          {this.renderSection()}
        </div>

        <div className="tag-edit__field" >
          <label className="tag-edit__input-group__header">Editions</label>
          <input type="checkbox"
                 onChange={this.targetAllEditions.bind(this)}
                 checked={this.isTargettingAllEditions()} />
          <label className="tag-edit__label"> All</label>
          {this.renderEditionToggles()}

        </div>

        <div className="tag-edit__field" >
          <label className="tag-edit__input-group__header">Only show for content published after</label>
          <DateTimePicker
            format={"DD/MM/YYYY HH:mm"}
            value={this.publishedSince()}
            onChange={this.setPublishedSince.bind(this)}/>
        </div>
      </div>
    );
  }
}
