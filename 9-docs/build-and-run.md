# Deploy the app

In here, we assume that you are in the machine you want to use as your server, and you already have cloned the repo on it. If not, see [setup-infrastructure](./setup-infrastructure.md).

## Traefik

If you already have Traefik set up on your server, you can skip this step. Otherwise, check out [this guide](../2-traefik/README.md) to set up Traefik on your server.

## Setup the environment variables

In the 1-yes-tion folder, update the environment variables marked with `#CHANGEME` in the .env file
- `FULLY_QUALIFIED_DOMAIN_NAME`: The domain name you set up with DuckDNS

## Build Docker image

This is not mandatory. You can use the image we built and published on GitHub Container Registry.

<details>
<summary>Tutorial</summary>

Or simply go into the `1-yes-tion` folder and use the following script to build the Docker image:

```bash
./build.sh
```

### Publish the image to GitHub Container Registry

If you are not familiar with Github Container Registry:

<details>
<summary>Setup Github Container Registry</summary>

### Create a personal access token

You will need a personal access token to publish an image on GitHub Container
Registry.

A personal access token is a token that you can use to authenticate to GitHub
instead of using your password. It is more secure than using your password.

Follow the instructions on the official website to authenticate with a personal
access token (classic):
<https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry>.

> [!NOTE]
>
> You can find the personal access token in the settings of your GitHub account:
> **Settings** > **Developer settings** (at the very end of the left side bar) >
> **Personal access tokens** > **Tokens (classic)**.

### Login to GitHub Container Registry

Login to GitHub Container Registry with the following command, replacing
`<username>` with your GitHub username:

```sh
# Login to GitHub Container Registry
docker login ghcr.io -u <username>
```

When asked for the password, use the personal access token you created earlier.

The output should be similar to the following:

```text
Login Succeeded
```

</details>

If you built a new image, you can publish it to GitHub Container Registry with the following script:

```bash
./publish.sh
```

</details>

## Run the app with Docker compose

Make sure traefik is running.  
Navigate to the `1-yes-tion` folder and run:

```bash
docker compose up -d
```

Note: This runs by default the image from the GitHub Container Registry. If you want to run the image you built, you can change the image in the `docker-compose.yml` file.

You're all set! You can now access the app at `https://yes-tion.your-domain.com`.