import React from 'react';
import PillarEdit from '../Pillar/PillarEdit.react';
import SaveButton from '../utils/SaveButton.react';

class PillarCreate extends React.Component {

    constructor(props) {
        super(props);

        this.isPillarDirty = this.isPillarDirty.bind(this);

        this.state = {
            pathInUse: false
        };
    }

    componentDidMount() {
        this.props.pillarActions.populateEmptyPillar();
    }

    isPillarDirty() {
        return this.props.saveState === 'SAVE_STATE_DIRTY';
    }

    resetPillar() {
        this.props.pillarActions.populateEmptyPillar();
    }

    savePillar() {
        this.props.pillarActions.createPillar(this.props.pillar);
    }

    render () {

        return (
            <div className="pillar-edit">
                <div className="pillar-edit__column--sidebar">
                    <PillarEdit pillar={this.props.pillar} updatePillar={this.props.pillarActions.updatePillar}/>
                </div>
                <div className="pillar-edit__column"></div>
                <div className="pillar-edit__column"></div>
                <SaveButton isHidden={!this.isPillarDirty()} onSaveClick={this.savePillar.bind(this)} onResetClick={this.resetPillar.bind(this)}/>
            </div>
        );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createPillar from '../../actions/PillarsActions/createPillar';
import * as updatePillar from '../../actions/PillarsActions/updatePillar';

function mapStateToProps(state) {
    return {
        config: state.config,
        saveState: state.saveState,
        pillar: state.pillar
    };
}

function mapDispatchToProps(dispatch) {
    return {
        pillarActions: bindActionCreators(Object.assign({}, createPillar, updatePillar), dispatch)
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(PillarCreate);
