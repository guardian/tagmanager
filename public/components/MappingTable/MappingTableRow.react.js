import React from 'react';
import {Link} from 'react-router';

export default class MappingTableRow extends React.Component {

    constructor(props) {
        super(props);
    }

    deleteMapping(e) {
      this.props.deleteMapping(this.props.tagId, this.props.referenceType, this.props.referenceValue);
    }

    updateMapping(e) {
      this.props.updateMapping(this.props.tagId, this.props.referenceType, this.props.referenceValue, e.target.value);
    }

    saveTag() {
      this.props.saveTag(this.props.tagId);
    }

    render() {
      return (
        <tr>
          <td>
            {this.props.tagInternalName}
            <Link to={'/tag/' + this.props.tagId}>
              <i className="i-preview-eye"/>
            </Link>
          </td>
          <td>
            <input value={this.props.referenceValue} onChange={this.updateMapping.bind(this)}/>
            <input type="submit" onClick={this.saveTag.bind(this)} />
          </td>
          <td style={{textAlign: 'center'}}>
            <i className="i-delete" onClick={this.deleteMapping.bind(this)}/>
          </td>
        </tr>
      );
    }
}
