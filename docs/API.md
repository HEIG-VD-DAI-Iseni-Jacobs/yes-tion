# yes-tion API

The yes-tion API allows to manage notes. It uses the HTTP protocol, the JSON
format and cookies to manage sessions.

The API is based on the CRUD pattern. It has the following operations:

- [Create a new user](#create-a-new-user)
- [Create a note](#create-a-note)
- [Modify a note](#update-a-note)
- [Get title of all notes](#get-all-notes-titles)
- [Get a note by his title](#get-one-note)
- [Delete a note](#delete-a-note)

Users are also able to [log in](#login) and [logout](#logout). They can also get their profile.

## Endpoints

### Create a new user

- `POST /users`

Create a new user.

#### Request

The request body must contain a JSON object with the following properties:

- `firstName` - The first name of the user
- `lastName` - The last name of the user
- `email` - The email address of the user

#### Response

The response body contains a JSON object with the following properties:

- `userId` - The unique identifier of the user
- `firstName` - The first name of the user
- `lastName` - The last name of the user
- `email` - The email address of the user

#### Status codes

- `201` (Created) - The user has been successfully created
- `400` (Bad Request) - The request body is invalid
- `409` (Conflict) - The user already exists

### Create a note

- `POST /notes`

Create a new note.

#### Request

The request must include the `user` cookie.

The request body must contain a JSON object with the following properties:

- `noteTitle` - The unique note's title
- `noteContent` - The note's content

#### Response

The response body contains a JSON object with the following properties:

- `noteId` - The unique identifier of the note
- `noteTitle` - The note's title
- `noteContent` - The note's content

#### Status codes

- `201` (Created) - The note has been successfully created
- `400` (Bad Request) - The request body is invalid
- `401` (Unauthorized) - Missing or invalid `user` cookie.
- `409` (Conflict) - The note already exists


### Update a note

- `PUT /notes/{noteId}`

Update a note by its unique identifier.

#### Request

The request path must contain the id of the note.

The request must include the `user` cookie.

The request body must contain a JSON object with the following properties:

- `noteTitle` - The note's updated title
- `noteContent` - The note's updated content

#### Response

The response body contains a JSON object with the following properties:

- `noteTitle` - The unique note's updated title
- `noteContent` - The note's updated content

#### Status codes

- `200` (OK) - The note has been successfully updated
- `400` (Bad Request) - The request body is invalid
- `401` (Unauthorized) - Missing or invalid `user` cookie.
- `404` (Not Found) - The note does not exist


### Get all notes titles

- `GET /notes`

Get all user's notes.

#### Request

The request must include the `user` cookie.

The request contains no query parameters.

#### Response

The response body contains a JSON array with the following properties:

- `noteId` - The unique identifier of the note
- `noteTitle` - The note's title
- `noteContent` - The note's content

#### Status codes

- `200` (OK) - The notes have been successfully retrieved
- `401` (Unauthorized) - Missing or invalid `user` cookie.


### Get one note

- `GET /notes/{noteId}`

Get one note by its unique identifier.

#### Request

The request path must contain the id of the note.

The request must include the `user` cookie.

The request contains no query parameters.

#### Response

The response body contains a JSON object with the following properties:

- `noteId` - The unique identifier of the note
- `noteTitle` - The note's title
- `noteContent` - The note's content

#### Status codes

- `200` (OK) - The note has been successfully retrieved
- `401` (Unauthorized) - Missing or invalid `user` cookie.
- `404` (Not Found) - The note does not exist


### Delete a note

- `DELETE /notes/{noteId}`

Delete a note by its unique identifier.

#### Request

The request path must contain the id of the note.

The request must include the `user` cookie.

#### Response

The response body is empty.

#### Status codes

- `204` (No Content) - The note has been successfully deleted
- `401` (Unauthorized) - Missing or invalid `user` cookie.
- `404` (Not Found) - The note does not exist

### Login

- `POST /login`

Login a user.

#### Request

The request body must contain a JSON object with the following propertie:

- `email` - The email address of the user

#### Response

The response body is empty. A `user` cookie is set with the ID of the user.

#### Status codes

- `204` (No Content) - The user has been successfully logged in
- `400` (Bad Request) - The request body is invalid
- `401` (Unauthorized) - The user does not exist


### Logout

- `POST /logout`

Logout a user.

#### Request

The request must include the `user` cookie.

#### Response

The response body is empty. The `user` cookie is removed.

#### Status codes

- `204` (No Content) - The user has been successfully logged out


### Profile

- `GET /profile`

Get the current user (the user that is logged in).

#### Request

The request must include the `user` cookie.

#### Response

The response body contains a JSON object with the following properties:

- `userId` - The unique identifier of the user
- `firstName` - The first name of the user
- `lastName` - The last name of the user
- `email` - The email address of the user

#### Status codes

- `200` (OK) - The user has been successfully retrieved
- `401` (Unauthorized) - The user is not logged in