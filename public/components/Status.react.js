import React from 'react';

export default class Status extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="editor">
                <h2>Status.</h2>
                {this.props.children}
            </div>
        );
    }
}