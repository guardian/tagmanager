include "section.thrift"

namespace scala com.gu.tagmanagement

enum EventType {
    UPDATE = 0,
    DELETE = 1
}

struct SectionEvent {
    /** the type of this event */
    1: required EventType eventType;

    /** the tag's id */
    2: required i64 sectionId;

    /** the full representation of the section's state */
    3: optional section.Section section;
}