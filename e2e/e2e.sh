#!/usr/bin/env bash

docker-compose -f vtp-og-infrastruktur.yml up -d

while [[ $(curl -s -X GET "http://localhost:8060/rest/isso/isAlive.jsp") == "" ]]; do
    echo "venter på oppstart av VTP"
    sleep 1
done

docker-compose -f familie-ba-apps.yml up -d

while [[ $(curl -s -X GET "http://localhost:8089/internal/health") == "" ]]; do
    echo "venter på oppstart av mottak / sak / oppslag"
    sleep 1
done

docker-compose -f cypress.yml up