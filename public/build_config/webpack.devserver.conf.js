//
// Imports
//

var path    = require('path');
var webpack = require('webpack');

//
// Config
//

var port = 8248;
var addr = 'tagmanager-assets.local.dev-gutools.co.uk';
var host = 'https://' + addr;

//
// Exports
//

module.exports = {
    entry: {
        app: [
            'webpack-dev-server/client?' + host,
            'webpack/hot/dev-server',
            path.join(__dirname, '..', 'app.js')
        ]
    },
    output: {
        path:       path.join(__dirname, '..'),
        publicPath: host + '/assets/build/',
        filename:   'app.js'
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: [{ loader: 'babel-loader' }]
            },
            { 
                test: require.resolve('react'), 
                use: [{ loader: 'expose-loader', options: 'React' }]},
            {
              test:    /\.js$/,
              use: [{
                loader: "babel-loader",
                options: {
                  presets: ["@babel/preset-env", "@babel/preset-react"]
                }
              }],
              exclude: /node_modules/,
            },
            {
                test: /\.scss$/,
                loaders: ['style-loader', 'css-loader', 'sass-loader']
            },
            {
                test: /\.css$/,
                loaders: ['style-loader', 'css-loader']
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
        new webpack.HotModuleReplacementPlugin()
    ],
    resolve: {
        modules: [
            path.join(__dirname, "src"),
            "node_modules"
        ],
        // Allows require('file') instead of require('file.js|x')
        extensions: ['.js', '.jsx', '.json']
    }
};
