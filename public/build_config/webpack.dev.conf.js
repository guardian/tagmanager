var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  devtool: 'source-map',
  module: {
    loaders: [
      {
        test:    /\.js$/,
        exclude: /node_modules/,
        loaders: ['babel?presets[]=es2015&presets[]=react&plugins[]=transform-object-assign']
      },
      {
        test: /\.scss$/,
        loader: ExtractTextPlugin.extract('style-loader', 'css-loader?sourceMap!sass-loader?sourceMap')
      }
    ]
  },
  resolveLoader: {
    root: path.join(__dirname, '..', 'node_modules')
  },

  sassLoader: {
    includePaths: [path.resolve(__dirname, '../style')]
  },

  resolve: {
    extensions: ['', '.js', '.jsx', '.json', '.scss']
  },

  plugins: [
    new ExtractTextPlugin('main.css')
  ]
};
