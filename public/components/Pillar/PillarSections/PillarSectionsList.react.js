import React from 'react';
import PropTypes from 'prop-types';

export default class PillarSectionsList extends React.Component {
    renderFinalRow() {
        if ((!this.props.actionButton)) {
            return null
        }

        return (
            <tr>
                <td colSpan="2" className="tag-references__addrow">
                    {this.props.actionButton}
                </td>
            </tr>
        )
    }

    render() {
        return (
            <div className="tag-context__item">
                <div className="tag-context__header">{this.props.title}</div>
                <table className={"grid-table pillar-sections " + this.props.tableClassName}>
                    <tbody className="pillar-sections__sections">
                    {this.props.children}
                    {this.renderFinalRow()}
                    </tbody>
                </table>
            </div>
        );
    }
}

PillarSectionsList.propTypes = {
    title: PropTypes.string.isRequired,
    actionButton: PropTypes.node
}
