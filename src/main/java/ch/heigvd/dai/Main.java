package ch.heigvd.dai;

import ch.heigvd.dai.notes.*;
import ch.heigvd.dai.users.*;
import io.javalin.Javalin;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
  public static final int PORT = 8080;

  public static void main(String[] args) {
    Javalin app = Javalin.create();

    // This will serve as our database
    ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Note> notes = new ConcurrentHashMap<>();

    UsersController usersController = new UsersController(users, notes);
    NotesController notesController = new NotesController(users, notes);

    // Users routes
    app.post("/signup", usersController::signUp);
    app.post("/login", usersController::login);
    app.post("/logout", usersController::logout);
    app.get("/profile", usersController::getProfile);
    app.put("/profile", usersController::updateProfile);
    app.delete("/profile", usersController::deleteProfile);

    // Notes routes
    app.post("/notes", notesController::createNote);
    app.put("/notes/{id}", notesController::updateNote);
    app.get("/notes", notesController::getAllNotes);
    app.get("/notes/{id}", notesController::getOneNote);
    app.delete("/notes/{id}", notesController::deleteOneNote);

    app.start(PORT);
  }
}
