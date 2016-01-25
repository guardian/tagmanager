import React from 'react';

function slugify(text) {
  return text ? text.toLowerCase().replace(/[^a-z0-9-]/g, '-') : '';
}

function inferLockStateFromProps(props) {
  return {
    comparableValueLocked: props.section.comparableValue === undefined || props.section.comparableValue === props.section.name.toLowerCase(),
    wordsForUrlLocked: props.section.wordsForUrl === undefined || props.section.wordsForUrl === slugify(props.section.name)
  };
}

export default class SectionNameEdit extends React.Component {

  constructor(props) {
    super(props);
    this.state = inferLockStateFromProps(props);
  }

  componentWillReceiveProps(props) {
    this.setState(inferLockStateFromProps(props));
  }

  onUpdateName(e) {
    this.props.updateSection(Object.assign({}, this.props.section, {
      name: e.target.value,
      wordsForUrl: (!this.props.pathLocked && this.state.wordsForUrlLocked) ? slugify(e.target.value) : this.props.section.wordsForUrl
    }));
  }

  onUpdatewordsForUrl(e) {

    if (this.props.pathLocked) {
      return;
    }

    this.props.updateSection(Object.assign({}, this.props.section, {
      wordsForUrl: slugify(e.target.value)
    }));
  }

  togglewordsForUrlLock() {
    if (this.props.pathLocked) {
      return;
    }

    var newLockState = !this.state.wordsForUrlLocked;

    this.setState({
      wordsForUrlLocked: newLockState
    });

    if (newLockState) {
      this.props.updateSection(Object.assign({}, this.props.section, {
        wordsForUrl: slugify(this.props.section.name)
      }));
    }
  }

  render () {
    if (!this.props.section) {
      return false;
    }

    var classNames = {
      wordsForUrl: {
        lock: this.state.wordsForUrlLocked ? 'tag-edit__linked-field__lock' : 'tag-edit__linked-field__lock--unlocked',
        link: this.state.wordsForUrlLocked ? 'tag-edit__linked-field__link--corner' : 'tag-edit__linked-field__link'
      }
    };

    return (
      <div className="tag-edit__input-group">
        <div className="tag-edit__name">
          <label className="tag-edit__input-group__header">Name</label>
          <input className="tag-edit__input" type="text" value={this.props.section.name} onChange={this.onUpdateName.bind(this)}/>
          <div className="tag-edit__linked-field">
            <div className={classNames.wordsForUrl.link}></div>
            <div className={classNames.wordsForUrl.lock} onClick={this.togglewordsForUrlLock.bind(this)}></div>
            <label>Path</label>
            <div className="tag-edit__linked-field__input-container">
              {
                !this.props.pathLocked ?
                <input type="text" value={this.props.section.wordsForUrl} onChange={this.onUpdatewordsForUrl.bind(this)}/>
                :
                <span>{this.props.section.path}</span>
              }
            </div>
          </div>
        </div>
      </div>
    );
  }
}
