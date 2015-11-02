var webpack = require('webpack');
var path = require('path');

module.exports = {
  module: {
    loaders: [
      {
        test:    /\.js$/,
        exclude: /node_modules/,
        loaders: ['babel-loader']
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
