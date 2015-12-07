import React from 'react';
import {validateImageUrl} from '../../../util/validateImage';
import {getImageMetadata} from '../../../util/supportApi.js';

const FETCH_STATES = {
  error: 'FETCH_STATE_ERROR',
  fetching: 'FETCH_STATE_FETCHING',
  success: 'FETCH_STATE_SUCCESS'
};

export default class TagImageEdit extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      inputUrl: ''
    };

    this.fetchMetadata = this.fetchMetadata.bind(this);
  }

  onImageUpdate() {
    this.props.onChange();
  }

  getMainAsset() {

    if (!this.props.tagImage.assets) {
      return false;
    }

    return this.props.tagImage.assets.sort((a, b) => a.width < b.width)[0];
  }

  removeImage() {
    this.props.onChange({});
  }

  updateInputUrl(e) {
    const url = e.target.value.replace('http://static.guim.co.uk', 'https://static.guim.co.uk')

    this.setState({
      inputUrl: url,
      currentMetadata: false,
      metadataFetchStatus: FETCH_STATES.fetching
    });

    this.fetchMetadata(url);
  }

  addImage() {
    this.props.onChange({
      imageId: this.state.currentMetadata.id,
      assets: [
        {
          height: this.state.currentMetadata.height,
          imageUrl: this.state.inputUrl,
          mimeType: this.state.currentMetadata.mimeType,
          width: this.state.currentMetadata.width
        }
      ]
    });

    this.setState({
      inputUrl: undefined,
      currentMetadata: undefined,
      metadataFetchStatus: undefined
    });
  }

  fetchMetadata(imageUrl) {

    //Validate before any requests happen
    if (!validateImageUrl(imageUrl)) {
      this.setState({
        currentMetadata: false,
        metadataFetchStatus: FETCH_STATES.error
      });

      return false;
    }

    getImageMetadata(imageUrl).then(metadata => {
      if (this.state.inputUrl === imageUrl) { // Check it's still the current url
        this.setState({
          currentMetadata: metadata,
          metadataFetchStatus: FETCH_STATES.success
        });
      }
    }).fail(e => {
      this.setState({
        currentMetadata: false,
        metadataFetchStatus: FETCH_STATES.error
      });
    });
  }

  renderAddButton() {
    if (this.state.metadataFetchStatus === FETCH_STATES.fetching) {
      return (<div className="tag-edit__image__add">Checking Url...</div>);
    }

    if (this.state.metadataFetchStatus === FETCH_STATES.error) {
      return (
        <div className="tag-edit__image__add--error">
          <i className="i-cross-red" />
          Url Not Valid. The image should be located on https://static.guim.co.uk
        </div>
      );
    }

    if (this.state.metadataFetchStatus === FETCH_STATES.success) {
      return (<div className="tag-edit__image__add--success" onClick={this.addImage.bind(this)}><i className="i-tick-green" />Add Image</div>);
    }

    return false;
  }

  renderImage() {

    const imageAsset = this.getMainAsset();

    if (!validateImageUrl(imageAsset.imageUrl)) {
      return (
        <div>
          <input type="text" className="tag-edit__input" value={this.state.inputUrl} onChange={this.updateInputUrl.bind(this)}/>
          {this.renderAddButton()}
        </div>
      );
    }

    return (
      <div className="tag-edit__field">
        <a href={imageAsset.imageUrl} target="_blank">
          <img src={imageAsset.imageUrl} className="tag-edit__field__image"/>
        </a>
        <div className="tag-edit__image__info">
          <div>Width: {imageAsset.width}px</div>
          <div>Height: {imageAsset.height}px</div>
          <div className="tag-edit__image__remove" onClick={this.removeImage.bind(this)}>
            <i className="i-cross-red" />Remove image
          </div>
        </div>
      </div>
    );

  }

  render () {

    return (
        <div className="tag-edit__field">
          <label className="tag-edit__label">{this.props.label}</label>
          {this.renderImage()}
        </div>
    );
  }
}
