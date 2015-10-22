import React from 'react';

export default class BatchTag extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="editor">
                <h2>batch tag.</h2>
                {this.props.children}
            </div>
        );
    }
}