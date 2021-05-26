#!/usr/bin/env bash

set -e

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
ROOT_DIR=$DIR/..

checkNodeVersion() {
  runningNodeVersion=$(node -v)
  requiredNodeVersion=$(cat "$ROOT_DIR/.nvmrc")

  if [ "$runningNodeVersion" != "$requiredNodeVersion" ]; then
    echo -e "ERROR: Using wrong version of Node. Required ${requiredNodeVersion}. Running ${runningNodeVersion}."
    echo -e "  Use a tool such as nvm or fnm to switch versions."
    exit 1
  fi
}

checkNodeVersion
dev-nginx setup-app "$ROOT_DIR/nginx/nginx-mapping.yml"

printf "\n\rSetting up Tag Manager Client Side dependencies... \n\r\n\r"
printf "\n\rInstalling NPM packages... \n\r\n\r"

npm install

printf "\n\Compiling Javascript... \n\r\n\r"

npm run build

printf "\n\Building Icons... \n\r\n\r"

npm run build-icons

printf "\n\rDone.\n\r\n\r"
