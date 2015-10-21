import React from 'react';
import Header from './Header.react';

export default class ReactApp extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="wrapper">
                <Header />
                <div className="editor">
                    <h1>React is running.</h1>
                    {this.props.children}
                </div>
            </div>
        );
    }
}