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
                    <nav className="links">
                        <HeaderMenuItem to="/audit">Audit Logs</HeaderMenuItem>
                        <HeaderMenuItem to="/microsite">Microsite Manager</HeaderMenuItem>
                        <HeaderMenuItem to="/section">Section Editor</HeaderMenuItem>
                        <HeaderMenuItem to="/mapping">Mapping Manager</HeaderMenuItem>
                        <HeaderMenuItem to="/batch">Batch tag</HeaderMenuItem>
                        <HeaderMenuItem to="/merge">Merge tag</HeaderMenuItem>
                        <HeaderMenuItem to="/status">Job Status</HeaderMenuItem>
                    </nav>
                </div>
            </header>
        );
    }
}

class HeaderMenuItem extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    return (
      <Link
        to={this.props.to}
        activeClassName="links__item--active"
        className="links__item top-toolbar__item--highlight"
      >{this.props.children}</Link>
    )
  }
}
