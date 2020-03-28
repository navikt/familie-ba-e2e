#!/usr/bin/env bash

export COMPOSE_IGNORE_ORPHANS=true

docker-compose pull
docker-compose build
docker-compose up

while [[ $(curl -s -X GET "http://host.docker.internal:8089/internal/health") == "" ]]; do
    echo "venter på oppstart av mottak / sak / integrasjoner"
    sleep 1
done

echo "Miljøet er satt opp."
#docker-compose -f cypress.yml up