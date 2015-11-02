#!/usr/bin/env bash
cd public
npm run client-dev-js &
cd ..
AWS_PROFILE=composer JS_ASSET_HOST=https://tagmanager-assets.local.dev-gutools.co.uk/assets/ sbt run
