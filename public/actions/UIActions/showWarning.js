export const SHOW_WARNING = 'SHOW_WARNING';

export function showWarning(message) {
    return {
        type:       SHOW_WARNING,
        message:    message,
        receivedAt: Date.now()
    };
}
