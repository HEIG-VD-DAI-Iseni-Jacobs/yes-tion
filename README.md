**Authors :** [Iseni Aladin](https://github.com/aladin-heig) & [Jacobs Arthur](https://github.com/Arthur2479)
# Yes-tion

## Table of contents
- [About](#about)
- [Usage](#usage)
  - [Requirements](#requirements)
  - [Create an account](#create-an-account)
  - [Log in](#log-in)
  - [Log out](#log-out)
  - [Get the profile](#get-the-profile)
  - [Update the profile](#update-the-profile)
  - [Delete the account](#delete-the-account)
  - [Create a note](#create-a-note)
  - [Get all notes](#get-all-notes)
  - [Get a note](#get-a-note)
  - [Update a note](#update-a-note)
  - [Delete a note](#delete-a-note)
- [Contributing](#contributing)
  - [Start an issue and fork the project](#start-an-issue-and-fork-the-project)
  - [Run with docker](#run-with-docker)
  - [Build and publish with Docker](#build-and-publish-with-docker)


## About
[No-tion](https://github.com/HEIG-VD-DAI-Iseni-Jacobs/no-tion), but yes.

This project is a simple web application that allows users to create, read, update, and delete accounts and the notes associated to them.
It is a really simplified version of the popular note-taking application [Notion](https://www.notion.so/).

The domain names used in this project are:
- traefik.arthurjacobs.duckdns.org
- yes-tion.arthurjacobs.duckdns.org
## Usage

In this section, we will explain how to interact with the application using the HTTP protocol and the `curl` command-line tool.

You can acces the traefik dashboard at the following link: [https://traefik.arthurjacobs.duckdns.org/dashboard/](https://traefik.arthurjacobs.duckdns.org/dashboard/)

### Requirements

At first, you need to have the `curl` command-line tool installed on your machine.
If you don't have it, you can install it by executing the following command:

```bash
# Install curl
sudo apt install curl
```

Then, our application uses cookies to manage the user's session.
To store the cookies between requests, some commands will create a `cookies.txt` file in the current directory.
To simplify this process, you can change the current directory to the /test-app directory which already contains the cookies.txt file.
Or, you can stay in any directory.
Afterward, you can keep this file or delete it after you finish your tests.

### Create an account

To create an account, you need to send a `POST` request to the `/users` endpoint with a JSON payload containing the first name, last name, and email of the user.

```bash
curl -i \
  -k \
  -L \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  }' \
  https://yes-tion.arthurjacobs.duckdns.org/signup
```

Output:
```bash
HTTP/2 201
content-type: application/json
date: Thu, 23 Jan 2025 19:56:05 GMT
content-length: 79

{
  "userId":1,"firstName":"John",
  "lastName":"Doe",
  "email":"john.doe@example.com"
}
```

### Log in

To login, you need to send a `POST` request to the `/login` endpoint with a JSON payload containing the email of the user and store the cookies in a file.

```bash
curl -i \
  -k \
  -L \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email": "john.doe@example.com"}' \
  -c cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/login
```

Output:
```bash
HTTP/2 204
content-type: text/plain
date: Thu, 23 Jan 2025 19:59:40 GMT
expires: Thu, 01 Jan 1970 00:00:00 GMT
set-cookie: user=1; Path=/
```

### Log out

To logout, you need to send a `POST` request to the `/logout` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X POST \
  -c cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/logout
```

Output:
```bash
HTTP/2 204
content-type: text/plain
date: Thu, 23 Jan 2025 20:35:07 GMT
expires: Thu, 01 Jan 1970 00:00:00 GMT
set-cookie: user=; Path=/; Expires=Thu, 01-Jan-1970 00:00:00 GMT; Max-Age=0
```

### Get the profile

To get the profile, you need to be logged in and send a `GET` request to the `/profile` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X GET \
  -b cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/profile
```

Output:
```bash
HTTP/2 200
content-type: application/json
date: Thu, 23 Jan 2025 20:01:29 GMT
content-length: 79

{
  "userId":1,
  "firstName":"John",
  "lastName":"Doe",
  "email":"john.doe@example.com"
}
```

### Update the profile

To update the profile, you need to send a `PUT` request to the `/profile` endpoint with a JSON payload containing the new first name, last name, or email of the user.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X PUT  \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": ""
    }' \
    -b cookies.txt \
    https://yes-tion.arthurjacobs.duckdns.org/profile
```

Output:
```bash
HTTP/2 200
content-type: application/json
date: Thu, 23 Jan 2025 20:36:45 GMT
content-length: 79

{
  "userId":1,
  "firstName":"Jane",
  "lastName":"Doe",
  "email":"john.doe@example.com"
}
```

### Delete the account

To delete the account, you need to send a `DELETE` request to the `/profile` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X DELETE \
  -b cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/profile
```

Output:
```bash
HTTP/2 204
content-type: text/plain
date: Thu, 23 Jan 2025 20:49:08 GMT
expires: Thu, 01 Jan 1970 00:00:00 GMT
set-cookie: user=; Path=/; Expires=Thu, 01-Jan-1970 00:00:00 GMT; Max-Age=0
```

### Create a note

To create a note, you need to send a `POST` request to the `/notes` endpoint with a JSON payload containing the title and content of the note.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "noteTitle": "Ma première note",
    "noteContent": "Contenu de ma première note"
  }' \
  -b cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/notes
```

Output:
```bash
HTTP/2 201
content-type: application/json
date: Thu, 23 Jan 2025 20:37:55 GMT
content-length: 100

{
  "noteId":1,
  "userId":1,
  "noteTitle":"Ma première note",
  "noteContent":"Contenu de ma première note"
}
```

### Get all notes

To get all notes, you need to send a `GET` request to the `/notes` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X GET \
  -b cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/notes
```

Output:
```bash
HTTP/2 200
content-type: application/json
date: Thu, 23 Jan 2025 20:39:21 GMT
content-length: 199

[{
  "noteId":1,
  "userId":1,
  "noteTitle":"Ma première note",
  "noteContent":"Contenu de ma première note"
},{
  "noteId":2,
  "userId":1,
  "noteTitle":"Ma seconde note",
  "noteContent":"Contenu de ma seconde note"
}]
```

### Get a note

To get a note, you need to send a `GET` request to the `/notes/{noteId}` endpoint, where `{noteId}` is the identifier of the note.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X GET \
  -b cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/notes/2
```

Output:
```bash
HTTP/2 200
content-type: application/json
date: Thu, 23 Jan 2025 20:40:44 GMT
content-length: 96

{
  "noteId":2,
  "userId":1,
  "noteTitle":"Ma seconde note",
  "noteContent":"Contenu de ma seconde note"
}
```

### Update a note

To update a note, you need to send a `PUT` request to the `/notes/{noteId}` endpoint with a JSON payload containing the new title and content of the note.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X PUT \
  -H "Content-Type: application/json" \
  -d '{
    "noteTitle": "Titre modifié",
    "noteContent": "Nouveau contenu"
  }' \
  -b cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/notes/1
```

Output:
```bash
HTTP/2 200
content-type: application/json
date: Thu, 23 Jan 2025 20:42:39 GMT
content-length: 84

{
  "noteId":1,
  "userId":1,
  "noteTitle":"Titre modifié",
  "noteContent":"Nouveau contenu"
}
```

### Delete a note

To delete a note, you need to send a `DELETE` request to the `/notes/{noteId}` endpoint, where `{noteId}` is the identifier of the note.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -k \
  -L \
  -X DELETE \
  -b cookies.txt \
  https://yes-tion.arthurjacobs.duckdns.org/notes/1
```

Output:
```bash
HTTP/2 204
content-type: text/plain
date: Thu, 23 Jan 2025 20:47:28 GMT
```

## Contributing

Contributions are welcome! To contribute:

### Start an issue and fork the project

1. Create an issue describing the feature you want to implement
2. Fork the project and clone it

```shell
git clone git@github.com:<your-GitHub-name>/<repo-name>.git
```

3. Create your feature branch

````shell
git checkout -b feature/my-feature
````

4. Build the project

````shell
mvn spotless:apply dependency:go-offline clean compile package -DskipTests
````

5. Add and commit your changes

````shell
git add <files>
git commit -m "Add my feature"
````

6. Push the branch

```shell
git push
```

7. Open a Pull Request

If you have any questions or suggestions, feel free to open
an [issue](https://github.com/HEIG-VD-DAI-Iseni-Jacobs/pictures-cli-editor/issues) on GitHub.

### Run with docker
```bash
docker run -p 8080:8080 ghcr.io/heig-vd-dai-iseni-jacobs/yes-tion
```

### Build and publish with Docker
To build and publish the Docker image, you can refer to the following file: [setup-and-deploy.md](docs/setup-and-deploy.md)