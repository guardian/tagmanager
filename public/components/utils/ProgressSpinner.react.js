import React from 'react';

export default class ProgressSpinner extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (<div>
                    <i className="spinner" />
                </div>);
    }

}
