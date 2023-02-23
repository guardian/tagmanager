import React from 'react';
import R from 'ramda';

import TagImageEdit from '../TagImageEdit.react';
import PodcastCategorySelect from './PodcastCategorySelect.js';

export default class PodcastMetadata extends React.Component {

  constructor(props) {
    super(props);
  }

  togglePodcast(e) {
    if (this.tagHasPodcast()) {
      this.props.updateTag(R.omit(['podcastMetadata'], this.props.tag));
    } else {
      this.props.updateTag(R.merge(this.props.tag, {
        podcastMetadata: {}
      }));
    }
  }

  updateLinkUrl(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {linkUrl: e.target.value})
    }));
  }

  updateCopyright(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {copyrightText: e.target.value})
    }));
  }

  updateAuthor(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {authorText: e.target.value})
    }));
  }

  updateiTunesUrl(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {iTunesUrl: e.target.value})
    }));
  }

  updateGooglePodcastsUrl(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {googlePodcastsUrl: e.target.value})
    }));
  }

  updateSpotifyUrl(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {spotifyUrl: e.target.value})
    }));
  }

  updateAcastId(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {acastId: e.target.value})
    }));
  }

  updatePocketCastsUrl(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {pocketCastsUrl: e.target.value})
    }));
  }

  updateiTunesBlock(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {iTunesBlock: e.target.checked})
    }));
  }

  updateClean(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {clean: e.target.checked})
    }));
  }

  updateExplicit(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {explicit: e.target.checked})
    }));
  }

  updateImage(image) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {image: image})
    }));
  }

  updatePodcastType(e) {

      const newType = e.target.value;

      if (!newType) {
          this.props.updateTag(R.merge(this.props.tag, {
              podcastMetadata: R.omit(['podcastType'], this.props.tag.podcastMetadata)
          }));

          return;
      }

      this.props.updateTag(R.merge(this.props.tag, {
          podcastMetadata: R.merge(this.props.tag.podcastMetadata, {podcastType: e.target.value})
          }));
  }

  renderMetadataForm() {
    if (!this.tagHasPodcast()) {
      return false;
    }

    return (
      <div>
        <PodcastCategorySelect
          tag={this.props.tag}
          updateTag={this.props.updateTag}/>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Podcast Type</label>
          <select value={this.props.tag.podcastMetadata.podcastType || ""} onChange={this.updatePodcastType.bind(this)} >
            <option value=""></option>
            <option value="episodic" key="episodic">Episodic</option>
            <option value="serial" key="serial">Serial</option>
          </select>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Link URL</label>
          <input type="text"
            className="tag-edit__input"
            value={this.props.tag.podcastMetadata.linkUrl || ''}
            onChange={this.updateLinkUrl.bind(this)}
            disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Copyright</label>
          <input type="text"
            className="tag-edit__input"
            value={this.props.tag.podcastMetadata.copyrightText || ''}
            onChange={this.updateCopyright.bind(this)}
            disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Author</label>
          <input type="text"
            className="tag-edit__input"
            value={this.props.tag.podcastMetadata.authorText || ''}
            onChange={this.updateAuthor.bind(this)}
            disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">iTunes Url</label>
          <input type="text"
            className="tag-edit__input"
            value={this.props.tag.podcastMetadata.iTunesUrl || ''}
            onChange={this.updateiTunesUrl.bind(this)}
            disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <input type="checkbox"
            onChange={this.updateiTunesBlock.bind(this)}
            checked={this.props.tag.podcastMetadata.iTunesBlock || false}
            disabled={!this.props.tagEditable}/>
          <label className="tag-edit__label"> Block from iTunes</label>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Google Podcasts Url</label>
          <input type="text"
                 className="tag-edit__input"
                 value={this.props.tag.podcastMetadata.googlePodcastsUrl || ''}
                 onChange={this.updateGooglePodcastsUrl.bind(this)}
                 disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Spotify Url</label>
          <input type="text"
                 className="tag-edit__input"
                 value={this.props.tag.podcastMetadata.spotifyUrl || ''}
                 onChange={this.updateSpotifyUrl.bind(this)}
                 disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Pocket Casts Url</label>
          <input type="text"
                 className="tag-edit__input"
                 value={this.props.tag.podcastMetadata.pocketCastsUrl || ''}
                 onChange={this.updatePocketCastsUrl.bind(this)}
                 disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Acast ID</label>
          <input type="text"
                 className="tag-edit__input"
                 value={this.props.tag.podcastMetadata.acastId || ''}
                 onChange={this.updateAcastId.bind(this)}
                 disabled={!this.props.tagEditable}/>
        </div>
        <div className="tag-edit__field">
          <input type="checkbox"
            onChange={this.updateClean.bind(this)}
            checked={this.props.tag.podcastMetadata.clean || false}
            disabled={!this.props.tagEditable}/>
          <label className="tag-edit__label"> Clean</label>
        </div>
        <div className="tag-edit__field">
          <input type="checkbox"
            onChange={this.updateExplicit.bind(this)}
            checked={this.props.tag.podcastMetadata.explicit || false}
            disabled={!this.props.tagEditable}/>
          <label className="tag-edit__label"> Explicit</label>
        </div>
        <TagImageEdit
          tagImage={this.props.tag.podcastMetadata.image}
          label="Podcast Image"
          onChange={this.updateImage.bind(this)}
          tagEditable={this.props.tagEditable}/>
      </div>
    );
  }

  tagHasPodcast() {
    return !!this.props.tag.podcastMetadata;
  }

  render () {

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Podcast Metadata</label>
        <div className="tag-edit__field">
          <input
            type="checkbox"
            checked={this.tagHasPodcast()}
            onChange={this.togglePodcast.bind(this)}
            disabled={!this.props.tagEditable}/>
          <label className="tag-edit__label"> Is this series a podcast?</label>
        </div>
        {this.renderMetadataForm()}
      </div>
    );
  }
}
