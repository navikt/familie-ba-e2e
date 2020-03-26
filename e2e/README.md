For å kjøre dette lokalt må du lage en .env fil på rotnivå i e2e-mappen med azureAD settinger. Disse kan du kopiere inn fra vault.
Husk å prefikse med app siden noen env variabler er like på tvers av apper.

```
docker-compose pull
docker-compose build
docker-compose up
```


```
MOTTAK_CLIENT_ID=
MOTTAK_FRONTEND_CLIENT_ID=
MOTTAK_CLIENT_SECRET=
MOTTAK_BA_SAK_SCOPE=
MOTTAK_FAMILIE_INTEGRASJONER_SCOPE=

INTEGRASJONER_CLIENT_ID=
INTEGRASJONER_CLIENT_SECRET=
INTEGRASJONER_INFOTRYGD_KS_SCOPE=
INTEGRASJONER_AAD_GRAPH_SCOPE=

OPPDRAG_CLIENT_ID=
OPPDRAG_BA_SAK_CLIENT_ID=
```