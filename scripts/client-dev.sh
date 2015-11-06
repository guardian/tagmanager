#!/usr/bin/env bash
cd public
printf "\n\rRemoving compiled css file... \n\r\n\r"
rm main.compiled.css
cd ..
printf "\n\rStarting Webpack Dev Server... \n\r\n\r"
npm run client-dev &
printf "\n\rStarting Play App... \n\r\n\r"
AWS_PROFILE=composer JS_ASSET_HOST=https://tagmanager-assets.local.dev-gutools.co.uk/assets/ sbt run
