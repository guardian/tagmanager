import React from 'react';
import { validateImageUrl } from '../../../util/validateImage';
import { getImageMetadata } from '../../../util/supportApi.js';

const FETCH_STATES = {
  error: 'FETCH_STATE_ERROR',
  fetching: 'FETCH_STATE_FETCHING',
  success: 'FETCH_STATE_SUCCESS',
  badURL: 'FETCH_STATE_BAD_URL',
  notFound: 'FETCH_STATE_NOT_FOUND'
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
    if (!this.props.tagImage || !this.props.tagImage.assets) {
      return false;
    }

    return this.props.tagImage.assets.sort((a, b) => a.width < b.width)[0];
  }

  removeImage() {
    this.props.onChange(undefined);
  }

  updateInputUrl(e) {
    const url = e.target.value.replace('http://', 'https://');

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
        metadataFetchStatus: FETCH_STATES.badURL
      });

      return false;
    }

    getImageMetadata(imageUrl)
      .then(metadata => {
        if (this.state.inputUrl === imageUrl) {
          // Check it's still the current url
          this.setState({
            currentMetadata: metadata,
            metadataFetchStatus: FETCH_STATES.success
          });
        }
      })
      .fail(e => {
        if (e.status === 404) {
          this.setState({
            currentMetadata: false,
            metadataFetchStatus: FETCH_STATES.notFound
          });
        } else {
          this.setState({
            currentMetadata: false,
            metadataFetchStatus: FETCH_STATES.error
          });
        }
      });
  }

  renderAddButton(imageUrl) {
    const renderButtonError = message => (
      <div className="tag-edit__image__add--error">
        <i className="i-cross-red" />
        {message}
      </div>
    );

    if (
      this.props.pngOnly &&
      imageUrl &&
      !(imageUrl.endsWith('png') || imageUrl.endsWith('PNG'))
    ) {
      return renderButtonError(`Image must be a PNG.`);
    }

    if (this.state.metadataFetchStatus === FETCH_STATES.fetching) {
      return <div className="tag-edit__image__add">Checking Url...</div>;
    }

    if (this.state.metadataFetchStatus === FETCH_STATES.error) {
      return renderButtonError(`Server has failed to read image metadata. If this problem persists,
        contact Central Production.`);
    }

    if (this.state.metadataFetchStatus === FETCH_STATES.badURL) {
      const message = (
        <span>
          Invalid image URL. Upload your image with the{" "}
          <a href="https://s3-uploader.gutools.co.uk/">image uploader</a> and
          use the URL it provides.
        </span>
      );
      return renderButtonError(message);
    }

    if (this.state.metadataFetchStatus === FETCH_STATES.notFound) {
      const message = (
        <span>
          Image not found. Upload your image with the{" "}
          <a href="https://s3-uploader.gutools.co.uk/">image uploader</a> and
          use the URL it provides.
        </span>
      );
      return renderButtonError(message);
    }

    if (this.state.metadataFetchStatus === FETCH_STATES.success) {
      return (
        <div
          className="tag-edit__image__add--success"
          onClick={this.addImage.bind(this)}
        >
          <i className="i-tick-green" />
          Click here to add the image
        </div>
      );
    }

    return false;
  }

  renderImage() {
    const imageAsset = this.getMainAsset();

    if (imageAsset.imageUrl) {
      return (
        <div className="tag-edit__field">
          <a href={imageAsset.imageUrl} target="_blank">
            <img src={imageAsset.imageUrl} className="tag-edit__field__image" />
          </a>
          <div className="tag-edit__label">
            {imageAsset.width} &times; {imageAsset.height} px
          </div>
          <div
            className="tag-edit__image__remove"
            onClick={this.removeImage.bind(this)}
          >
            <i className="i-delete" />
            Remove image
          </div>
        </div>
      );
    } else {
      return (
        <div>
          <input
            type="text"
            className="tag-edit__input"
            value={this.state.inputUrl}
            onChange={this.updateInputUrl.bind(this)}
            disabled={!this.props.tagEditable}
            placeholder="Enter image URL..."
          />
          {this.renderAddButton(this.state.inputUrl)}
        </div>
      );
    }
  }

  render() {
    return (
      <div className="tag-edit__field">
        <label className="tag-edit__label">{this.props.label}</label>
        {this.renderImage()}
      </div>
    );
  }
}
