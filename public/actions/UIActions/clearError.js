export const CLEAR_ERROR = 'CLEAR_ERROR';

export function clearError() {
    return {
        type:       CLEAR_ERROR,
        receivedAt: Date.now()
    };
}
