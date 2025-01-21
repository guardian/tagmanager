## Microsites

Microsites are sections and are often used by commercial. Unlike tags there is currently no option to delete a microsite in Tagmanager.

Occasionally there is a request to delete a microsite in order for a redirect to work. Steps to do this:

Tasks for the WebX team:

- Check that other pieces of content don't refer directly to the microsite page. For example, articles containing links.
- Check our github repo for references to the microsite. We occasionally find hardcoded references to tag pages.

Tasks for the CAPI team:

- Check the microsite has no content (including expired) associated to it in the Content API (ask the CAPI team to check Elasticsearch).
- Providing that is okay, delete the microsite directly in Dynamo.
- The microsite will also need to be deleted in Elasticsearch of CAPI.

Tasks for CP:

- The redirect will need to put in place.
