include "image.thrift"


namespace scala com.gu.tagmanagement

/** the types of tags supported */
enum TagType {
    TOPIC = 0,
    CONTRIBUTOR = 1,
    SERIES = 2,
    TONE = 3,
    CONTENT_TYPE = 4,
    PUBLICATION = 5,
    NEWSPAPER_BOOK = 6,
    NEWSPAPER_BOOK_SECTION = 7
}

struct PodcastMetadata {
    /** The iTunes link URL **/
    1: required string linkUrl;

    /** The iTunes copyright text **/
    2: optional string copyrightText;

    /** The iTunes author text **/
    3: optional string authorText;

    /** The iTunes url for the podcast **/
    4: optional string iTunesUrl;

    /** Should the podcast appear in iTunes **/
    5: required bool iTunesBlock;

    /** Should the podcast be marked as clean in iTunes **/
    6: required bool clean;

    /** Should the podcast be marked as explicit in iTunes **/
    7: required bool explicit;

    /** iTunes podcast image **/
    8: optional image.Image image

}

struct ContributorInformation {
    /** Any RCS id associated with the tag */
    1: optional string rcsId;

    /** Any byline image associated with the tag   */
    2: optional image.Image bylineImage;

    /** Any Large byline image associated with the tag */
    3: optional image.Image largeBylineImage;

    /** Twitter Handle associated with the tag */
    4: optional string twitterHandle;

    /** Contact Email for contributors */
    5: optional string contactEmail;

}


struct Reference {
    /** the type of the the reference, e.g. musicbrainz, imdb, pa football team etc. */
    1: required string type;

    /** the value to the reference */
    2: required string value;
}

struct Tag {

    /** the id of the tag */
    1: required i64 id;

    /** the path of the canonical page for this tag - also used as api identifier*/
    2: required string path;

    /** the page id for the path as asigned by the path manager */
    3: required i64 pageId;

    /** the type of this tag */
    4: required TagType type;

    /** the internal label for the tag */
    5: required string internalName;

    /** the publicly displayed name for the tag */
    6: required string externalName;

    /** the tag owned url slug, the path is derived from this, the section and the tag type */
    7: required string slug;

    /** the tag is not displayed on the site if hidden is true */
    8: required bool hidden;

    /** legally sensitive tags surpress content showing related content and showing up in related content results */
    9: required bool legallySensitive;

    /** The natural sort key used for ordering tag lists, puts surname first, removes 'the' etc. */
    10: required string comparableValue;

    /** the id of the section this tag belongs to, if missing the tag is in the 'Global' pseudosection */
    11: optional i64 section;

    /** a description of the tag, rendered on tag pages, this could be a topic precis or a contributor's profile */
    12: optional string description;

    /** a set of parent tag ids. NB a tag can have multiple parents, but more often the set will be empty */
    13: required set<i64> parents;

    /** the reference mappings for this tag */
    14: required list<Reference> references;

    /** Any Podcast Metadata associated with this tag */
    15: optional PodcastMetadata podcastMetadata;

    /** Any Contributor Information associated with this tag */
    16: optional ContributorInformation contributorInformation;

}
