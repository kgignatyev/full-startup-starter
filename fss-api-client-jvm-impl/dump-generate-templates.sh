#!/usr/bin/env bash
MODULE=fss
API_VERSION=v1

GENERATOR=kotlin
OUTPUT_DIR="./out/template"

mkdir -p "out"
rm -fR ./out/*


USER=$(id -u)

echo "using openapi definitions located at: $DEFINITIONS"
docker run --rm  --user=$USER \
  -v ${PWD}:/local \
  openapitools/openapi-generator-cli:v7.19.0 \
  author template \
  -g ${GENERATOR} \
  -o /local/${OUTPUT_DIR}

