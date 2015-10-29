export const TAG_SAVE = 'TAG_SAVE';

export function saveTag(tag) {
  return {
    type:       TAG_SAVE,
    tag:        tag,
    receivedAt: Date.now()
  };
}
