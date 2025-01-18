package ch.heigvd.dai.notes;

import ch.heigvd.dai.users.User;
import io.javalin.http.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller responsible for handling operations related to notes. Supports CRUD operations for
 * notes and ensures notes are linked to the correct user session.
 */
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
   * Creates a new note for the authenticated user.
   *
   * @param ctx the Javalin context of the request
   * @throws UnauthorizedResponse if the user is not authenticated
   * @throws BadRequestResponse if the note's title or content is missing
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
   * Updates an existing note for the authenticated user.
   *
   * @param ctx the Javalin context of the request
   * @throws UnauthorizedResponse if the user is not authenticated
   * @throws NotFoundResponse if the note is not found or does not belong to the user
   * @throws BadRequestResponse if the note title or content is missing
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
   * Retrieves all notes for the authenticated user.
   *
   * @param ctx the Javalin context of the request
   * @throws UnauthorizedResponse if the user is not authenticated
   */
  public void getAllNotes(Context ctx) {
    // Check session
    Integer currentUserId = getUserIdFromCookie(ctx);

    // Get the notes of the user
    ctx.json(notes.values().stream().filter(note -> note.userId.equals(currentUserId)).toArray());
  }

  /**
   * Retrieves a single note by its ID for the authenticated user.
   *
   * @param ctx the Javalin context of the request
   * @throws UnauthorizedResponse if the user is not authenticated
   * @throws NotFoundResponse if the note is not found or does not belong to the user
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
   * Deletes a single note by its ID for the authenticated user.
   *
   * @param ctx the Javalin context of the request
   * @throws UnauthorizedResponse if the user is not authenticated
   * @throws NotFoundResponse if the note is not found or does not belong to the user
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

  /**
   * Retrieves the user ID from the cookie.
   *
   * @param ctx the Javalin context of the request
   * @return the authenticated user's ID
   * @throws UnauthorizedResponse if the user cookie is missing, invalid, or does not map to a user
   */
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

  /** Class representing the request body for creating or updating a note. */
  private static class CreateOrUpdateNoteRequest {
    public String noteTitle;
    public String noteContent;
  }
}
