import React from 'react';

export default class TagDisplay extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="editor">
                <h2>Tag display.</h2>
                {this.props.children}
            </div>
        );
    }
}