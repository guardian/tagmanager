import React from 'react';

/**
 * Renders the table header and the passed content & action button for the
 * tag reference table list.
 * Child of this element must be a `<tr></tr>` node with `<td>`s matching the
 * number of headers.
 * The `actionButton` parameter can be any node and will be rendered in a
 * cell that spans the whole table.
 */
export default class TagReferenceList extends React.Component {
    renderFinalRow() {
        if ((!this.props.actionButton)) {
            return null
        }

        return (
            <tr>
              <td colSpan={this.props.headers.length} className="tag-references__addrow">
                {this.props.actionButton}
              </td>
            </tr>
        )
    }

    render() {
        return (
            <div className="tag-context__item">
              <div className="tag-context__header">{this.props.title}</div>
              <table className="grid-table tag-references">
                <thead className="tag-references__header">
                  <tr>
                    {this.props.headers.map(h => <th>{h}</th>)}
                  </tr>
                </thead>
                <tbody className="tag-references__references">
                  {this.props.children}
                  {this.renderFinalRow()}
                </tbody>
              </table>
            </div>
        );
    }
}

TagReferenceList.propTypes = {
    title: React.PropTypes.string.isRequired,
    headers: React.PropTypes.arrayOf(React.PropTypes.string).isRequired,
    actionButton: React.PropTypes.node
}
