#!/usr/bin/env bash
set -e

npm config set registry https://registry.npmjs.org
npm install
npm run build
npm run build-icons