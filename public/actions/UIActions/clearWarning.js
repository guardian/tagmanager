export const CLEAR_WARNING = 'CLEAR_WARNING';

export function clearWarning() {
    return {
        type:       CLEAR_WARNING,
        receivedAt: Date.now()
    };
}

