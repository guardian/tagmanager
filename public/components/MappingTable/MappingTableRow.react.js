import React from 'react';
import {Link} from 'react-router';

export default class MappingTableRow extends React.Component {

    constructor(props) {
        super(props);
    }

    deleteMapping(e) {
      this.props.deleteMapping(this.props.tag, this.props.reference);
    }

    updateMapping(e) {
      this.props.updateMapping(this.props.tag, this.props.reference, e.target.value);
    }

    saveTag() {
      this.props.saveTag(this.props.tag);
    }

    render() {
      return (
        <tr>
          <td>
            {this.props.tag.internalName}
            <Link to={'/tag/' + this.props.tag.id}>
              <i className="i-preview-eye"/>
            </Link>
          </td>
          <td>
            <input value={this.props.reference.value} onChange={this.updateMapping.bind(this)}/>
            <input type="submit" onClick={this.saveTag.bind(this)} />
          </td>
          <td style={{textAlign: 'center'}}>
            <i className="i-delete" onClick={this.deleteMapping.bind(this)}/>
          </td>
        </tr>
      );
    }
}
