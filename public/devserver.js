//
// Imports
//

var webpack          = require('webpack');
var WebpackDevServer = require('webpack-dev-server');
var wpConfig         = require('./build_config/webpack.devserver.conf.js');

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

wpServer.listen(wpConfig.port, wpConfig.addr, function() {
    console.log('WebpackDevServer listening on port %d', wpConfig.port);
});
