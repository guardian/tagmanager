import React from "react";

export default class TagAdBlocking extends React.Component {
  constructor(props) {
    super(props);
  }

  setAdBlockingLevel(e) {
    this.props.updateTag(
      Object.assign({}, this.props.tag, {
        adBlockingLevel: e.target.value
      })
    );
  }

  setContributionBlockingLevel(e) {
    this.props.updateTag(
      Object.assign({}, this.props.tag, {
        contributionBlockingLevel: e.target.value
      })
    );
  }

  render() {
    const blockingLevels = ["None", "Suggest", "Force"];
    const renderBlockingLevels = (field, onChange) => (
      <select onChange={onChange} value={this.props.tag[field]}>
        {blockingLevels.map(t => {
          const optionValue = t.toUpperCase();
          return (
            <option key={t} value={optionValue}>
              {t}
            </option>
          );
        })}
        )
      </select>
    );

    return (
      <div className="tag-edit__input-group">
        <label className="tag-edit__input-group__header">Ad Blocking</label>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Block Ads</label>
          {renderBlockingLevels(
            "adBlockingLevel",
            this.setAdBlockingLevel.bind(this)
          )}
        </div>
        <div className="tag-edit__field">
          <label className="tag-edit__label">Block Contribution Asks</label>
          {renderBlockingLevels(
            "contributionBlockingLevel",
            this.setContributionBlockingLevel.bind(this)
          )}
        </div>
      </div>
    );
  }
}
