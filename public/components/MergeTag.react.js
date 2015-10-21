import React from 'react';

export default class ReactApp extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="editor">
                <h2>Merge tag.</h2>
                {this.props.children}
            </div>
        );
    }
}