import React from 'react';

export default class ReactApp extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="container">
                <h1>React is running.</h1>
                {this.props.children}
            </div>
        );
    }
}