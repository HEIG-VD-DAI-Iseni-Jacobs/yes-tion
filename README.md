**Authors :** Iseni Aladin & Jacobs Arthur
# Yes-tion

[No-tion](https://github.com/HEIG-VD-DAI-Iseni-Jacobs/no-tion), but yes.


## Description

This project is a simple web application that allows users to create, read, update, and delete accounts and the notes associated to them.
It is a really simplified version of the popular note-taking application [Notion](https://www.notion.so/).

# `TODO` Run with docker
Build using `./build.sh` (may need to use chmod to update rights)

```bash
docker run -p 8080:8080 ghcr.io/heig-vd-dai-iseni-jacobs/yes-tion
```

publish the image to the github container registry

```bash
./publish.sh
```

## Launch app with Maven

```bash
# Launch the application without tests
mvn spotless:apply dependency:go-offline clean compile package -DskipTests

# Launch tests
mvn test
```

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
To simplify this process, you can change the current directory to the /test-app directory which already contains the cookies.txt file.
Or, you can stay in any directory.
Afterward, you can keep this file or delete it after you finish your tests.

### Create an account

To create an account, you need to send a `POST` request to the `/users` endpoint with a JSON payload containing the first name, last name, and email of the user.

```bash
curl -i \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  }' \
  http://localhost:8080/signup
```

Output:
```bash
HTTP/1.1 201 Created
Date: Thu, 23 Jan 2025 19:01:23 GMT
Content-Type: application/json
Last-Modified: 2025-01-23T20:01:23.913262213
Content-Length: 79

{"userId":1,"firstName":"John","lastName":"Doe","email":"john.doe@example.com"}
```

### Log in

To login, you need to send a `POST` request to the `/login` endpoint with a JSON payload containing the email of the user and store the cookies in a file.

```bash
curl -i \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"email": "john.doe@example.com"}' \
  -c cookies.txt \
  http://localhost:8080/login
```

Output:
```bash
HTTP/1.1 204 No Content
Date: Thu, 23 Jan 2025 19:04:12 GMT
Content-Type: text/plain
Set-Cookie: user=1; Path=/
Expires: Thu, 01 Jan 1970 00:00:00 GMT
```

### Log out

To logout, you need to send a `POST` request to the `/logout` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -X POST \
  -c cookies.txt \
  http://localhost:8080/logout
```

Output:
```bash
HTTP/1.1 204 No Content
Date: Thu, 23 Jan 2025 19:05:00 GMT
Content-Type: text/plain
Set-Cookie: user=; Path=/; Expires=Thu, 01-Jan-1970 00:00:00 GMT; Max-Age=0
Expires: Thu, 01 Jan 1970 00:00:00 GMT
```

### Get the profile

To get the profile, you need to be logged in and send a `GET` request to the `/profile` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -X GET \
  -b cookies.txt \
  http://localhost:8080/profile
```

Output:
```bash
HTTP/1.1 200 OK
Date: Thu, 23 Jan 2025 19:08:08 GMT
Content-Type: application/json
Last-Modified: 2025-01-23T20:01:23.913262213
Content-Length: 79

{"userId":1,"firstName":"John","lastName":"Doe","email":"john.doe@example.com"}
```

### Update the profile

To update the profile, you need to send a `PUT` request to the `/profile` endpoint with a JSON payload containing the new first name, last name, or email of the user.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -X PUT  \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "email": ""
    }' \
    -b cookies.txt \
    http://localhost:8080/profile
```

Output:
```bash
HTTP/1.1 200 OK
Date: Thu, 23 Jan 2025 19:08:43 GMT
Content-Type: application/json
Last-Modified: 2025-01-23T20:01:23.913262213
Content-Length: 79

{"userId":1,"firstName":"Jane","lastName":"Doe","email":"john.doe@example.com"}
```

### Delete the account

To delete the account, you need to send a `DELETE` request to the `/profile` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -X DELETE \
  -b cookies.txt \
  http://localhost:8080/profile
```

Output:
```bash
HTTP/1.1 204 No Content
Date: Thu, 23 Jan 2025 19:14:41 GMT
Content-Type: text/plain
Set-Cookie: user=; Path=/; Expires=Thu, 01-Jan-1970 00:00:00 GMT; Max-Age=0
Expires: Thu, 01 Jan 1970 00:00:00 GMT
```

### Create a note

To create a note, you need to send a `POST` request to the `/notes` endpoint with a JSON payload containing the title and content of the note.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "noteTitle": "Ma seconde note",
    "noteContent": "Contenu de ma seconde note"
  }' \
  -b cookies.txt \
  http://localhost:8080/notes
```

Output:
```bash
HTTP/1.1 201 Created
Date: Thu, 23 Jan 2025 19:16:33 GMT
Content-Type: application/json
Last-Modified: 2025-01-23T20:16:33.036126574
Content-Length: 90

{"noteId":1,"userId":3,"noteTitle":"Ma première note","noteContent":"Contenu de ma note"}
```

### Get all notes

To get all notes, you need to send a `GET` request to the `/notes` endpoint.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -X GET \
  -b cookies.txt \
  http://localhost:8080/notes
```

Output:
```bash
HTTP/1.1 200 OK
Date: Thu, 23 Jan 2025 19:18:09 GMT
Content-Type: application/json
Last-Modified: 2025-01-23T20:18:09.759392633
Content-Length: 189

[{
  "noteId":1,
  "userId":3,
  "noteTitle":"Ma première note",
  "noteContent":"Contenu de ma note"},
  {
  "noteId":2,
  "userId":3,
  "noteTitle":"Ma seconde note",
  "noteContent":"Contenu de ma seconde note"
}]
```

### Get a note

To get a note, you need to send a `GET` request to the `/notes/{noteId}` endpoint, where `{noteId}` is the identifier of the note.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -X GET \
  -b cookies.txt \
  http://localhost:8080/notes/2
```

### Update a note

To update a note, you need to send a `PUT` request to the `/notes/{noteId}` endpoint with a JSON payload containing the new title and content of the note.
You also need to send the cookies stored in the login step.

```bash
curl -X PUT http://localhost:8080/notes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "noteTitle": "Titre modifié",
    "noteContent": "Nouveau contenu"
  }' \
  -b cookies.txt
```

Output:
```bash
HTTP/1.1 200 OK
Date: Thu, 23 Jan 2025 19:20:58 GMT
Content-Type: application/json
Last-Modified: 2025-01-23T20:18:01.393036634
Content-Length: 96

{"noteId":2,
 "userId":3,
 "noteTitle":"Ma seconde note",
 "noteContent":"Contenu de ma seconde note"}
```

### Delete a note

To delete a note, you need to send a `DELETE` request to the `/notes/{noteId}` endpoint, where `{noteId}` is the identifier of the note.
You also need to send the cookies stored in the login step.

```bash
curl -i \
  -X DELETE \
  -b cookies.txt \
  http://localhost:8080/notes/1
```

Output:
```bash
HTTP/1.1 204 No Content
Date: Thu, 23 Jan 2025 19:21:52 GMT
Content-Type: text/plain
```
