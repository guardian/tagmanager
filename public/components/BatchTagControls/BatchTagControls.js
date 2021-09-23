import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group';
import TagSelect from '../utils/TagSelect';
import tagManagerApi from '../../util/tagManagerApi';
import { browserHistory } from 'react-router'

export default class BatchTagControls extends React.Component {
    constructor(props) {
        super(props);

        this.modeMap = {
          'add_to_top': {
            text: 'Add tag to top',
            func: this.addTagToContentTop
          },
          'add_to_bottom': {
            text: 'Add tag to bottom',
            func: this.addTagToContentBottom
          },
          'remove': {
            text: 'Remove tag',
            func: this.removeTagFromContent
          }
        }

        this.state = {
            mode: '',
            toAddToTop: [],
            toAddToBottom: [],
            toRemove: [],
        };
    }

    switchMode(mode) {
      this.setState({
        mode
      });
    }

    addTagToContentTop(tag) {
        if (!this.state.toAddToTop.some(t => t.id === tag.id)) {
            this.setState({
              toAddToTop: [tag, ...this.state.toAddToTop],
              mode: ''
            });
        }
    }

    addTagToContentBottom(tag) {
        if (!this.state.toAddToBottom.some(t => t.id === tag.id)) {
            this.setState({
              toAddToBottom: [tag, ...this.state.toAddToBottom],
              mode: ''
            });
        }
    }

    removeTagFromContent(tag) {
        if (!this.state.toRemove.some(t => t.id === tag.id)) {
            this.setState({
              toRemove: [tag, ...this.state.toRemove],
              mode: ''
            });
        }
    }

    resetMode() {
        this.setState({
            mode: ''
        });
    }

    performBatchTag(tag, operation) {
      const {toAddToTop, toAddToBottom, toRemove} = this.state;
      const toId = (tag) => tag.id;
      // Currently we only support setting 1 tag to "add to top" because otherwise it will depends on the order the events are processed
      tagManagerApi.batchTag(this.props.selectedContent, toAddToTop.map(toId)[0], toAddToBottom.map(toId), toRemove.map(toId))
        .then(_ => browserHistory.push('/status'));
    }

    renderButtons() {
      const pluralContent = this.props.selectedContent.length > 1 ? 'pieces' : 'piece';

      const topButtonClass = this.state.toAddToTop.length > 0 ? "batch-status__button--disabled" : "batch-status__button";
      const topButtonFunc = this.state.toAddToTop.length > 0 ? () => {} : this.switchMode.bind(this, 'add_to_top');

      return (
        <div className="batch-status__mode">
          <div className={topButtonClass} onClick={topButtonFunc}>
            Add tag to top of tag list
          </div>
          <div className="batch-status__button" onClick={this.switchMode.bind(this, 'add_to_bottom')}>
            Add tag to bottom of tag list
          </div>
          <div className="batch-status__button--remove" onClick={this.switchMode.bind(this, 'remove')}>
            Remove tag
          </div>
          <div className="batch-status__info">
            {this.props.selectedContent.length} {pluralContent} selected
          </div>
        </div>
      );
    }


    renderTagPicker() {
      const {text, func} = this.modeMap[this.state.mode];
      return (
          <div className="batch-status__mode">
            <div className="batch-status__info">
              {text}
            </div>
            <TagSelect onTagClick={func.bind(this)} showResultsAbove={true} />
            <i className="i-cross batch-status__cancel" onClick={this.resetMode.bind(this)}></i>
          </div>
      );
    }

    renderMode() {
      if (!this.state.mode) {
        return this.renderButtons();
      } else {
        return this.renderTagPicker();
      }
    }

    removeFromList(tagIndex, list, listName) {
      const before = [...list].splice(0, tagIndex);
      const after = [...list].splice(tagIndex + 1);

      this.setState({
        [listName]: [...before, ...after]
      });
    }

    renderBasket() {
      const renderCategory = (title, tags, name) => {
        if (tags.length) {
          return (
            <ul className='batch-status__basket-category'>
              <h3 className='batch-status__basket-category-title'>{title}</h3>
              {tags.map((tag, i) => {
                return (
                    <li key={tag.id}>
                        <i className="i-cross" onClick={() => this.removeFromList(i, tags, name)}></i>
                        <strong>{tag.internalName}</strong> <span className='batch-status__small-path'>({tag.path})</span>
                    </li>
                );
              })}
            </ul>
          );
        } else {
          return false;
        }
      };

      return (
          <div className='batch-status__basket'>
            <h2 className='batch-status__basket-title'>Changes</h2>
              {renderCategory('To Add to Top', this.state.toAddToTop, 'toAddToTop')}
              {renderCategory('To Add to Bottom', this.state.toAddToBottom, 'toAddToBottom')}
              {renderCategory('To Remove', this.state.toRemove, 'toRemove')}
            <button className='batch-status__submit' onClick={this.performBatchTag.bind(this)}>Submit</button>
          </div>
      );
    }


    render () {
      if (this.props.selectedContent.length === 0) {
        return false;
      }

      const {toAddToTop, toAddToBottom, toRemove} = this.state;
      return (
        <ReactCSSTransitionGroup transitionName="batch-status-transition" transitionEnterTimeout={500} transitionLeaveTimeout={500}>
          <div className="batch-status__container">
            <div className="batch-status">
              {this.renderMode()}
            </div>
            {toAddToTop.length + toAddToBottom.length + toRemove.length > 0 ? this.renderBasket() : false}
          </div>
        </ReactCSSTransitionGroup>
      );
    }
}
