# familie-ba-e2e

Testriggen kan kjøre på ekstern server og lokalt. Lokalt kan man erstatte ét eller flere docker-images manuelt, slik at man kan teste applikasjonen i sammenheng med de andre applikasjonene. På sikt bør man kunne kjøre applikasjonen under test som en lokal prosess (f.eks. via IntelliJ), da det er vanskeligere å debugge et docker-image.

e2e.sh kjører alle applikasjonene, og inneholder enkel logikk for å vente på applikasjoner som er avhengig av andre kjørende applikasjoner.

## Kjør headless tester

```shell
docker login docker.pkg.github.com -u USERNAME -p TOKEN
cd e2e
docker-compose up
cd ../frontend
yarn
yarn test
```

## Kjør cypress og lag tester

For å kjøre opp cypress:

```shell
cd frontend
yarn open
```

Cypress åpnes i eget vindu og man kan klikke inn på testene. Dersom man gjør endringer i testene (/frontend/integration) kjøres testen(e) på nytt automatisk.

## Mer informasjon

- [https://docs.cypress.io](https://docs.cypress.io)
- [https://on.cypress.io/docker](https://on.cypress.io/docker)
