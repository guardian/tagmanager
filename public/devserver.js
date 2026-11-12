//
// Imports
//

var webpack          = require('webpack');
var WebpackDevServer = require('webpack-dev-server');
var wpConfig         = require('./build_config/webpack.devserver.conf.js');

var port = 8248;
var addr = 'tagmanager-assets.local.dev-gutools.co.uk';
var host = 'https://' + addr;

//
// Webpack
//

var compiler = webpack(wpConfig);
var wpServer = new WebpackDevServer({
  host:         addr,
  port:         port,
  allowedHosts: 'all',
  static:       {
    directory: wpConfig.output.path
  },
  devMiddleware: {
    publicPath: '/assets/build/'
  },
  hot:        true,
  watchFiles: {
    paths: ['public/**/*'],
    options: {
      aggregateTimeout: 300,
      poll:             1000
    }
  },
  headers: {
    'X-Custom-Header': 'yes',
    'Access-Control-Allow-Origin' : '*'
  },
  client: {
    progress: true
  }
}, compiler);

//
// Exports
//

//wpServer.use('/public', express.static('public'));

wpServer.startCallback(function() {
    console.log('WebpackDevServer listening on port %d', port);
});
