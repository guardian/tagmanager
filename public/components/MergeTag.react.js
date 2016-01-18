import React from 'react';
import TagSelect from './utils/TagSelect.js';
import CapiStats from './CapiStats/CapiStats.react';
import ConfirmButton from './utils/ConfirmButton.react';
import tagManagerApi from '../util/tagManagerApi';
import history from '../routes/history';
import showError from '../actions/UIActions/showError';

export default class MergeTag extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
          fromTag: undefined,
          toTag: undefined
        };
    }

    setFromTag(tag) {
      this.setState({
        fromTag: tag
      });
    }

    setToTag(tag) {
      this.setState({
        toTag: tag
      });
    }

    performMerge() {
      tagManagerApi.mergeTag(this.state.fromTag.id, this.state.toTag.id)
      .then((res) => {
        history.pushState(null, '/status');
      })
      .error((error) => {
        showError(error);
      });
    }

    renderButton() {
      if (!this.state.fromTag || !this.state.toTag) {
        return false;
      }

      return (
        <div className="merge__confirmation">
          <div>This operation will DELETE all instances of the tag "{this.state.fromTag.internalName}" and replace them with the "{this.state.toTag.internalName}" tag. This operation is not reversible.</div>
          <ConfirmButton onClick={this.performMerge.bind(this)} buttonText="Perform Merge"/>
        </div>
      );
    }

    renderTag(tag, setTagFn) {
      if (!tag) {
        return <TagSelect onTagClick={setTagFn} />;
      }

      return (
        <div className="merge__tag">
          {tag.internalName}
          <i className="i-cross" onClick={setTagFn.bind(this, undefined)} />
          <CapiStats tag={tag}/>
        </div>
      );
    }

    render () {
        return (
            <div className="merge">
              <div className="merge__select">
                <div className="merge__select__header">From:</div>
                {this.renderTag(this.state.fromTag, this.setFromTag.bind(this))}
              </div>
              <div className="merge__select">
                <div className="merge__select__header">To:</div>
                {this.renderTag(this.state.toTag, this.setToTag.bind(this))}
              </div>
              {this.renderButton()}
            </div>
        );
    }
}
