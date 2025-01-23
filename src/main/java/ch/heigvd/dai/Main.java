package ch.heigvd.dai;

import ch.heigvd.dai.notes.*;
import ch.heigvd.dai.users.*;
import io.javalin.Javalin;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
  public static final int PORT = 8080;

  public static void main(String[] args) {
    Javalin app =
        Javalin.create(
            config -> {
              config.validation.register(LocalDateTime.class, LocalDateTime::parse);
            });

    // Base de données
    ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Note> notes = new ConcurrentHashMap<>();

    // Caches
    ConcurrentHashMap<Integer, LocalDateTime> usersCache = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, LocalDateTime> notesCache = new ConcurrentHashMap<>();

    UsersController usersController = new UsersController(users, notes, usersCache, notesCache);
    NotesController notesController = new NotesController(users, notes, notesCache);

    // Routes utilisateurs
    app.post("/signup", usersController::signUp);
    app.post("/login", usersController::login);
    app.post("/logout", usersController::logout);
    app.get("/profile", usersController::getProfile);
    app.put("/profile", usersController::updateProfile);
    app.delete("/profile", usersController::deleteProfile);

    // Routes notes
    app.post("/notes", notesController::createNote);
    app.put("/notes/{id}", notesController::updateNote);
    app.get("/notes", notesController::getAllNotes);
    app.get("/notes/{id}", notesController::getOneNote);
    app.delete("/notes/{id}", notesController::deleteOneNote);

    app.start(PORT);
  }
}
