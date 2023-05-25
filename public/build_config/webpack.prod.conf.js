var webpack = require('webpack');
var path = require('path');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = {
  bail: true,
  mode: 'production',
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'app.js'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        use: [{
          loader: "babel-loader",
          options: {
            presets: ["@babel/preset-env", "@babel/preset-react"]
          }
        }],
        exclude: /node_modules/,
      },
      {
        test: /\.css$/,
        use: [MiniCssExtractPlugin.loader, "css-loader"],
      },
      {
        test: /\.scss$/,
        use: [
          MiniCssExtractPlugin.loader,
          {
            loader: "css-loader",
          },
          "sass-loader"
        ]
      },
      {
        test: /\.woff(2)?(\?v=[0-9].[0-9].[0-9])?$/,
        use: [{loader: "url-loader", options: {mimetype: 'application/font-woff'}}]
      },
      {
        test: /\.(ttf|eot|svg|gif)(\?v=[0-9].[0-9].[0-9])?$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: '[name].[ext]',
              outputPath: 'fonts/'
            }
          }
        ]
      }
    ]
  },
  plugins: [
    new MiniCssExtractPlugin({filename: 'main.css'}),
    new webpack.IgnorePlugin({
      resourceRegExp: /^\.\/locale$/,
      contextRegExp: /moment$/,
    }),
  ],

  resolve: {
    modules: [
      path.join(__dirname, "src"),
      "node_modules"
    ],
    extensions: ['.js', '.jsx', '.json']
  }
};
