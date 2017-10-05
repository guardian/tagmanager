const URL_REGEX = /^https:\/\/(static.guim.co.uk\/sys-images|uploads.guim.co.uk)\/.*\.(png|jpg|jpeg|PNG|JPG|JPEG)$/;

export function validateImageUrl(url) {
  return !!url && !!url.match(URL_REGEX);
}
