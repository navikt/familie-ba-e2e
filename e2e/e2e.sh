#!/usr/bin/env bash

export COMPOSE_IGNORE_ORPHANS=true

docker-compose pull
docker-compose build
docker-compose up -d --force-recreate


while [[ $(curl -s -X GET "http://localhost:8060/rest/isso/isAlive.jsp") == "" ]]; do
    echo "venter på oppstart av VTP"
    sleep 1
done


while [[ $(curl -s -X GET "http://localhost:8089/internal/health") == "" ]]; do
    echo "venter på oppstart av mottak / sak / oppslag"
    sleep 1
done

echo "Miljøet er satt opp."
#docker-compose -f cypress.yml up