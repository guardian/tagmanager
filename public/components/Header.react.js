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
                            <Link to="/audit">Audit Logs</Link>
                        </li>
                        <li className="links__item top-toolbar__item--highlight">
                            <Link to="/microsite">Microsite Manager</Link>
                        </li>
                        <li className="links__item top-toolbar__item--highlight">
                            <Link to="/section">Section Editor</Link>
                        </li>
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
                            <Link to="/status">Job Status</Link>
                        </li>
                    </ul>
                </div>
            </header>
        );
    }
}
