import React from 'react';

export default class PageNavigator extends React.Component {
    constructor(props) {
        super(props);
    }

    renderPageButton(number) {
        if (number == this.props.currentPage) {
            return (<span key={"page_" + number} className="page-navigator__selected">{number}</span>);
        }
        return (<span key={"page_" + number} className="page-navigator__page" onClick={this.props.pageSelectCallback.bind(this, number)}>{number}</span>);
    }

    renderStartElipsis(firstDisplayedPage, lastPage) {
        if (firstDisplayedPage > 2 && firstDisplayedPage < lastPage) {
            return (<span>...</span>);
        }
        return false;
    }

    renderEndElipsis(lastDisplayedPage, lastPage) {
        if (lastDisplayedPage < lastPage) {
            return (<span> ... </span>);
        }
        return false;
    }

    renderPageSpan(firstDisplayedPage, lastDisplayedPage) {
        var spans = [];
        for (var i = firstDisplayedPage; i <= lastDisplayedPage; i++) {
            spans.push(this.renderPageButton(i));
        }
        return spans;
    }

    render() {
        var firstDisplayedPage = Math.max(2, this.props.currentPage - this.props.pageSpan);
        var lastDisplayedPage = Math.min(this.props.lastPage, firstDisplayedPage + this.props.pageSpan * 2);
        return (<div className="page-navigator">
                    {this.renderPageButton(1)}

                    {this.renderStartElipsis(firstDisplayedPage, this.props.lastPage)}

                    {this.renderPageSpan(firstDisplayedPage, lastDisplayedPage)}

                    {this.renderEndElipsis(lastDisplayedPage, this.props.lastPage)}
                </div>);
    }
}
