#!/usr/bin/env bash

set -e

REGISTRY="${REGISTRY:-}"

# build pgman
pgman_version="1.4.0"
echo "
################################################################################
################### Build pgman ##############################################
################################################################################
Image: ${REGISTRY}touchhealth/pgman:${pgman_version}"
docker build -t "${REGISTRY}touchhealth/pgman:${pgman_version}" -t "${REGISTRY}touchhealth/pgman:latest" .

echo "All images successfully built."

# push
if [ "${1}" == "push" ]; then
  docker push "${REGISTRY}touchhealth/pgman:${pgman_version}"
  docker push "${REGISTRY}touchhealth/pgman:latest"

  echo "All images sent successfully."
fi
