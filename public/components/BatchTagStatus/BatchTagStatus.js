import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import TagSelect from '../utils/TagSelect';
const modes = {
  add_to_top: 'ADD_TOP_BATCH_MODE',
  add_to_bottom: 'ADD_BOTTOM_BATCH_MODE',
  remove: 'REMOVE_BATCH_MODE'
};

export default class BatchTagStatus extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          mode: undefined,
          selectedTag: undefined
        };

        this.resetMode = this.resetMode.bind(this);
    }

    switchMode(mode) {
      this.setState({
        mode: modes[mode],
        selectedTag: undefined
      });
    }

    selectTag(tag) {
      this.setState({
        selectedTag: tag
      });
    }

    resetMode() {
      this.setState({
        mode: undefined,
        selectedTag: undefined
      });
    }

    removeTagFromContent() {
      this.props.onRemoveTagFromContent(this.state.selectedTag);
    }

    addTagToContentTop() {
      this.props.onAddTagToContentTop(this.state.selectedTag);
    }

    addTagToContentBottom() {
      this.props.onAddTagToContentBottom(this.state.selectedTag);
    }

    renderNoMode() {
      const pluralContent = this.props.selectedContent.length > 1 ? 'pieces' : 'piece';

      return (
          <div className="batch-status__mode">
            <div className="batch-status__info">
              {this.props.selectedContent.length} {pluralContent} selected
            </div>
            <div className="batch-status__button" onClick={this.switchMode.bind(this, 'add_to_top')}>
              Add tag to top of tag list
            </div>
            <div className="batch-status__button" onClick={this.switchMode.bind(this, 'add_to_bottom')}>
              Add tag to bottom of tag list
            </div>
            <div className="batch-status__button--remove" onClick={this.switchMode.bind(this, 'remove')}>
              Remove tag
            </div>
          </div>
      );
    }

    renderAddTag(isAddTagToTopOfList) {

      if (this.state.selectedTag) {

        const pluralContent = this.props.selectedContent.length > 1 ? 'pieces' : 'piece';
        const _addFn = isAddTagToTopOfList ? this.addTagToContentTop : this.addTagToContentBottom;

        return (
          <div className="batch-status__mode">
            <div className="batch-status__info">
              Add "{this.state.selectedTag.internalName}" to {isAddTagToTopOfList ? 'top' : 'bottom'} of {this.props.selectedContent.length} {pluralContent} of content?
            </div>
            <div className="batch-status__button" onClick={_addFn.bind(this)} >
              <i className="i-tick-green"></i> Confirm
            </div>
            <i className="i-cross" onClick={this.resetMode}></i>
          </div>
        );
      }

      return (
          <div className="batch-status__mode">
            <div className="batch-status__info">
              Add Tag
            </div>
            <TagSelect onTagClick={this.selectTag.bind(this)} showResultsAbove={true} />
            <i className="i-cross" onClick={this.resetMode}></i>
          </div>
      );
    }

    renderRemoveTag() {

      if (this.state.selectedTag) {

        const pluralContent = this.props.selectedContent.length > 1 ? 'pieces' : 'piece';

        return (
          <div className="batch-status__mode">
            <div className="batch-status__info">
              Remove "{this.state.selectedTag.internalName}" from {this.props.selectedContent.length} {pluralContent} of content?
            </div>
            <div className="batch-status__button--remove" onClick={this.removeTagFromContent.bind(this)}>
              <i className="i-cross-red"></i> Confirm
            </div>
            <i className="i-cross" onClick={this.resetMode}></i>
          </div>
        );
      }

      return (
          <div className="batch-status__mode">
            <div className="batch-status__info">
              Remove Tag
            </div>
            <TagSelect onTagClick={this.selectTag.bind(this)} showResultsAbove={true} />
            <i className="i-cross" onClick={this.resetMode}></i>

          </div>
      );
    }

    renderMode() {

      if (this.state.mode === modes.add_to_top) {
        return this.renderAddTag(true);
      }

      if (this.state.mode === modes.add_to_bottom) {
        return this.renderAddTag(false);
      }

      if (this.state.mode === modes.remove) {
        return this.renderRemoveTag();
      }

      return this.renderNoMode();
    }

    render () {

      if (this.props.selectedContent.length === 0) {
        return false;
      }

      return (
        <ReactCSSTransitionGroup transitionName="batch-status-transition" transitionEnterTimeout={500} transitionLeaveTimeout={500}>
          <div className="batch-status__container">
            <div className="batch-status">
              {this.renderMode()}
            </div>
          </div>
        </ReactCSSTransitionGroup>
      );
    }
}
