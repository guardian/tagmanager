import {getStore} from './storeAccessor';

export function hasPermission(permissionName) {
  const store = getStore();

  return !!store.getState().config.permissions[permissionName];
}
