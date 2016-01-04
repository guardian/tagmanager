import React from 'react';

export default class Unauthorised extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="editor">
                <h2>Unauthorised</h2>
                You don't have permission to access this page, please contact central production.
            </div>
        );
    }
}
