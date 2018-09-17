Tag Manager
===========

Tags are great, they are literally the best way we have of indicating what a piece of content is, what it’s about and
where it came from. We use tags to generate navigation on theguardian.com and provide pages and pages of links to
interesting content. Tags can be mapped which allows them to magically show rich contextual information from external
systems on relevant pages. Everyone loves tags.

The tag manager allows the tags to be ... er ... managed
 
Running
=======

Before you run for the first time you will need to run `./scripts/setup.sh` this will install and compile all the frontend
dependencies needed for the app (you may need to install npm before you can run this successfully). If any frontend
dependencies are changed you should re run the setup script.

The Nginx setup uses the [dev-nginx](https://github.com/guardian/dev-nginx) tool. After running this the tag manager
will be available on [https://tagmanager.local.dev-gutools.co.uk](https://tagmanager.local.dev-gutools.co.uk)

To read the database and interact with the AWS infrastructure you will need AWS credentials that allow you access to the
Composer account available to the default credentials provider chain. The easiest way to configure this is to obtain the
credentials via [janus](https://janus.gutools.co.uk/) and export the credentials to your shell using the handy script provided by janus.

Tag Manager also makes requests to CAPI preview. This means you will also need CAPI credentials from janus (with 'API Gateway invocation' permission).

If you are working on the commercial tag functionality and need to be able to upload logos you will also need AWS credentials for 
the frontend aws account available as an AWS profile named "frontend". This can also be obtained via janus.

The tag manager is a standard play app, fire up sbt and run.

By default if you change any frontend code, you will need to recompile the assets using `./scripts/setup.sh` but there
are alternatives:

Client Side Development
=======================

We use webpack to compile the assets for this project. You have the option to run `./scripts/setup.sh` after each change
as mentioned above, or alternatively you can choose to use one of the alternative startup scripts provided

1. `./scripts/start.sh` This starts a webpack watcher in addition to running the application - The watcher will compile
unminified code when it detects a change to the javascript. Refresh the webpage to see the new code.
2. `./scripts/client-dev.sh` This scripts starts a [webpack-dev-server](https://webpack.github.io/docs/webpack-dev-server.html)
alongside the application which provides Hot Reloading. Changes you make to code should be immediately visible in the browser

Developing
==========

The backend code used the standard scala play layout.

The frontend components live in the public directory in root. Css is compiled from sass file in the style directory.
