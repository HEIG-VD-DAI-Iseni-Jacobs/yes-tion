# Traefik

This is a quick guide on how to use Traefik to run yes-tion (and other apps).

We assume you already are on the machine you want to run the app on.
See [here](../9-docs/setup-infrastructure.md) if that's not the case.

## Setup Traefik

Go to the `2-traefik` folder. Set up the environment variables marked with `#CHANGEME` in the `.env` and `dns-challenge.env` file.

- `FULLY_QUALIFIED_DOMAIN_NAME`: The domain name you set up with DuckDNS
- `TRAEFIK_ACME_EMAIL`: Your email address
- `TRAEFIK_ACME_DNS_PROVIDER`: The DNS provider you use (e.g. `duckdns`)
- `DUCKDNS_TOKEN`: Your DuckDNS token


## Launch Traefik

Use the following script to launch Traefik:

```bash
docker compose up -d
```

You can access the Traefik dashboard at `http://traefik.your-domain.com`.