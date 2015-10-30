#!/usr/bin/env bash

cd public
printf "\nCompiling Client JS... \n\r\n\r"
npm run build-js-dev

cd ..
printf "\nRunning Application... \n\r\n\r"
AWS_PROFILE=composer sbt run
