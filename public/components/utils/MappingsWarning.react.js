import React from 'react';

export default class MappingsWarning extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    if (this.props.capiUsages == undefined) {
      console.error("Missing capiUsages props in MappingsWarnign")
    } else if (this.props.capiUsages > 10000) {
      return (
        <div className="warning-bar-small">
          This tag has over 10,000 uses, changes to the external references could cause slowness. If you want to add an external reference but are concerned, please contact: <code>digitalcms.dev@theguardian.com</code>
        </div>);
      }
    return false;
  }
}
