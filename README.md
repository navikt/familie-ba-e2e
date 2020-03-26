# familie-ba-e2e

## WIP

Alle docker-images det refereres til i stegX.yml må bygges lokalt. På sikt skal det hentes docker-images fra eksternt repository. Tanken er å oppdatere referansene til eksterne repoer etterhvert som de pushes til master.

Testriggen skal kunne kjøre på ekstern server og lokalt. For lokal kjøring via IntelliJ ligger det mer info i .idea/[README](.idea/README.md)

e2e.sh kjører alle applikasjonene, og inneholder enkel logikk for å vente på applikasjoner som er avhengig av andre kjørende applikasjoner.

## Run headless tests

[e2e/docker-compose.yml](e2e/docker-compose.yml) has all settings to start the web application and run Cypress end-to-end tests from [e2e/cypress/integration](e2e/cypress/integration)

```shell
docker login docker.pkg.github.com -u USERNAME -p TOKEN
cd e2e
docker-compose up --exit-code-from cypress
```

## Run Test Runner

To start Cypress Test Runner (just like `cypress open` does), you need to:

- set up X11 server on the host machine, for example using [XQuartz](https://www.xquartz.org) following the [Running GUI applications using Docker for Mac](https://sourabhbajaj.com/blog/2017/02/07/gui-applications-docker-mac/)
- set the `IP` variable and allow X11 server to accept incoming connections from that network address, see [e2e/cy-open.yml](e2e/cy-open.yml) for commands
- set the `DISPLAY` variable and pass it to the `docker-compose` when running with both configuration files

```shell
cd e2e
docker-compose -f docker-compose.yml -f cy-open.yml up --exit-code-from cypress
```

You should see Cypress open and be able to run tests, change files, see tests rerun.


## More information

- [https://docs.cypress.io](https://docs.cypress.io)
- [https://on.cypress.io/docker](https://on.cypress.io/docker)
