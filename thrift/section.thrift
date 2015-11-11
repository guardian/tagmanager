namespace scala com.gu.tagmanagement

struct EditionalisedPage {

    /** the path for the editionalised front */
    1: required string path;

    /** the pathmanager assigned id for this editionalised front */
    2: required i64 pageId;
}

struct Section {

    /** the id of the section */
    1: required i64 id;

    /** the id of the primary tag for this section */
    2: required i64 sectionTagId;

    /** the display name for the section */
    3: required string name;

    /** the path of the section front, also used as the id in CAPI */
    4: required string path;

    /** the pathmanager assigned id for the front */
    5: required i64 pageId;

    /** the url slug associated with the section, used to generate urls of tags and content in the section */
    6: required string wordsForUrl;

    /** A map of editionalised versions of the section front keyed off the edition, normally empty */
    7: required map<string, EditionalisedPage> editions;
}