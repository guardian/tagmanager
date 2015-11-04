import React from 'react';

function slugify(text) {
  return text.toLowerCase().replace(/[^a-z0-9-]/g, '-');
}

function inferStateFromProps(props) {
  return {
    externalNameLocked: !props.tag.externalName || props.tag.externalName === props.tag.internalName,
    comparableValueLocked: !props.tag.comparableValue || props.tag.comparableValue === props.tag.internalName.toLowerCase(),
    slugLocked: !props.tag.slug || props.tag.slug === slugify(props.tag.internalName)
  };
}

export default class TagBasicInfo extends React.Component {

  constructor(props) {
    super(props);

    this.state = inferStateFromProps(props);
  }

  componentWillReceiveProps(props) {
    this.setState(inferStateFromProps(props));
  }

  onUpdateInternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      internalName: e.target.value,
      externalName: this.state.externalNameLocked ? e.target.value : this.props.tag.externalName,
      comparableValue: this.state.comparableValueLocked ? e.target.value.toLowerCase() : this.props.tag.comparableValue,
      slug: this.state.slugLocked ? slugify(e.target.value) : this.props.tag.slug
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
          <input className="tag-edit__input" type="text" value={this.props.tag.internalName} onChange={this.onUpdateInternalName.bind(this)}/>
          <div className="tag-edit__linked-field">
            <div className={classNames.externalName.link}></div>
            <div className={classNames.externalName.lock} onClick={this.toggleExternalNameLock.bind(this)}></div>
            <label>External Name</label>
            <div className="tag-edit__linked-field__input-container">
              <input type="text" value={this.props.tag.externalName} onChange={this.onUpdateExternalName.bind(this)} />
            </div>
          </div>
          <div className="tag-edit__linked-field">
            <div className={classNames.comparableValue.link}></div>
            <div className={classNames.comparableValue.lock} onClick={this.toggleComparableValueLock.bind(this)}></div>
            <label>Sort by name</label>
            <div className="tag-edit__linked-field__input-container">
              <input type="text" value={this.props.tag.comparableValue} onChange={this.onUpdateComparableValue.bind(this)} />
            </div>
          </div>
          <div className="tag-edit__linked-field">
            <div className={classNames.slug.link}></div>
            <div className={classNames.slug.lock} onClick={this.toggleSlugLock.bind(this)}></div>
            <label>Slug</label>
            <div className="tag-edit__linked-field__input-container">
              {this.props.tag.path}
            </div>
          </div>
        </div>
      </div>
    );
  }
}
