const URL_REGEX = /^https:\/\/(static.guim.co.uk\/sys-images\/Guardian|uploads.guim.co.uk)\/.*\.(png|jpg)$/;

export function validateImageUrl(url) {
  return !!url && !!url.match(URL_REGEX);
}
