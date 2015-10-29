//NOTE: THIS DOESN'T SAVE THE TAG, ONLY UPDATE THE CLIENT STATE. USE saveTag TO SAVE

export const TAG_UPDATE = 'TAG_UPDATE';

export function updateTag(tag) {
    return {
        type:       TAG_UPDATE,
        tag:        tag,
        receivedAt: Date.now()
    };
}
