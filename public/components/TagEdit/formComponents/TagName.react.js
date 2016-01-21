import React from 'react';
import * as tagTypes from '../../../constants/tagTypes';

function slugify(text) {
  return text ? text.toLowerCase().replace(/[^a-z0-9-]/g, '-') : '';
}

function inferLockStateFromProps(props) {
  return {
    externalNameLocked: props.tag.externalName === undefined || props.tag.externalName === props.tag.internalName,
    comparableValueLocked: props.tag.comparableValue === undefined || props.tag.comparableValue === props.tag.internalName.toLowerCase(),
    slugLocked: props.tag.slug === undefined || props.tag.slug === slugify(props.tag.internalName)
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

  onUpdateInternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      internalName: e.target.value,
      externalName: this.state.externalNameLocked ? e.target.value : this.props.tag.externalName,
      comparableValue: this.state.comparableValueLocked ? e.target.value.toLowerCase() : this.props.tag.comparableValue,
      slug: (!this.props.pathLocked && this.state.slugLocked) ? slugify(e.target.value) : this.props.tag.slug
    }));
  }

  onUpdateComparableValue(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      comparableValue: e.target.value.toLowerCase()
    }));
  }

  onUpdateExternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      externalName: e.target.value
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

  toggleExternalNameLock() {

    var newLockState = !this.state.externalNameLocked;

    this.setState({
      externalNameLocked: newLockState
    });

    if (newLockState) {
      this.props.updateTag(Object.assign({}, this.props.tag, {
        externalName: this.props.tag.internalName
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
        comparableValue: this.props.tag.internalName.toLowerCase()
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
        slug: slugify(this.props.tag.internalName)
      }));
    }
  }

  getPathPrefixForSection() {

    //Infer from Tag Type
    if (!this.props.tag.type) {
      return '/';
    }

    const tagTypeKey = Object.keys(tagTypes).filter((tagTypeKey) => {
      return tagTypes[tagTypeKey].name === this.props.tag.type;
    })[0];

    if (tagTypes[tagTypeKey].pathPrefix) {
      return tagTypes[tagTypeKey].pathPrefix + '/';
    }

    //Infer from section

    if (!this.props.sections || !this.props.tag.section) {
      return '/';
    }

    var section = this.props.sections.filter((section) => section.id === this.props.tag.section);

    if (section.length) {

      if (tagTypes[tagTypeKey].additionalPathPrefix) {
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
      externalName: {
        lock: this.state.externalNameLocked ? 'tag-edit__linked-field__lock' : 'tag-edit__linked-field__lock--unlocked',
        link: this.state.externalNameLocked ? 'tag-edit__linked-field__link--junction' : 'tag-edit__linked-field__link--line'
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
      classNames.externalName.link = this.state.externalNameLocked ? 'tag-edit__linked-field__link--corner' : 'tag-edit__linked-field__link';
    }
    return (
      <div className="tag-edit__input-group">
        <div className="tag-edit__name">
          <label className="tag-edit__input-group__header">Internal Name</label>
          <input className="tag-edit__input" type="text" value={this.props.tag.internalName} onChange={this.onUpdateInternalName.bind(this)} disabled={!this.props.tagEditable}/>
          <div className="tag-edit__linked-field">
            <div className={classNames.externalName.link}></div>
            <div className={classNames.externalName.lock} onClick={this.toggleExternalNameLock.bind(this)}></div>
            <label>External Name</label>
            <div className="tag-edit__linked-field__input-container">
              <input type="text"
                value={this.props.tag.externalName}
                onChange={this.onUpdateExternalName.bind(this)}
                disabled={!this.props.tagEditable}/>
            </div>
          </div>
          <div className="tag-edit__linked-field">
            <div className={classNames.comparableValue.link}></div>
            <div className={classNames.comparableValue.lock} onClick={this.toggleComparableValueLock.bind(this)}></div>
            <label>Sort by name</label>
            <div className="tag-edit__linked-field__input-container">
              <input type="text"
                value={this.props.tag.comparableValue}
                onChange={this.onUpdateComparableValue.bind(this)}
                disabled={!this.props.tagEditable}/>
            </div>
          </div>
          <div className="tag-edit__linked-field">
            <div className={classNames.slug.link}></div>
            <div className={classNames.slug.lock} onClick={this.toggleSlugLock.bind(this)}></div>
            <label>Slug</label>
            <div className="tag-edit__linked-field__input-container">
              <span>{!this.props.pathLocked ? this.getPathPrefixForSection() : this.props.tag.path}</span>
              {!this.props.pathLocked ? <input type="text"
                                          disabled={this.props.pathLocked}
                                          value={this.props.tag.slug}
                                          onChange={this.onUpdateSlug.bind(this)
                                          disabled={!this.props.tagEditable}}/> : false}
            </div>
          </div>
        </div>
      </div>
    );
  }
}
