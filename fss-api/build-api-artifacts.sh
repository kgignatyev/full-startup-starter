#!/usr/bin/env bash


export API_RELEASE=$(yq  '.info.version' definitions/fss-svc.v1.openapi.yaml)
set -e
set -x

export OPENAPI_GENERATOR_DOCKER_TAG=v7.19.0

scope=${1:-all}

if [ "$scope" == "all" ] || [ "$scope" == "docs" ]; then
  ./build-docs.sh
fi

if [ "$scope" == "all" ] || [ "$scope" == "ui" ]; then
cd ../fss-ui
./generate-api-client.sh
fi


if [ "$scope" == "all" ] || [ "$scope" == "server" ]; then
cd ../fss-api-server-stubs
./generate-server-stubs.sh
fi

if [ "$scope" == "all" ] || [ "$scope" == "jvm-client" ]; then
cd ../fss-api-client-jvm-impl
./generate-and-install-locally.sh
fi



