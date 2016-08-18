import React from 'react';
import Dropzone from 'react-dropzone';

export default class SponsorEdit extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      errorMessage: undefined
    };
  }

  removeImage() {
    this.props.onImageUpdated(undefined);
  }

  fileUploaded(e) {
    const resp = e.target;
    if (resp.status === 200) {
      this.props.onImageUpdated(JSON.parse(e.target.response));
    } else {
      this.setState({errorMessage: resp.responseText});
    }
  }

  constructPOSTUrl(filename, width, height) {
    let url = '/support/uploadLogo/' + filename;

    url = width || height ? url += '?' : url;
    url = width ? url += 'width=' + width : url;
    url = width && height ? url += '&' : url;
    url = height ? url += 'height=' + height : url;

    return url;
  }

  onDrop(files) {
    var file = files[0];

    var oReq = new XMLHttpRequest();
    oReq.addEventListener('load', this.fileUploaded.bind(this));
    oReq.open('POST', this.constructPOSTUrl(file.name, this.props.requiredWidth, this.props.requiredHeight));
    oReq.send(file);
  }

  errorDiv() {
    if (!this.state.errorMessage) {
      return false;
    }

    return (<div>{this.state.errorMessage}</div>);
  }


  renderImageError(imageAsset) {
    if (imageAsset.height < 500 && imageAsset.width < 500 ) {
      return false;
    }

    return (
      <div className="tag-edit__image__error">
        <i className="i-info-grey" /> Note: The uploaded logo is greater than 500px.
      </div>
    );
  }

  render () {

    if (!this.props.logo) {
      return (
        <div className="tag-edit__field">
          <Dropzone onDrop={this.onDrop.bind(this)} multiple={false} >
            <div>Drop a logo here, or click to select file.</div>
          </Dropzone>
          {this.errorDiv()}
        </div>
      );
    }

    const imageAsset = this.props.logo.assets[0];

    return (
      <div className="tag-edit__field--logo">
        <img src={imageAsset.imageUrl} className="tag-edit__field__image"/>
        <div className="tag-edit__image__info">
          <div>Width: {imageAsset.width}px</div>
          <div>Height: {imageAsset.height}px</div>
          <div className="tag-edit__image__remove" onClick={this.removeImage.bind(this)}>
            <i className="i-cross-red" />Remove image
          </div>
          {this.renderImageError(imageAsset)}
        </div>
      </div>
    );
  }
}
