#!/usr/bin/env bash

docker-compose -f cypress.yml down
docker-compose -f familie-ba-apps.yml down
docker-compose -f vtp-og-infrastruktur.yml down