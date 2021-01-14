# familie-ba-e2e

Testriggen kan kjøre på ekstern server og lokalt. Lokalt kan man erstatte ett eller flere docker-images manuelt, slik at man kan teste applikasjonen i sammenheng med de andre applikasjonene. På sikt bør man kunne kjøre applikasjonen under test som en lokal prosess (f.eks. via IntelliJ), da det er vanskeligere å debugge et docker-image.

e2e.sh kjører alle applikasjonene, og inneholder enkel logikk for å vente på applikasjoner som er avhengig av andre kjørende applikasjoner. 

For lokal kjøring via IntelliJ ligger det mer info i .idea/[README](.idea/README.md). Husk at run-konfigurasjonene må oppdateres hvis en app eller miljøvariabel legges til/endres.

## Kjøre tester
1. Legg inn secrets i `e2e/.env`. Hentes fra vault `prod-fss/familie/default/familie-ba-e2e.env`.
2. I `/e2e`: Spinn opp e2e-miljø: `./e2e.sh`
3. I `/autotest`: Kjør ønskede tester

## Tips

For effektiv utvikling kan disse kommandoene være nyttige:

- For mer effektivt bygg: `mvn clean install -Dmaven.test.skip=true`
- For å hente informasjon om docker containerne som kjører: `docker ps`
- For logger fra de ulike appene: `docker logs <docker-id> -f`

#### E2E-miljø
- Docker er forhåndskonfigurert med mindre tilgjengelig minne enn hva e2e-oppsettet trenger. Dette må økes i innstillingene til Docker. Det kan være lurt å sette av 6 GB.Kan gjøres ved å gå inn i preferences -> resources via Docker desktop UI.
- Ønsker du mer verbos kjøring kan du fjerne detach-flagg `-d` fra `e2e.sh`-script


## For frontend (ikke støttet enda)

For at e2e.sh skal kunne kjøre opp miljøet lokalt, må man legge inn følgende linje i /etc/hosts-filen: `127.0.0.1 host.docker.internal`. (Inntil vi kjører frontendtester er ikke dette steget nødvendig)

### Kjør headless tester

```shell
docker login docker.pkg.github.com -u USERNAME -p TOKEN
cd e2e
./e2e.sh

# Vent til miljøet er oppe
cd ../frontend
yarn
yarn test
```

### Kjør cypress og lag tester

For å kjøre opp cypress:

```shell
cd e2e
./e2e.sh

# Vent til miljøet er oppe
cd ../frontend
yarn open
```

Cypress åpnes i eget vindu og man kan klikke inn på testene. Dersom man gjør endringer i testene (/frontend/integration) kjøres testen(e) på nytt automatisk.

### Mer informasjon

- [https://docs.cypress.io](https://docs.cypress.io)
- [https://on.cypress.io/docker](https://on.cypress.io/docker)

