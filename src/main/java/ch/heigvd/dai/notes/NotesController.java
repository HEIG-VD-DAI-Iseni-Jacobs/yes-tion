package ch.heigvd.dai.notes;

import static ch.heigvd.dai.utils.CookieUtils.getUserIdFromCookie;

import ch.heigvd.dai.users.User;
import io.javalin.http.*;
import java.time.LocalDateTime;
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

  // private final ConcurrentHashMap<Integer, LocalDateTime> usersCache;
  private final ConcurrentHashMap<Integer, LocalDateTime> notesCache;

  private final Integer RESERVED_ID_TO_IDENTIFY_ALL_NOTES = -1;

  public NotesController(
      ConcurrentHashMap<Integer, User> users,
      ConcurrentHashMap<Integer, Note> notes,
      ConcurrentHashMap<Integer, LocalDateTime> notesCache) {
    this.users = users;
    this.notes = notes;
    // this.usersCache = usersCache;
    this.notesCache = notesCache;
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
    Integer currentUserId = getUserIdFromCookie(ctx, users);

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

    // Storthe last modified date of the note
    LocalDateTime now = LocalDateTime.now();
    notesCache.put(noteId, now);

    // Invalidate the cache for all the notes
    notesCache.remove(RESERVED_ID_TO_IDENTIFY_ALL_NOTES);

    ctx.status(HttpStatus.CREATED);

    // Add the last modification date to the response
    ctx.header("Last-Modified", String.valueOf(now));
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
    Integer currentUserId = getUserIdFromCookie(ctx, users);

    // Get the note ID from the path
    Integer noteId = ctx.pathParamAsClass("id", Integer.class).get();

    // Get the last known modification date of the note
    LocalDateTime lastKnownModification =
        ctx.headerAsClass("If-Unmodified-Since", LocalDateTime.class).getOrDefault(null);

    // Check if the note has been modified since the last known modification date
    if (lastKnownModification != null && !notesCache.get(noteId).equals(lastKnownModification)) {
      throw new PreconditionFailedResponse();
    }

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

    LocalDateTime now;
    if (notesCache.containsKey(noteId)) {
      // If it is already in the cache, get the last modification date
      now = notesCache.get(noteId);
    } else {
      // Otherwise, set to the current date
      now = LocalDateTime.now();
      notesCache.put(noteId, now);

      // Invalidate the cache for all notes
      notesCache.remove(RESERVED_ID_TO_IDENTIFY_ALL_NOTES);
    }

    // Add the last modification date to the response
    ctx.header("Last-Modified", String.valueOf(now));

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
    Integer currentUserId = getUserIdFromCookie(ctx, users);

    // Get the last known modification date of all notes
    LocalDateTime lastKnownModification =
        ctx.headerAsClass("If-Modified-Since", LocalDateTime.class).getOrDefault(null);

    // Check if all notes have been modified since the last known modification date
    if (lastKnownModification != null
        && notesCache.containsKey(RESERVED_ID_TO_IDENTIFY_ALL_NOTES)
        && notesCache.get(RESERVED_ID_TO_IDENTIFY_ALL_NOTES).equals(lastKnownModification)) {
      throw new NotModifiedResponse();
    }

    LocalDateTime now;
    if (notesCache.containsKey(RESERVED_ID_TO_IDENTIFY_ALL_NOTES)) {
      // If it is already in the cache, get the last modification date
      now = notesCache.get(RESERVED_ID_TO_IDENTIFY_ALL_NOTES);
    } else {
      // Otherwise, set to the current date
      now = LocalDateTime.now();
      notesCache.put(RESERVED_ID_TO_IDENTIFY_ALL_NOTES, now);
    }

    // Add the last modification date to the response
    ctx.header("Last-Modified", String.valueOf(now));
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
    Integer currentUserId = getUserIdFromCookie(ctx, users);
    // Get the note ID from the path
    Integer noteId = ctx.pathParamAsClass("id", Integer.class).get();
    // Get the note
    Note note = notes.get(noteId);

    // Get the last known modification date of the user
    LocalDateTime lastKnownModification =
        ctx.headerAsClass("If-Modified-Since", LocalDateTime.class).getOrDefault(null);

    // Check if the user has been modified since the last known modification date
    if (lastKnownModification != null && notesCache.get(noteId).equals(lastKnownModification)) {
      throw new NotModifiedResponse();
    }

    if (note == null || !note.userId.equals(currentUserId)) {
      throw new NotFoundResponse("Note not found");
    }

    LocalDateTime now;
    if (notesCache.containsKey(noteId)) {
      // If it is already in the cache, get the last modification date
      now = notesCache.get(noteId);
    } else {
      // Otherwise, set to the current date
      now = LocalDateTime.now();
      notesCache.put(noteId, now);
    }

    // Add the last modification date to the response
    ctx.header("Last-Modified", String.valueOf(now));

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
    Integer currentUserId = getUserIdFromCookie(ctx, users);

    // Get the note ID from the path
    Integer noteId = ctx.pathParamAsClass("id", Integer.class).get();

    // Get the note
    Note note = notes.get(noteId);

    // Get the last known modification date of the user
    LocalDateTime lastKnownModification =
        ctx.headerAsClass("If-Unmodified-Since", LocalDateTime.class).getOrDefault(null);

    // Check if the user has been modified since the last known modification date
    if (lastKnownModification != null && !notesCache.get(noteId).equals(lastKnownModification)) {
      throw new PreconditionFailedResponse();
    }

    if (note == null || !note.userId.equals(currentUserId)) {
      throw new NotFoundResponse("Note not found");
    }

    notes.remove(noteId);
    // Invalidate the cache for the user
    notesCache.remove(noteId);

    // Invalidate the cache for all users
    notesCache.remove(RESERVED_ID_TO_IDENTIFY_ALL_NOTES);
    ctx.status(HttpStatus.NO_CONTENT);
  }

  /** Class representing the request body for creating or updating a note. */
  private static class CreateOrUpdateNoteRequest {
    public String noteTitle;
    public String noteContent;
  }
}
