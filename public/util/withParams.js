import React from 'react';
import { useParams } from 'react-router-dom';

/**
 * Higher-order component that injects react-router v6 useParams()
 * as a `routeParams` prop, allowing class components to access URL params.
 */
export function withParams(Component) {
  return function WithParams(props) {
    const routeParams = useParams();
    return <Component {...props} routeParams={routeParams} />;
  };
}
