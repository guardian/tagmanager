Tag Manager
===========

Tags are great, they are literally the best way we have of indicating what a piece of content is, what it’s about and
where it came from. We use tags to generate navigation on theguardian.com and provide pages and pages of links to
interesting content. Tags can be mapped which allows them to magically show rich contextual information from external
systems on relevant pages. Everyone loves tags.

The tag manager allows the tags to be ... er ... managed
 
Running
=======

Before you run for the first time you will need to run `scripts/setup.sh` this will install all the frontend dependencies
needed for the app (you may need to install npm before you can run this successfully). If any frontend dependencies
are changed toy should re run the setup script.

The tag manager is a standard play app, fire up sbt and run.

The Nginx setup uses the [dev-nginx](https://github.com/guardian/dev-nginx) tool. after running this the tag manager
will be available on [https://tagmanager.local.dev-gutools.co.uk](https://tagmanager.local.dev-gutools.co.uk)

Developing
==========

The backend code used the standard scala play layout.

The frontend components live in the public directory in root. Css is compiled from sass file in the style directory.