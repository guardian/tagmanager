var webpack = require('webpack');
var path = require('path');

module.exports = {
  module: {
    loaders: [
      {
        test:    /\.js$/,
        exclude: /node_modules/,
        loaders: ['babel?presets[]=es2015&presets[]=react']
      }
    ]
  },
  resolveLoader: {
    root: path.join(__dirname, '..', 'node_modules')
  },

  resolve: {
    extensions: ['', '.js', '.jsx', '.json']
  }
};
