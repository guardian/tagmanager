namespace scala com.gu.tagmanagement

struct ImageAsset {

    /** the url of the asset */
    1: required string imageUrl;

    /** the width of the asset */
    2: required i64 width;

    /** the height of the asset */
    3: required i64 height;

    /** the mimeType of the asset */
    4: required string mimeType;
}



struct Image {

    /** a unique ID for the image */
    1: required string imageId;

    /** a list of individual image sizes */
    2: required list<ImageAsset> assets


}