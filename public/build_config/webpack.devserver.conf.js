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
    port:  port,
    addr:  addr,
    host:  host,
    entry: {
        app: [
            'webpack-dev-server/client?' + host,
            'webpack/hot/dev-server',
            path.join(__dirname, '..', 'app.js')
        ]
    },
    output: {
        path:       path.join(__dirname, '..'),
        publicPath: host + '/assets/',
        filename:   'app.compiled.js'
    },
    resolveLoader: {
        modulesDirectories: ['node_modules']
    },
    module: {
        loaders: [
            {
                test:    /\.jsx?$/,
                exclude: /node_modules/,
                loaders: ['react-hot', 'babel?presets[]=es2015&presets[]=react&plugins[]=transform-object-assign']
            },
            {
                test:   require.resolve('react'),
                loader: 'expose?React'
            },
            {
                test: /\.scss$/,
                loaders: ['style', 'css', 'sass']
            }
        ]
    },
    plugins: [
        new webpack.HotModuleReplacementPlugin()
    ],
    resolve: {
        // Allows require('file') instead of require('file.js|x')
        extensions: ['', '.js', '.jsx', '.json']
    }
};
