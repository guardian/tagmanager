import React from 'react';
import reqwest from 'reqwest';

export default class TagSearch extends React.Component {

    constructor(props) {
        super(props);
        this.state = {searchString: '', tags: []};
    }

    handleChange(event) {
        var self = this;

        reqwest({
            url: '/api/tags',
            method: 'get',
            data: [ {name: 'q', value: event.target.value} ],
            type: 'json'
        }).then(function(resp){
            console.log(resp);
            self.setState({tags: resp});
        }).fail(function(err, msg){
            console.log('failed', err, msg);
        });
        this.setState({searchString: event.target.value});
    }

    render () {

        var tagsList = this.state.tags.map(function(t) {
            return(
                <ul>
                    <li>{t.internalName}</li>
                </ul>
            );
        });

        return (
            <div className="editor">
                <h2>Tag search.</h2>
                <form>
                    <input type="text" value={this.state.searchString} onChange={this.handleChange.bind(this)} />
                </form>
                <p>search text is {this.state.searchString}</p>
                {tagsList}
                {this.props.children}
            </div>
        );
    }
}