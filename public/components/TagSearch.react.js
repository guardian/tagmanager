import React from 'react';
import reqwest from 'reqwest';
import { Link } from 'react-router';

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
        }).then(function(resp) {
            self.setState({tags: resp});
        }).fail(function(err, msg){
            console.log('failed', err, msg);
        });
        this.setState({searchString: event.target.value});
    }

    render () {

        var tagsList = this.state.tags.map(function(t) {
            return(
                <li className="search-suggestions__suggestion" key={t.id}><Link to={`/tag/${t.id}`}>{t.internalName}</Link></li>
            );
        });

        return (
            <div className="search">
                <h2>Tag search.</h2>
                <div className="search-suggester">
                    <form>
                        <input className="search-suggester__field" type="text" value={this.state.searchString} onChange={this.handleChange.bind(this)} />
                    </form>
                    <div className="search-suggestions">
                        <ul className="search-suggestions__list">{tagsList}</ul>
                    </div>
                </div>
                {this.props.children}
            </div>
        );
    }
}
