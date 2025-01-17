# yes-tion API

The yes-tion API allows to manage notes. It uses the HTTP protocol and the JSON
format.

The API is based on the CRUD pattern. It has the following operations:

- Create a new user
- Create a note
- Modify a note
- Delete a note
- Get title of all notes
- Get a note by his title

Users are also able to log in and logout. They can also get their profile.

## Endpoints

### Create a new user

- `POST /users`

Create a new user.

#### Request

The request body must contain a JSON object with the following properties:

- `firstName` - The first name of the user
- `lastName` - The last name of the user

#### Response

The response body contains a JSON object with the following properties:

- `id` - The unique identifier of the user
- `firstName` - The first name of the user
- `lastName` - The last name of the user

#### Status codes

- `201` (Created) - The user has been successfully created
- `400` (Bad Request) - The request body is invalid
- `409` (Conflict) - The user already exists

### Get all notes titles

- `GET /notes`

Get many users.

#### Request

The request can contain the following query parameters:

- `id` - The user's id

#### Response

The response body contains a JSON array with the following properties:

- `noteTitle` - The unique note's title
- `noteContent` - The note's content

#### Status codes

- `200` (OK) - The notes have been successfully retrieved

### Get one user

- `GET /notes/{title}`

Get one note by its title.

#### Request

The request path must contain the title of the user.

#### Response

The response body contains a JSON object with the following properties:

- `noteTitle` - The unique note's title
- `noteContent` - The note's content

#### Status codes

- `200` (OK) - The note has been successfully retrieved
- `404` (Not Found) - The note does not exist

### Update a note

- `PUT /notes/{title}`

Update a note by its title.

#### Request

The request path must contain the ID of the user.

The request body must contain a JSON object with the following properties:

- `noteTitle` - The unique note's updated title
- `noteContent` - The note's updated content

#### Response

The response body contains a JSON object with the following properties:

- `noteTitle` - The unique note's updated title
- `noteContent` - The note's updated content

#### Status codes

- `200` (OK) - The note has been successfully updated
- `400` (Bad Request) - The request body is invalid
- `404` (Not Found) - The note does not exist
- `409` (Conflict) - The note's title already exists

### Delete a note

- `DELETE /notes/{title}`

Delete a note by its title.

#### Request

The request path must contain the title of the note.

#### Response

The response body is empty.

#### Status codes

- `204` (No Content) - The note has been successfully deleted
- `404` (Not Found) - The note does not exist

### Login

- `POST /login`

Login a user.

#### Request

The request body must contain a JSON object with the following properties:

- `id` - The unique identifier of the user 

#### Response

The response body is empty. A `user` cookie is set with the ID of the user.

#### Status codes

- `204` (No Content) - The user has been successfully logged in
- `400` (Bad Request) - The request body is invalid
- `401` (Unauthorized) - The user does not exist or the password is incorrect

### Logout

- `POST /logout`

Logout a user.

#### Request

The request body is empty.

#### Response

The response body is empty. The `user` cookie is removed.

#### Status codes

- `204` (No Content) - The user has been successfully logged out

### Profile

- `GET /profile`

Get the current user (the user that is logged in).

#### Request

The request body is empty.

#### Response

The response body contains a JSON object with the following properties:

- `id` - The unique identifier of the user
- `firstName` - The first name of the user
- `lastName` - The last name of the user
- `email` - The email address of the user
- `password` - The password of the user

#### Status codes

- `200` (OK) - The user has been successfully retrieved
- `401` (Unauthorized) - The user is not logged in