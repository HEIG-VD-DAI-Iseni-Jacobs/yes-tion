package ch.heigvd.dai.notes;

import ch.heigvd.dai.users.User;
import io.javalin.http.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NotesController {
  private final ConcurrentHashMap<Integer, Note> notes;
  private final ConcurrentHashMap<Integer, User> users;
  private final AtomicInteger noteIdCounter = new AtomicInteger(1);

  public NotesController(
      ConcurrentHashMap<Integer, User> users, ConcurrentHashMap<Integer, Note> notes) {
    this.users = users;
    this.notes = notes;
  }

  /**
   * POST /notes Body attendu : { "noteTitle": "...", "noteContent": "..." } Nécessite le cookie
   * "user". Réponses : 201 si création 400 si corps invalide 401 si utilisateur non connecté 409 si
   * la noteTitle existe déjà (on considère "unique" dans tout le système ou pour le même user)
   */
  public void createNote(Context ctx) {
    // Get the user from the cookie
    Integer currentUserId = getUserIdFromCookie(ctx);

    // Deserialize the note from the request body
    CreateOrUpdateNoteRequest request =
        ctx.bodyValidator(CreateOrUpdateNoteRequest.class)
            .check(req -> req.noteTitle != null, "Missing noteTitle")
            .check(req -> req.noteContent != null, "Missing noteContent")
            .get();

    // Create the note
    int noteId = noteIdCounter.getAndIncrement();
    Note note = new Note(noteId, currentUserId, request.noteTitle, request.noteContent);
    notes.put(noteId, note);

    ctx.status(HttpStatus.CREATED);
    ctx.json(note);
  }

  /**
   * PUT /notes/{noteId} Body attendu : { "noteTitle": "...", "noteContent": "..." } Nécessite le
   * cookie "user". Réponses : 200 si mise à jour 400 si corps invalide 401 si utilisateur non
   * connecté 404 si la note n'existe pas OU n'appartient pas à l'utilisateur 409 si le nouveau
   * titre existe déjà
   */
  public void updateNote(Context ctx) {
    // Check session
    Integer currentUserId = getUserIdFromCookie(ctx);

    // Get the note ID from the path
    Integer noteId = ctx.pathParamAsClass("id", Integer.class).get();

    // Deserialize the note from the request body
    CreateOrUpdateNoteRequest request =
        ctx.bodyValidator(CreateOrUpdateNoteRequest.class)
            .check(req -> req.noteTitle != null, "Missing noteTitle")
            .check(req -> req.noteContent != null, "Missing noteContent")
            .get();

    // Check if the note exists
    Note note = notes.get(noteId);

    if (note == null) {
      throw new NotFoundResponse("Note not found");
    }

    // Check if the note belongs to the user
    if (!note.userId.equals(currentUserId)) {
      throw new NotFoundResponse("Note not found");
    }

    // Update the note
    note.noteTitle = request.noteTitle;
    note.noteContent = request.noteContent;

    ctx.json(note);
  }

  /**
   * GET /notes Nécessite le cookie "user". Renvoie la liste de toutes les notes de l'utilisateur.
   * Réponses : 200 si ok 401 si pas connecté
   */
  public void getAllNotes(Context ctx) {
    // Check session
    Integer currentUserId = getUserIdFromCookie(ctx);

    // Get the notes of the user
    ctx.json(notes.values().stream().filter(note -> note.userId.equals(currentUserId)).toArray());
  }

  /**
   * GET /notes/{noteId} Nécessite le cookie "user". Renvoie la note si elle appartient à
   * l'utilisateur. Réponses : 200 si ok 401 si pas connecté 404 si la note n'existe pas ou
   * n'appartient pas à l'utilisateur
   */
  public void getOneNote(Context ctx) {
    // Check session
    Integer currentUserId = getUserIdFromCookie(ctx);

    // Get the note ID from the path
    Integer noteId = ctx.pathParamAsClass("id", Integer.class).get();

    // Get the note
    Note note = notes.get(noteId);

    if (note == null || !note.userId.equals(currentUserId)) {
      throw new NotFoundResponse("Note not found");
    }

    ctx.json(note);
  }

  /**
   * DELETE /notes/{noteId} Nécessite le cookie "user". Réponses : 204 si ok 401 si pas connecté 404
   * si la note n'existe pas ou n'appartient pas à l'utilisateur
   */
  public void deleteOneNote(Context ctx) {
    // Check session
    Integer currentUserId = getUserIdFromCookie(ctx);

    // Get the note ID from the path
    Integer noteId = ctx.pathParamAsClass("id", Integer.class).get();

    // Get the note
    Note note = notes.get(noteId);

    if (note == null || !note.userId.equals(currentUserId)) {
      throw new NotFoundResponse("Note not found");
    }

    notes.remove(noteId);
    ctx.status(HttpStatus.NO_CONTENT);
  }

  private Integer getUserIdFromCookie(Context ctx) {
    String userIdCookie = ctx.cookie("user");

    if (userIdCookie == null) {
      throw new UnauthorizedResponse("Missing or invalid user cookie");
    }

    Integer userId;
    try {
      userId = Integer.parseInt(userIdCookie);
    } catch (NumberFormatException e) {
      throw new UnauthorizedResponse("Invalid user cookie");
    }

    User user = users.get(userId);
    if (user == null) {
      throw new UnauthorizedResponse("No user found for this cookie");
    }

    return userId;
  }

  private static class CreateOrUpdateNoteRequest {
    public String noteTitle;
    public String noteContent;
  }
}
