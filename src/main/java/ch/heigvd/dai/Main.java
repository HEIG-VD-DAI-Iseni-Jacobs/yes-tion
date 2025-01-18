package ch.heigvd.dai;

import ch.heigvd.dai.users.*;
import ch.heigvd.dai.notes.*;
import ch.heigvd.dai.auth.AuthController;
import io.javalin.Javalin;

import java.util.concurrent.ConcurrentHashMap;

public class Main {
  public static final int PORT = 8080;
  public static void main(String[] args) {
    Javalin app = Javalin.create();

    // This will serve as our database
    ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Note> notes = new ConcurrentHashMap<>();

    AuthController authController = new AuthController(users);
    UsersController usersController = new UsersController(users);
    NotesController notesController = new NotesController(users, notes);

    // Auth routes
    app.post("/login", authController::login);
    app.post("/logout", authController::logout);
    app.get("/profile", authController::profile);

    // Users routes
    app.post("/users", usersController::createUser);

    // Notes routes
    app.post("/notes", notesController::createNote);
    app.put("/notes/{id}", notesController::updateNote);
    app.get("/notes", notesController::getAllNotes);
    app.get("/notes/{id}", notesController::getOneNote);
    app.delete("/notes/{id}", notesController::deleteOneNote);

    app.start(PORT);
  }
}
