//NOTE: THIS DOESN'T SAVE THE SECTION, ONLY UPDATE THE CLIENT STATE. USE saveSection TO SAVE

export const SECTION_UPDATE = 'SECTION_UPDATE';

export function updateSection(section) {
    return {
        type:       SECTION_UPDATE,
        section:    section,
        receivedAt: Date.now()
    };
}
