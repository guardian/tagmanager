#!/usr/bin/env bash
cd public
npm run client-dev-js &
cd ..
AWS_PROFILE=composer sbt run
