# familie-ba-e2e

Her finner man ende-til-endetester (`/autotest`) og mijøet man kjører de med (`/e2e`). 

Testriggen kan kjøre på ekstern server og lokalt. Lokalt kan man erstatte ett eller flere docker-images manuelt (`/e2e/docker-compose.yml`), slik at man kan teste applikasjonen i sammenheng med de andre applikasjonene. Man kan også koble seg til en eller flere applikasjoner via IntelliJ, for å kjøre debugging med breakpoints (se egen seksjon nedenfor).

I `/e2e` ligger `e2e.sh` som kjører alle applikasjonene og inneholder enkel logikk for å vente på applikasjoner som er avhengig av andre kjørende applikasjoner. 

## Kjøre tester
1. Legg inn secrets i `e2e/.env`. Hentes fra vault `prod-fss/familie/default/familie-ba-e2e.env`.
2. Docker er forhåndskonfigurert med mindre tilgjengelig minne enn hva e2e-oppsettet trenger. Dette må økes i innstillingene til Docker. Det kan være lurt å sette av 6 GB. Kan gjøres ved å gå inn i preferences -> resources via Docker Dashboard.
3. I `/e2e`: Spinn opp e2e-miljø: `./e2e.sh`. (Ønsker du mer verbos kjøring kan du fjerne detach-flagg `-d` fra `e2e.sh`-script)
4. I `/autotest`: Kjør ønskede tester

### Tips

For effektiv utvikling kan disse kommandoene være nyttige:

- For mer effektivt bygg: `mvn clean install -Dmaven.test.skip=true`
- For å hente informasjon om docker containerne som kjører: `docker ps`
- For logger fra de ulike appene: `docker logs <docker-id> -f`

## Debugging via IntelliJ
Forutsetter at man gjør noen endringer rundt docker-fila til applikasjonen(e) under test i forkant av `docker build`.

#### Eksempel med ba-sak

Åpne familie-ba-sak i IntelliJ:
1. Legg til i `init.sh`:
    ```shell
    export JAVA_OPTS='-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:8089 -Djava.security.egd=file:/dev/./urandom'
    ```
2. Opprett ny fil, `run-java.sh`, med innehold:
    ```shell
    exec java ${DEFAULT_JVM_OPTS} ${JAVA_OPTS} -jar app.jar ${RUNTIME_OPTS}
   ```
3. Legg til følgende linje i `Dockerfile`:
    ```shell
    COPY run-java.sh run-java.sh
    ```
4. Kjør `docker build -t ba-sak:local .` fra rotmappen til prosjektet.

5. Opprett en Remote debug-konfigurasjon ( -> `Edit Configurations...` -> `+` -> `Remote` )
    og velg: `Host:` localhost, `Port:` 8089, `Use module classpath:` familie-ba-sak. Gi konfigurasjonen et navn og trykk OK.

Åpne `docker-compose.yml` i familie-ba-e2e, og sett `familie-ba-sak:` `image: ba-sak:local` 

Kjør
```shell
cd e2e
./e2e.sh
```
og vent på meldingen
```shell
venter på oppstart av mottak / sak / integrasjoner
```
Da er det på tide å starte debug-konfigurasjonen opprettet i punkt 5 over.

```shell
Miljøet er satt opp.
```
Deretter man sette breakpoints og f.eks kjøre en av testene i `/autotest`

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

