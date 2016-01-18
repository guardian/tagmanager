export const SHOW_ERROR = 'SHOW_ERROR';

export function showError(message) {
    return {
        type:       SHOW_ERROR,
        message:    message,
        receivedAt: Date.now()
    };
}
