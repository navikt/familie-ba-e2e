#!/usr/bin/env bash

docker-compose  -f cypress.yml down --remove-orphans
docker-compose  -f familie-ba-apps.yml down --remove-orphans
docker-compose  -f vtp-og-infrastruktur.yml down --remove-orphans
docker-compose  -f kafka-med-venner.yml down --remove-orphans