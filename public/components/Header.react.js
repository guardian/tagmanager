import React from 'react';
import { Link } from 'react-router';

export default class Header extends React.Component {

    constructor(props) {
        super(props);
    }

    userName () {
        if (this.props.user) {
            return this.props.user.firstName + ' ' + this.props.user.lastName;
        }
    }

    renderTitleHoverState() {
        if (this.props.suppressTitleLink) {
            return false;
        }

        return (
            <div className="top-toolbar__title__hover-state">
                <span className="top-toolbar__title__hover-state__subtitle">Back to</span><br />
                <span className="top-toolbar__title__hover-state__title">Dashboard</span>
            </div>
        );
    }

    render () {
        var self = this;

        return (
            <header className="top-toolbar">

                <Link to="/" className="top-toolbar__title">
                    <div className="top-toolbar__letter">
                        <span className="top-toolbar__letter-text">T</span>
                    </div>
                    <h1 className="top-toolbar__page-title">
                        <span className="top-toolbar__page-title-text">Tag manager</span>
                    </h1>
                    {this.renderTitleHoverState()}
                </Link>

                <div className="header__children">
                    <ul className="links">
                        <li className="links__item top-toolbar__item--highlight">
                            <Link to="/batchTag">Batch tag</Link>
                        </li>
                        <li className="links__item top-toolbar__item--highlight">
                            <Link to="/mergeTag">Merge tag</Link>
                        </li>
                        <li className="links__item top-toolbar__item--highlight">
                            <Link to="/status">Status</Link>
                        </li>
                    </ul>
                </div>

                <div className="top-toolbar__right-align">
                    <div className="top-toolbar__user top-toolbar__item">
                        <span className="user__name">{self.userName()}<i className="i-down-arrow"/></span>
                    </div>
                </div>
            </header>
        );
    }
}