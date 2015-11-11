import React from 'react';
import { Link } from 'react-router';
import tagManagerApi from '../util/tagManagerApi';

export default class TagSearch extends React.Component {

    constructor(props) {
        super(props);
        this.state = {searchString: '', tags: []};
    }

    handleChange(event) {
        var self = this;

        tagManagerApi.searchTags(event.target.value)
        .then(function(resp) {
            self.setState({tags: resp});
        }).fail(function(err, msg) {
            console.log('failed', err, msg);
        });
        this.setState({searchString: event.target.value});
    }

    render () {

        var tagsList = this.state.tags.map(function(t) {
            return (
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
                <Link to="/tag/create">Create a new tag</Link>
            </div>
        );
    }
}
