var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  devtool: 'source-map',
  module: {
    loaders: [
      {
        test:    /\.js$/,
        exclude: /node_modules/,
        loaders: ['babel-loader?presets[]=es2015&presets[]=react&plugins[]=transform-object-assign']
      },
      {
        test: /\.scss$/,
        loader: ExtractTextPlugin.extract('style-loader', 'css-loader?sourceMap!sass-loader?sourceMap')
      },
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract('style-loader', 'css-loader?sourceMap')
      },
      {
        test: /\.woff(2)?(\?v=[0-9].[0-9].[0-9])?$/,
        loader: "url-loader?mimetype=application/font-woff"
      },
      {
        test: /\.(ttf|eot|svg|gif)(\?v=[0-9].[0-9].[0-9])?$/,
        loader: "file-loader?name=[name].[ext]"
      }
    ]
  },

  sassLoader: {
    includePaths: [path.resolve(__dirname, '../style')]
  },

  resolve: {
    modules: [
        path.join(__dirname, "src"),
        "node_modules"
    ],
    // Allows require('file') instead of require('file.js|x')
    extensions: ['.js', '.jsx', '.json']
  },

  plugins: [
    new ExtractTextPlugin('main.css')
  ]
};
