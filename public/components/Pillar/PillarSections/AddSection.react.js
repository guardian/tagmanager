import React from 'react';
import SectionSelect from '../../utils/SectionSelect.react';

const BLANK_STATE = {
    expanded: false,
    newValue: ''
};

export default class AddSection extends React.Component {

    constructor(props) {
        super(props);

        this.state = BLANK_STATE;
    }

    expand() {
        this.setState({
            expanded: true
        });
    }

    onValueChange(e) {
        this.setState({
            newValue: e.target.value
        });
    }

    onAddSection(e) {
        e.preventDefault();

        if (this.state.newValue) {
            this.props.onAddSection(this.state.newValue);
        }

        this.setState(BLANK_STATE);
    }

    render () {

        if (!this.state.expanded) {
            return (
                <div onClick={this.expand.bind(this)}>
                    <i className="i-plus" /> Add Section
                </div>
            );
        }

        return (
            <div>
                <form onSubmit={this.onAddSection.bind(this)}>
                    <SectionSelect
                        selectedId={this.state.newValue}
                        sections={this.props.sections.filter(s => this.props.pillar.sectionIds.indexOf(s.path) === -1)}
                        isMicrosite={false}
                        onChange={this.onValueChange.bind(this)}
                        usePath={true}
                    />
                    <input type="submit" value="Add" />
                </form>
            </div>
        );
    }
}
