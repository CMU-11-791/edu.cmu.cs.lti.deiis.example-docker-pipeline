#!/usr/bin/env bash

# The main entry point for the integration tests.
#
# This script runs the war file in a Docker container, runs a LSD script
# to query the services, and then kills the docker container.

./start.sh
echo "Waiting for the Docker container to start."
sleep 5

echo "Running the LSD script."
./metadata.lsd

echo "Killing the Docker container."
./stop.sh

echo "Done."

