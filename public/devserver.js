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

var wpServer = new WebpackDevServer(webpack(wpConfig), {
  contentBase:  wpConfig.output.path,
  publicPath:   '/assets/build/',
  hot:          true,
  progress:     true,
  noInfo:       true,
  watchOptions: {
    aggregateTimeout: 300,
    poll:             1000
  },
  quiet:   false,
  headers: {
    'X-Custom-Header': 'yes',
    'Access-Control-Allow-Origin' : '*'
  },
  stats:   {
    colors: true
  }
});

//
// Exports
//

//wpServer.use('/public', express.static('public'));

wpServer.listen(port, addr, function() {
    console.log('WebpackDevServer listening on port %d', port);
});
