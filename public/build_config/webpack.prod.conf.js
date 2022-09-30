var webpack = require('webpack');
var path = require('path');
var ExtractTextPlugin = require('extract-text-webpack-plugin');

module.exports = {
  bail: true,
  module: {
    loaders: [
      {
        test:    /\.js$/,
        exclude: /node_modules/,
        loaders: ['babel-loader?presets[]=es2015&presets[]=react&plugins[]=transform-object-assign']
      },
      {
        test: /\.scss$/,
        loader: ExtractTextPlugin.extract({ fallback: 'style-loader', use: 'css-loader!sass-loader' })
      },
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract({ fallback: 'style-loader', use: 'css-loader' })
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
  plugins: [
    new ExtractTextPlugin('main.css'),
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': '"production"'
      }
    }),
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      }
    })
  ],

  resolve: {
    modules: [
      path.join(__dirname, "src"),
      "node_modules"
    ],
    extensions: ['.js', '.jsx', '.json']
  }
};
