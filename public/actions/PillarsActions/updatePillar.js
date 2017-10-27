//NOTE: THIS DOESN'T SAVE THE PILLAR, ONLY UPDATE THE CLIENT STATE. USE savePillar TO SAVE

export const PILLAR_UPDATE = 'PILLAR_UPDATE';

export function updatePillar(pillar) {
    return {
        type:       PILLAR_UPDATE,
        pillar:    pillar,
        receivedAt: Date.now()
    };
}
