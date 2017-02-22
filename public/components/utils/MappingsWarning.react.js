import React from 'react';

export default class MappingsWarning extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    if (this.props.capiUsages == undefined) {
      console.error("Missing capiUsages props in MappingsWarning")
    } else if (this.props.capiUsages > 10000) {
      return (
        <div className="warning-bar-small">
        This tag has {this.props.capiUsages.toLocaleString()} uses, mapping changes could cause high demand on the tag management infrastructure. If you want to add an external reference but are concerned, please contact: <code>digitalcms.dev@theguardian.com</code>
        </div>);
      }
    return false;
  }
}
