import React from 'react';
import R from 'ramda';

import {podcastCategories} from '../../../../constants/podcastCategories';

export default class PodcastCategorySelect extends React.Component {

  constructor(props) {
    super(props);
  }

  updateMainPodcastCategory(e) {

    const newMainCat = e.target.value;

    console.log(R.omit('categories', this.props.tag.podcastMetadata))

    if (!newMainCat) {
      this.props.updateTag(R.merge(this.props.tag, {
        podcastMetadata: R.omit(['categories'], this.props.tag.podcastMetadata)
      }));

      return;
    }

    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {
        categories: {
          main: e.target.value
        }
      })
    }));
  }

  updateSecondaryPodcastCategory(e) {
    this.props.updateTag(R.merge(this.props.tag, {
      podcastMetadata: R.merge(this.props.tag.podcastMetadata, {
        categories: R.merge(this.props.tag.podcastMetadata.categories, {
          sub: e.target.value
        })
      })
    }));
  }

  tagHasPodcast() {
    return !!this.props.tag.podcastMetadata;
  }

  renderSubCategory() {
    const categories = this.props.tag.podcastMetadata.categories || {};

    if (!categories.main) {
      return false;
    }

    const activeCategory = podcastCategories.filter((cat) => cat.category === categories.main)[0];

    if (!activeCategory || !activeCategory.subCategories) {
      return false;
    }

    return (
      <div className="tag-edit__field">
        <label className="tag-edit__label">Sub Category</label>
        <select value={categories.sub} onChange={this.updateSecondaryPodcastCategory.bind(this)}>
          <option value=""></option>
          {activeCategory.subCategories.map(function(cat) {
            return (
              <option value={cat} key={cat}>{cat}</option>
            );
          })}
        </select>
      </div>
    );
  }

  render () {

    if (!this.tagHasPodcast) {
      return false;
    }

    const categories = this.props.tag.podcastMetadata.categories || {};

    return (
      <div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Main Category</label>
          <select value={categories.main} onChange={this.updateMainPodcastCategory.bind(this)}>
            <option value=""></option>
            {podcastCategories.map(function(cat) {
              return (
                <option value={cat.category} key={cat.category}>{cat.category}</option>
              );
            })}
          </select>
        </div>
        {this.renderSubCategory()}
      </div>
    );
  }
}
