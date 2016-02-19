import React from 'react';
import R from 'ramda';
import {allowedEditions} from '../../constants/allowedEditions';
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
    const sectionId = parseInt(e.target.value, 10);
    const section = R.find(R.propEq('id', sectionId))(this.props.sections);
    this.props.updateSponsorship(Object.assign({}, this.props.sponsorship, {
      section: section
    }));
  }

  renderTag(tag, setTagFn) {
    if(!!this.props.sponsorship.section) {
      return (<div>Remove the targeted section to target by tag.</div>);
    }

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

  renderSection() {
    if(!!this.props.sponsorship.tag) {
      return (<div>Remove the targeted tag to target by section.</div>);
    }

    return (
      <SectionSelect
        selectedId={this.sectionId()}
        sections={this.props.sections}
        isMicrosite={false}
        onChange={this.onUpdateSection.bind(this)}
        disabled={this.hasTag()}
        />
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
      console.log("here's some ramda", R);
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
      console.log('setting validEditions to', editions);
      this.props.updateSponsorship(R.merge(this.props.sponsorship, {targeting: {validEditions: editions}}));
    }
  }

  renderEditionToggles() {
    return allowedEditions.map((ed) => {
      console.log("rendering editions", ed, this.isTargettingEdition(ed));
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
          <label className="tag-edit__input-group__header">Tag</label>
          {this.renderTag(this.props.sponsorship.tag, this.onTagSelected.bind(this))}
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
      </div>
    );
  }
}
