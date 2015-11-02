import React from 'react';
import reqwest from 'reqwest';

export default class SectionSelect extends React.Component {

    constructor(props) {
        super(props);
        this.state = {sections: []};
    }

    componentDidMount() {
        var self = this;
        reqwest({
            url: '/api/sections',
            method: 'get',
            type: 'json'
        }).then(function(resp) {
            self.setState({sections: resp});
        }).fail(function(err, msg){
            console.log('failed', err, msg);
        });
    }

    render () {

        var sectionOptions = this.state.sections.map(function(s) {
            return(
                <option value={s.id} key={s.id} >{s.name}</option>
            );
        });

        return (
            <select value={this.props.selectedId} onChange={this.props.onChange}>
                {sectionOptions}
            </select>
        );
    }
}