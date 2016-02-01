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

      </div>
    );
  }
}
