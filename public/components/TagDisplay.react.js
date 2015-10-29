import React from 'react';

class TagDisplay extends React.Component {

    constructor(props) {
        super(props);
    }

    componentDidMount() {
      if (!this.props.tag) {
        this.props.tagActions.getTag(this.props.routeParams.tagId);
      }
    }

    render () {
      if (!this.props.tag) {
        return (
          <div>Fetching Tag</div>
        );
      }

      return (
        <div className="tag">
          <h2>Tag display for tag {this.props.tag.internalName}.</h2>
        </div>
      );
    }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as tagActionCreators from '../actions/tagActions';

function mapStateToProps(state) {
  return { tag: state.tag };
}

function mapDispatchToProps(dispatch) {
  return { tagActions: bindActionCreators(tagActionCreators, dispatch) };
}

export default connect(mapStateToProps, mapDispatchToProps)(TagDisplay);
