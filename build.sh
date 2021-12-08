#!/usr/bin/env bash

set -e

REGISTRY="${REGISTRY:-}"

# build pgman
pgman_version="1.4.0"
echo "
################################################################################
################### Build pgman ##############################################
################################################################################
Image: ${REGISTRY}dgiorgio/pgman:${pgman_version}"
docker build -t "${REGISTRY}dgiorgio/pgman:${pgman_version}" -t "${REGISTRY}dgiorgio/pgman:latest" .

echo "All images successfully built."

# push
if [ "${1}" == "push" ]; then
  docker push "${REGISTRY}dgiorgio/pgman:${pgman_version}"
  docker push "${REGISTRY}dgiorgio/pgman:latest"

  echo "All images sent successfully."
fi
