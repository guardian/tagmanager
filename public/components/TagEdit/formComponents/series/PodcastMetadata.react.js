import React from 'react';

export default class PodcastMetadata extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      isPodcast: !!props.tag.podcastMetadata
    };
  }

  togglePodcast(e) {
    this.setState({
      isPodcast: !this.state.isPodcast
    });
  }

  renderMetadataForm() {
    if (!this.state.isPodcast) {
      return false;
    }

    return (
      <div>
        PODCAST META
      </div>
    );
  }

  render () {

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Podcast Metadata</label>

        <input type="checkbox" checked={this.state.isPodcast} onChange={this.togglePodcast.bind(this)}/> Is this series a podcast?

        {this.renderMetadataForm()}
      </div>
    );
  }
}
