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

    render () {
        var self = this;

        return (
            <header className="top-toolbar">

                <Link to="/" className="top-toolbar__title">
                    <div className="top-toolbar__letter">
                        <span className="top-toolbar__letter-text">T</span>
                    </div>
                    <h1 className="top-toolbar__page-title">
                        <span className="top-toolbar__page-title-text">Tags</span>
                    </h1>
                </Link>

                <div className="header__children">
                    <ul className="links">
                        <li className="links__item top-toolbar__item--highlight">
                            <Link to="/mapping">Mapping Manager</Link>
                        </li>
                        <li className="links__item top-toolbar__item--highlight">
                            <Link to="/batch">Batch tag</Link>
                        </li>
                        <li className="links__item top-toolbar__item--highlight">
                            <Link to="/merge">Merge tag</Link>
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
