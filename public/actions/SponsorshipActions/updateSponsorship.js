//NOTE: THIS DOESN'T SAVE THE SPONSORSHIP, ONLY UPDATE THE CLIENT STATE. USE saveSponsorship TO SAVE

export const SPONSORSHIP_UPDATE = 'SPONSORSHIP_UPDATE';

export function updateSponsorship(sponsorship) {
    return {
        type:         SPONSORSHIP_UPDATE,
        sponsorship:  sponsorship,
        receivedAt:   Date.now()
    };
}
