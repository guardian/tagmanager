import React from 'react';
import { NavLink } from 'react-router-dom';

export default class Header extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <header className="top-toolbar">
                <div className="top-toolbar-background"/>
                <Link to="/" className="home-logo">
                    <span className="home-logo__text-large">Tags</span>
                    <span className="home-logo__text-small">home</span>
                </Link>

                <div className="header__children">
                    <nav className="links">
                        <HeaderMenuItem to="/audit">Audit Logs</HeaderMenuItem>
                        <HeaderMenuItem to="/sponsorship">Commercial</HeaderMenuItem>
                        <HeaderMenuItem to="/microsite">Microsite Manager</HeaderMenuItem>
                        <HeaderMenuItem to="/section">Section Editor</HeaderMenuItem>
                        { /* <HeaderMenuItem to="/pillar">Pillar Editor</HeaderMenuItem> */ }
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
      <NavLink
        to={this.props.to}
        className={({ isActive }) =>
          isActive
            ? 'links__item top-toolbar__item--highlight links__item--active'
            : 'links__item top-toolbar__item--highlight'
        }
      >{this.props.children}</NavLink>
    )
  }
}
