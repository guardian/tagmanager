#!/usr/bin/env bash

printf "\n\rSetting up Tag Manager Client Side dependancies... \n\r\n\r"

cd public

printf "\n\rInstalling NPM packages... \n\r\n\r"

npm install

printf "\n\rInstalling JSPM packages... \n\r\n\r"

jspm install

cd ..

printf "\n\rDone.\n\r\n\r"