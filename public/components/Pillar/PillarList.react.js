import React from 'react';
import {Link} from 'react-router';
import { browserHistory } from 'react-router'


class PillarList extends React.Component {

    constructor(props) {
        super(props);
    }

    onPillarClick(pillar) {
        const path = '/pillar/' + pillar.id;
        browserHistory.push(path);
    }

    componentWillMount() {
        if (!this.props.pillars || !this.props.pillars.length) {
            this.props.pillarActions.getPillars();
        }
    }

    renderListItem(pillar) {

        const pillarClickHandler = (e) => {
            e.preventDefault();
            this.onPillarClick(pillar);
        };

        return (
            <a key={pillar.id}  href={'/pillar/' + pillar.id} onClick={pillarClickHandler}>
                <div className="pillarlist__table__row" onClick={pillarClickHandler}>
                    <div className="pillarlist__table__pillarName">{pillar.name}</div>
                    <div className="pillarlist__table__pillarPath">{pillar.path}</div>
                </div>
            </a>
        );
    }

    render () {

        if (!this.props.pillars || !this.props.pillars.length) {
            return (
                <div>Fetching pillars...</div>
            );
        }

        return (
            <div className="pillarlist">
                <div className="tag-search__filters">
                    <Link className="tag-search__create" to="/pillar/create">Create a new Pillar</Link>
                </div>
                <div className="pillarlist__table" >
                    <div className="pillarlist__table__row">
                        <div className="pillarlist__table__pillarName--header">Name</div>
                        <div className="pillarlist__table__pillarPath--header">Path</div>
                    </div>
                    {this.props.pillars.sort((a, b) => a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1).map(this.renderListItem, this)}
                </div>
            </div>

        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getPillars from '../../actions/PillarsActions/getPillars';

function mapStateToProps(state) {
    return {
        pillars: state.pillars,
        config: state.config
    };
}

function mapDispatchToProps(dispatch) {
    return {
        pillarActions: bindActionCreators(Object.assign({}, getPillars), dispatch)
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(PillarList);
