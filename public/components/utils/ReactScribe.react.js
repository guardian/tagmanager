import React from 'react';
import Scribe from 'scribe-editor';
import scribeKeyboardShortcutsPlugin from 'scribe-plugin-keyboard-shortcuts';
import scribePluginToolbar from 'scribe-plugin-toolbar';
import scribePluginLinkPromptCommand from 'scribe-plugin-link-prompt-command';

export default class ReactScribe extends React.Component {

  constructor(props) {
    super(props);

    this.onContentChange = this.onContentChange.bind(this);
  }

  componentDidMount() {
    this.scribe = new Scribe(this.refs.editor);

    this.configureScribe();

    this.scribe.on('content-changed', this.onContentChange);
  }

  componentWillUnmount() {
    this.scribe.off('content-changed', this.onContentChange);
  }

  configureScribe() {

    this.scribe.use(scribePluginLinkPromptCommand());
    this.scribe.use(scribeKeyboardShortcutsPlugin({
      bold: function (event) { return event.metaKey && event.keyCode === 66; }, // b
      italic: function (event) { return event.metaKey && event.keyCode === 73; }, // i
      linkPrompt: function (event) { return event.metaKey && !event.shiftKey && event.keyCode === 75; }, // k
      unlink: function (event) { return event.metaKey && event.shiftKey && event.keyCode === 75; } // shft + k
    }));

    this.scribe.use(scribePluginToolbar(this.refs.toolbar));
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.value !== this.refs.editor.innerHTML;
  }

  onContentChange() {
    const newContent = this.refs.editor.innerHTML;

    if (newContent !== this.props.value) {
      this.props.onChange(newContent);
    }
  }

  render () {
    return (
      <div className={this.props.className}>
        <div ref="toolbar" className={this.props.toolbarClassName}>
          <div data-command-name="bold" className={this.props.toolbarItemClassName}>Bold</div>
          <div data-command-name="italic" className={this.props.toolbarItemClassName}>Italic</div>
          <div data-command-name="linkPrompt" className={this.props.toolbarItemClassName}>Link</div>
          <div data-command-name="unlink" className={this.props.toolbarItemClassName}>Unlink</div>
        </div>
        <div className={this.props.editorClassName} dangerouslySetInnerHTML={{__html: this.props.value}} ref="editor"></div>
      </div>
    );
  }
}
