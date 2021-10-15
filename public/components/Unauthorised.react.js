import React from 'react';

export default class Unauthorised extends React.Component {

    constructor(props) {
        super(props);
    }

    render () {
        return (
            <div className="editor unauthorised">
                <h2>Unauthorised</h2>
                You don't have permission to access this page, please <a href="mailto:central.production@guardian.co.uk">contact central production</a> if you require access.
            </div>
        );
    }
}
