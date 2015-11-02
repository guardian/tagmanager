import React from 'react';

export default class TagBasicInfo extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      externalNameLocked: props.tag.externalName === props.tag.internalName,
      slugLocked: this.slugify(props.tag.internalName) === props.tag.slug //determine this
    };
  }

  componentWillReceiveProps(props) {
    this.setState({
      externalNameLocked: props.tag.externalName === props.tag.internalName,
      slugLocked: this.slugify(props.tag.internalName) === props.tag.slug //determine this
    });
  }

  onUpdateInternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      internalName: e.target.value,
      externalName: this.state.externalNameLocked ? e.target.value : this.props.tag.externalName,
      slug: this.state.slugLocked ? this.slugify(e.target.value) : this.props.tag.slug
    }));
  }

  onUpdateExternalName(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      externalName: e.target.value
    }));
  }

  onUpdateSlug(e) {
    this.props.updateTag(Object.assign({}, this.props.tag, {
      slug: this.slugify(e.target.value)
    }));
  }

  toggleUnlock(name) {
    var newState = {};
    newState[name] = !this.state[name];
    this.setState(newState);
  }

  slugify(text) {
    return text.toLowerCase().replace(/[^a-z0-9-]/g, '-');
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
      slug: {
        lock: this.state.slugLocked ? 'tag-edit__linked-field__lock' : 'tag-edit__linked-field__lock--unlocked',
        link: this.state.slugLocked ? 'tag-edit__linked-field__link--corner' : 'tag-edit__linked-field__link'
      }
    };

    return (
      <div className="tag-edit__input-group">
        <div className="tag-edit__name">
          <label>Internal Name</label>
          <input className="tag-edit__input" type="text" value={this.props.tag.internalName} onChange={this.onUpdateInternalName.bind(this)}/>
          <div className="tag-edit__linked-field">
            <div className={classNames.externalName.link}></div>
            <div className={classNames.externalName.lock} onClick={this.toggleUnlock.bind(this, 'externalNameLocked')}></div>
            <label>External Name</label>
            <input type="text" value={this.props.tag.externalName} onChange={this.onUpdateExternalName.bind(this)}/>
          </div>
          <div className="tag-edit__linked-field">
            <div className={classNames.slug.link}></div>
            <div className={classNames.slug.lock} onClick={this.toggleUnlock.bind(this, 'slugLocked')}></div>
            <label>Slug</label>
            <input type="text" value={this.props.tag.slug} onChange={this.onUpdateSlug.bind(this)}/>
          </div>
        </div>
      </div>
    );
  }
}
