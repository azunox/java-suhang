#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
# Start the application separately on port 18081 before running this script.
npm exec --yes --package=newman -- newman run \
  docs/postman/teamnote-member-a.postman_collection.json \
  --environment docs/postman/teamnote-local.postman_environment.json \
  --reporters cli,json \
  --reporter-json-export docs/postman/member-a-results.json "$@"
