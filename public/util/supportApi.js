import Reqwest from 'reqwest';

export function getImageMetadata(imageUrl) {
      return Reqwest({
          url: '/support/image-metadata?imageUrl=' + imageUrl,
          contentType: 'application/json',
          method: 'get'
      });
}
