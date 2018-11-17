import React from 'react';
import * as tagTypes from '../../../constants/tagTypes';

function slugify(text) {
  return text ? text.trim().toLowerCase().replace(/[^a-z0-9-]/g, '-') : '';
}

function inferLockStateFromProps(props) {
  return {
    internalNameLocked: props.tag.internalName === undefined || props.tag.internalName === props.tag.externalName,
    comparableValueLocked: props.tag.comparableValue === undefined || props.tag.comparableValue === props.tag.externalName.toLowerCase(),
    slugLocked: props.tag.slug === undefined || props.tag.slug === slugify(props.tag.externalName)
  };
}

export default class TagNameEdit extends React.Component {

  constructor(props) {
    super(props);
    this.state = inferLockStateFromProps(props);
    this.getPathPrefixForSection = this.getPathPrefixForSection.bind(this);
  }

  componentWillReceiveProps(props) {
    this.setState(inferLockStateFromProps(props));
  }

  onUpdateExternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      externalName: e.target.value,
      internalName: this.state.internalNameLocked ? e.target.value : this.props.tag.internalName,
      comparableValue: this.state.comparableValueLocked ? e.target.value.toLowerCase() : this.props.tag.comparableValue,
      slug: (!this.props.pathLocked && this.state.slugLocked) ? slugify(e.target.value) : this.props.tag.slug
    }));
  }

  onUpdateComparableValue(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      comparableValue: e.target.value.toLowerCase()
    }));
  }

  onUpdateInternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      internalName: e.target.value
    }));
  }

  onUpdateSlug(e) {

    if (this.props.pathLocked) {
      return;
    }

    this.props.updateTag(Object.assign({}, this.props.tag, {
      slug: slugify(e.target.value)
    }));
  }

  toggleInternalNameLock() {

    var newLockState = !this.state.internalNameLocked;

    this.setState({
      internalNameLocked: newLockState
    });

    if (newLockState) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        internalName: this.props.tag.externalName
      }));
    }
  }

  toggleComparableValueLock() {

    var newLockState = !this.state.comparableValueLocked;

    this.setState({
      comparableValueLocked: newLockState
    });

    if (newLockState) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        comparableValue: this.props.tag.externalName.toLowerCase()
      }));
    }
  }

  toggleSlugLock() {
    if (this.props.pathLocked) {
      return;
    }

    var newLockState = !this.state.slugLocked;

    this.setState({
      slugLocked: newLockState
    });

    if (newLockState) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        slug: slugify(this.props.tag.externalName)
      }));
    }
  }

  getPathPrefixForSection() {

    if (!this.props.tag.type) {
      return '.../';
    }

    //Tracking Type Exception

    if (this.props.tag.type === tagTypes.tracking.name) {
      const trackingTypeName = this.props.tag.trackingInformation && this.props.tag.trackingInformation.trackingType ? this.props.tag.trackingInformation.trackingType.toLowerCase() : '...';
      return 'tracking/' + trackingTypeName + '/';
    }

    //Campaigns Type Exception
    if (this.props.tag.type === tagTypes.campaign.name) {
      const campaignTypeName = this.props.tag.campaignInformation && this.props.tag.campaignInformation.campaignType ? this.props.tag.campaignInformation.campaignType.toLowerCase() : '...';
      return 'campaign/' + campaignTypeName + '/';
    }

    // Paid content with sub type of hosted exception

    if (this.props.tag.type === tagTypes.paidContent.name
      && this.props.tag.paidContentInformation
      && this.props.tag.paidContentInformation.paidContentType === 'HostedContent') {

      return 'advertiser-content/';
    }

    //Infer from Tag Type

    const tagTypeKey = Object.keys(tagTypes).filter((tagTypeKey) => {
      return tagTypes[tagTypeKey].name === this.props.tag.type;
    })[0];

    if (tagTypes[tagTypeKey] && tagTypes[tagTypeKey].pathPrefix) {
      return tagTypes[tagTypeKey].pathPrefix + '/';
    }

    //Infer from section

    if (!this.props.sections || !this.props.tag.section) {
      return '.../';
    }

    var section = this.props.sections.filter((section) => section.id === this.props.tag.section);

    if (section.length) {

      if (tagTypes[tagTypeKey] && tagTypes[tagTypeKey].additionalPathPrefix) {
        return section[0].wordsForUrl + '/' + tagTypes[tagTypeKey].additionalPathPrefix + '/';
      }

      return section[0].wordsForUrl + '/';
    } else {
      return '/';
    }
  }

  render () {
    if (!this.props.tag) {
      console.log('TagEdit loaded without tag provided');
      return false;
    }

    var classNames = {
      internalName: {
        lock: this.state.internalNameLocked ? 'tag-edit__linked-field__lock' : 'tag-edit__linked-field__lock--unlocked',
        link: this.state.internalNameLocked ? 'tag-edit__linked-field__link--junction' : 'tag-edit__linked-field__link--line'
      },
      comparableValue: {
        lock: this.state.comparableValueLocked ? 'tag-edit__linked-field__lock' : 'tag-edit__linked-field__lock--unlocked',
        link: this.state.comparableValueLocked ? 'tag-edit__linked-field__link--junction' : 'tag-edit__linked-field__link--line'
      },
      slug: {
        lock: this.state.slugLocked ? 'tag-edit__linked-field__lock' : 'tag-edit__linked-field__lock--unlocked',
        link: this.state.slugLocked ? 'tag-edit__linked-field__link--corner' : 'tag-edit__linked-field__link'
      }
    };

    if (!this.state.slugLocked) {
      classNames.comparableValue.link = this.state.comparableValueLocked ? 'tag-edit__linked-field__link--corner' : 'tag-edit__linked-field__link';
    }

    if (!this.state.slugLocked && !this.state.comparableValueLocked) {
      classNames.internalName.link = this.state.internalNameLocked ? 'tag-edit__linked-field__link--corner' : 'tag-edit__linked-field__link';
    }
    return (
      <div className="tag-edit__input-group">
        <div className="tag-edit__name">
          <label className="tag-edit__input-group__header">External Name</label>
          <input className="tag-edit__input" type="text" value={this.props.tag.externalName || ""} onChange={this.onUpdateExternalName.bind(this)} disabled={!this.props.tagEditable}/>
          <div className="tag-edit__linked-field">
            <div className={classNames.internalName.link}></div>
            <div className={classNames.internalName.lock} onClick={this.toggleInternalNameLock.bind(this)}></div>
            <label>Internal Name</label>
            <div className="tag-edit__linked-field__input-container">
              <input type="text"
                value={this.props.tag.internalName || ""}
                onChange={this.onUpdateInternalName.bind(this)}
                disabled={!this.props.tagEditable}/>
            </div>
          </div>
          <div className="tag-edit__linked-field">
            <div className={classNames.comparableValue.link}></div>
            <div className={classNames.comparableValue.lock} onClick={this.toggleComparableValueLock.bind(this)}></div>
            <label>Sort by name</label>
            <div className="tag-edit__linked-field__input-container">
              <input type="text"
                value={this.props.tag.comparableValue || ""}
                onChange={this.onUpdateComparableValue.bind(this)}
                disabled={!this.props.tagEditable}/>
            </div>
          </div>
          <div className="tag-edit__linked-field">
            <div className={classNames.slug.link}></div>
            <div className={classNames.slug.lock} onClick={this.toggleSlugLock.bind(this)}></div>
            <label>Path</label>
            <div className="tag-edit__linked-field__input-container">
              <span>{!this.props.pathLocked ? this.getPathPrefixForSection() : <a href={`https:\/\/theguardian.com/${this.props.tag.path}`} target="_blank">{this.props.tag.path}</a>}</span>
              {!this.props.pathLocked ? <input type="text"
                                          value={this.props.tag.slug}
                                          onChange={this.onUpdateSlug.bind(this)}
                                          disabled={this.props.pathLocked || !this.props.tagEditable}/> : false}
            </div>
          </div>
        </div>
      </div>
    );
  }
}
