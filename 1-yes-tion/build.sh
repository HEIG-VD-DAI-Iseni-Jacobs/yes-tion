#!/bin/bash

# Get build date in UTC
BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')

# Build the project
mvn spotless:apply dependency:go-offline clean compile package -DskipTests  # Needed to not run the tests

# Build the Docker image
docker build \
    --no-cache=true \
    --build-arg BUILD_DATE="${BUILD_DATE}" \
    -t ghcr.io/heig-vd-dai-iseni-jacobs/yes-tion:latest \
    .

# Display confirmation message for user
echo -e "\e[37m\e[46mBuilt yes-tion:latest with BUILD_DATE=${BUILD_DATE}\e[0m"