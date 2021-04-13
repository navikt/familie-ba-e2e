#!/usr/bin/env bash

export COMPOSE_IGNORE_ORPHANS=true

appsombyggeslokalt=${1}
appversjonlokalt=${2}

if [ -z "$appsombyggeslokalt" ]
then
  echo "starter e2e"
else
  echo "starter e2e med $appsombyggeslokalt på versjon $appversjonlokalt "
fi

docker-compose pull --quiet
docker-compose build

dockercompose=`cat docker-compose.yml`
detviskalfinne="docker.pkg.github.com/navikt/$appsombyggeslokalt/$appsombyggeslokalt:latest"

echo "${dockercompose/$detviskalfinne/$appversjonlokalt}" | docker-compose -f - up -d --force-recreate

while [[ $(curl -s -X GET "http://localhost:8089/internal/health") == "" ]]; do
    echo "venter på oppstart av mottak / sak / integrasjoner"
    sleep 1
done

echo "Miljøet er satt opp."