#!/usr/bin/env bash
npm run build-dev &
AWS_PROFILE=composer sbt run
