#!/usr/bin/env bash
cd public
npm run build-js-dev &
cd ..
AWS_PROFILE=composer sbt run
