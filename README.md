# Yes-tion

[No-tion](https://github.com/HEIG-VD-DAI-Iseni-Jacobs/no-tion), but yes.

## Description

This project is a simple web application that allows users to create, read, update, and delete notes.
It is a really simplified version of the popular note-taking application [Notion](https://www.notion.so/).

# `TODO` : Modify the commands to execute them with the vm
## Usage

In this section, we will explain how to interact with the application using the HTTP protocol and the `curl` command-line tool.

### Requirements

At first, you need to have the `curl` command-line tool installed on your machine.
If you don't have it, you can install it by executing the following command:

```bash
# Install curl
sudo apt install curl
```

Then, our application uses cookies to manage the user's session.
To store the cookies between requests, some commands will create a `cookies.txt` file in the current directory.
You can keep this file or delete it after you finish your tests.

### Create an account

To create an account, you need to send a `POST` request to the `/users` endpoint with a JSON payload containing the first name, last name, and email of the user.

```bash
curl POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  }'
```

### Login

To login, you need to send a `POST` request to the `/login` endpoint with a JSON payload containing the email of the user and store the cookies in a file.

```bash
curl POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email": "john.doe@example.com"}' \
  -c cookies.txt
```

### Create a note

To create a note, you need to send a `POST` request to the `/notes` endpoint with a JSON payload containing the title and content of the note.
You also need to send the cookies stored in the login step.

```bash
curl POST http://localhost:8080/notes \
  -H "Content-Type: application/json" \
  -d '{
    "noteTitle": "Ma première note",
    "noteContent": "Contenu de ma note"
  }' \
  -b cookies.txt
```

### Get all notes

To get all notes, you need to send a `GET` request to the `/notes` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl GET http://localhost:8080/notes \
  -b cookies.txt
```

### Get a note

To get a note, you need to send a `GET` request to the `/notes/{noteId}` endpoint, where `{noteId}` is the identifier of the note.
You also need to send the cookies stored in the login step.

```bash
curl GET http://localhost:8080/notes/1 \
  -b cookies.txt
```

### Update a note

To update a note, you need to send a `PUT` request to the `/notes/{noteId}` endpoint with a JSON payload containing the new title and content of the note.
You also need to send the cookies stored in the login step.

```bash
curl PUT http://localhost:8080/notes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "noteTitle": "Titre modifié",
    "noteContent": "Nouveau contenu"
  }' \
  -b cookies.txt
```

### Delete a note

To delete a note, you need to send a `DELETE` request to the `/notes/{noteId}` endpoint, where `{noteId}` is the identifier of the note.
You also need to send the cookies stored in the login step.

```bash
curl DELETE http://localhost:8080/notes/1 \
  -b cookies.txt
```

### Logout

To logout, you need to send a `POST` request to the `/logout` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl POST http://localhost:8080/logout \
  -b cookies.txt
```