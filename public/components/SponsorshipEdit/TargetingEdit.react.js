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

    this.state = {
      pickMicrosite: false
    };
  }

  onTagSelected(tag) {
    var tags = this.props.sponsorship.tags || [];
    if(!this.hasTag(tag)) {
      this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
        tags: tags.concat([tag])
      }));
    }
  }

  onTagRemoved(tag) {
    var tags = this.props.sponsorship.tags || [];
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      tags: R.filter(t => tag.id !== t.id, tags)
    }));
  }

  onSectionSelected(e) {
    const sectionId = parseInt(e.target.value, 10);
    const section = R.find(R.propEq('id', sectionId))(this.props.sections);
    var sections = this.props.sponsorship.sections || [];
    if(!this.hasSection(section)) {
      this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
        sections: sections.concat([section])
      }));
    }
  }

  onSectionRemoved(section) {
    var sections = this.props.sponsorship.sections || [];
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      sections: R.filter(s => section.id !== s.id, sections)
    }));
  }

  togglePickingMicrosites() {
    this.setState({pickMicrosite: !this.state.pickMicrosite});
  }

  renderTags(tags) {
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
          <TagSelect onTagClick={selectTagFn} tagType="blog,topic,series" />
        </div>
    );
  }

  renderSections(sections) {
    sections = sections || [];
    var selectSectionFn = this.onSectionSelected.bind(this);
    var removeSectionFn = this.onSectionRemoved.bind(this);
    return (
        <div>
          {sections.map(function (section) {
            return (
                <div className="merge__tag" key={section.id}>
                  {section.name}
                  <i className="i-cross" onClick={removeSectionFn.bind(null, section)}/>
                </div>
            );
          })}
          <input type="checkbox" checked={this.state.pickMicrosite} onChange={this.togglePickingMicrosites.bind(this)} /> Show Microsites
          <SectionSelect
              selectedId=""
              sections={this.props.sections}
              isMicrosite={this.state.pickMicrosite}
              onChange={selectSectionFn}
          />
        </div>
    );
  }

  hasSection(section) {
    var sections = this.props.sponsorship.sections;
    if(!sections || !sections.length) {
      return false;
    }
    return section ? R.any(s => s.id === section.id, sections) : true;
  }

  hasTag(tag) {
    var tags = this.props.sponsorship.tags;
    if(!tags || !tags.length) {
      return false;
    }
    return tag ? R.any(t => t.id === tag.id, tags) : true;
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
          <label className="tag-edit__input-group__header">Sections</label>
          {this.renderSections(this.props.sponsorship.sections)}
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
