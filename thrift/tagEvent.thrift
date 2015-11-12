include "tag.thrift"

namespace scala com.gu.tagmanagement

enum EventType {
    UPDATE = 0,
    DELETE = 1
}

struct TagEvent {
    /** the type of this event */
    1: required EventType eventType;

    /** the tag's id */
    2: required i64 tagId;

    /** the full representation of the tag's state, missing if the tag is deleted */
    3: optional tag.Tag tag;
}