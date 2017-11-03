import React from 'react';
import PillarEdit from './PillarEdit.react';
import PillarSections from './PillarSections/PillarSections.react';
import SaveButton from '../utils/SaveButton.react';
import ConfirmButton from '../utils/ConfirmButton.react';

class PillarDisplay extends React.Component {

    constructor(props) {
        super(props);

        this.isPillarDirty = this.isPillarDirty.bind(this);
    }

    componentDidMount() {
        if (!this.props.pillar || this.props.pillar.id !== parseInt(this.props.routeParams.pillarId, 10)) {
            this.props.pillarActions.getPillar(this.props.routeParams.pillarId);
        }

        if (!this.props.sections || !this.props.sections.length) {
            this.props.sectionActions.getSections();
        }
    }

    isPillarDirty() {
        return this.props.saveState === 'SAVE_STATE_DIRTY';
    }

    resetPillar() {
        this.props.pillarActions.getPillar(this.props.routeParams.pillarId);
    }

    savePillar() {
        this.props.pillarActions.savePillar(this.props.pillar);
    }

    deletePillar() {
        this.props.pillarActions.deletePillar(this.props.pillar);
    }

    render () {

        if (!this.props.pillar || this.props.pillar.id !== parseInt(this.props.routeParams.pillarId, 10)) {
            return (
                <div>Fetching pillar...</div>
            );
        }

        return (
            <div className="pillar-edit">
                <div className="pillar-edit__column--sidebar">
                    <PillarEdit pillar={this.props.pillar} updatePillar={this.props.pillarActions.updatePillar} pathLocked={true} />
                    <div>
                        <ConfirmButton className="pillar__delete" onClick={this.deletePillar.bind(this)} buttonText="Delete Pillar" />
                    </div>
                </div>
                <div className="pillar-edit__column">
                    <PillarSections sections={this.props.sections} pillar={this.props.pillar} updatePillar={this.props.pillarActions.updatePillar}/>
                </div>
                <div className="pillar-edit__column"></div>
                <SaveButton isHidden={!this.isPillarDirty()} onSaveClick={this.savePillar.bind(this)} onResetClick={this.resetPillar.bind(this)}/>
            </div>
        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getPillar from '../../actions/PillarsActions/getPillar';
import * as updatePillar from '../../actions/PillarsActions/updatePillar';
import * as savePillar from '../../actions/PillarsActions/savePillar';
import * as deletePillar from '../../actions/PillarsActions/deletePillar';
import * as showError from '../../actions/UIActions/showError';
import * as getSections from '../../actions/SectionsActions/getSections';

function mapStateToProps(state) {
    return {
        pillar: state.pillar,
        config: state.config,
        saveState: state.saveState,
        sections: state.sections
    };
}

function mapDispatchToProps(dispatch) {
    return {
        pillarActions: bindActionCreators(Object.assign({}, getPillar, updatePillar, savePillar, deletePillar), dispatch),
        sectionActions: bindActionCreators(Object.assign({}, getSections), dispatch),
        uiActions: bindActionCreators(Object.assign({}, showError), dispatch)
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(PillarDisplay);
