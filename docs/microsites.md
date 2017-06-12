## Microsites

Are sections and are often used by commercial. Unlike tags there is 
currently no option to delete a microsite in Tagmanager. 

Occasionally there is a request to delete a microsite in order for a redirect
to work. Steps to do this:

* Check the microsite has no content (including expired) associated to it in the
 Content API (ask the content-platform team to check Elasticsearch).
* Providing that is okay delete the microsite directly in Dynamo. 
* The microsite will also need to be deleted in Elasticsearch of CAPI.
* The redirect will need to put in place (Dotcom does this).