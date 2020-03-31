# familie-ba-e2e

Testriggen kan kjøre på ekstern server og lokalt. Lokalt kan man erstatte ett eller flere docker-images manuelt, slik at man kan teste applikasjonen i sammenheng med de andre applikasjonene. På sikt bør man kunne kjøre applikasjonen under test som en lokal prosess (f.eks. via IntelliJ), da det er vanskeligere å debugge et docker-image.

e2e.sh kjører alle applikasjonene, og inneholder enkel logikk for å vente på applikasjoner som er avhengig av andre kjørende applikasjoner. For at e2e.sh skal kunne kjøre opp miljøet lokalt, må man legge inn følgende linje i /etc/hosts-filen: `127.0.0.1 host.docker.internal`. 

For lokal kjøring via IntelliJ ligger det mer info i .idea/[README](.idea/README.md). Husk at run-konfigurasjonene må oppdateres hvis en app eller miljøvariabel legges til/endres.

Docker er forhåndskonfigurert med mindre tilgjengelig minne enn hva e2e-oppsettet trenger. Dette må økes i innstillingene til Docker. Det kan være lurt å sette av 6 GB.

## Kjør headless tester

```shell
docker login docker.pkg.github.com -u USERNAME -p TOKEN
cd e2e
./e2e.sh

# Vent til miljøet er oppe
cd ../frontend
yarn
yarn test
```

## Kjør cypress og lag tester

For å kjøre opp cypress:

```shell
cd e2e
./e2e.sh

# Vent til miljøet er oppe
cd ../frontend
yarn open
```

Cypress åpnes i eget vindu og man kan klikke inn på testene. Dersom man gjør endringer i testene (/frontend/integration) kjøres testen(e) på nytt automatisk.

## Mer informasjon

- [https://docs.cypress.io](https://docs.cypress.io)
- [https://on.cypress.io/docker](https://on.cypress.io/docker)
