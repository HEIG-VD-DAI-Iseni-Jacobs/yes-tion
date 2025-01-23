package ch.heigvd.dai.users;

import static ch.heigvd.dai.utils.CookieUtils.getUserIdFromCookie;

import ch.heigvd.dai.notes.Note;
import io.javalin.http.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller responsible for managing users. Supports user creation, authentication, profile
 * management, and ensures that email addresses are unique.
 */
public class UsersController {

  private final ConcurrentHashMap<Integer, User> users;
  private final ConcurrentHashMap<Integer, Note> notes;
  private final ConcurrentHashMap<Integer, LocalDateTime> usersCache;
  private final ConcurrentHashMap<Integer, LocalDateTime> notesCache;
  private final AtomicInteger userIdCounter = new AtomicInteger(1);

  // This is a magic number used to store the users' list last modification date
  // As the ID for users starts from 1, it is safe to reserve the value -1 for all users
  private final Integer RESERVED_ID_TO_IDENTIFY_ALL_USERS = -1;

  public UsersController(
      ConcurrentHashMap<Integer, User> users,
      ConcurrentHashMap<Integer, Note> notes,
      ConcurrentHashMap<Integer, LocalDateTime> usersCache,
      ConcurrentHashMap<Integer, LocalDateTime> notesCache) {
    this.users = users;
    this.notes = notes;
    this.usersCache = usersCache;
    this.notesCache = notesCache;
  }

  /**
   * Creates a new user if the email address is not already in use.
   *
   * @param ctx the Javalin context of the request
   * @throws ConflictResponse if a user with the same email already exists
   * @throws BadRequestResponse if any of the required fields (firstName, lastName, email) are
   *     missing
   */
  public void signUp(Context ctx) {
    // body deserialization
    SignUpOrUpdateRequest request =
        ctx.bodyValidator(SignUpOrUpdateRequest.class)
            .check(req -> req.firstName != null && !req.firstName.isBlank(), "Missing firstName")
            .check(req -> req.lastName != null && !req.lastName.isBlank(), "Missing lastName")
            .check(req -> req.email != null && !req.email.isBlank(), "Missing email")
            .get();

    // check if the email is already used
    for (User existing : users.values()) {
      if (existing.email.equalsIgnoreCase(request.email)) {
        throw new ConflictResponse("User already exists");
      }
    }

    // create the user
    User user =
        new User(
            userIdCounter.getAndIncrement(), request.firstName, request.lastName, request.email);

    // store the user
    users.put(user.userId, user);

    // Store the last modification date of the user
    LocalDateTime now = LocalDateTime.now();
    usersCache.put(user.userId, now);

    // Invalidate the cache for all users
    usersCache.remove(RESERVED_ID_TO_IDENTIFY_ALL_USERS);

    ctx.status(HttpStatus.CREATED);
    // Add the last modification date to the response
    ctx.header("Last-Modified", String.valueOf(now));
    ctx.json(user);
  }

  /**
   * Authenticates a user using their email address. Sets a session cookie if the user exists.
   *
   * @param ctx the Javalin context of the request
   * @throws UnauthorizedResponse if no user with the given email exists
   * @throws BadRequestResponse if the email field is missing
   */
  public void login(Context ctx) {
    String email =
        ctx.bodyValidator(LoginRequest.class)
            .check(req -> req.email != null, "Missing email")
            .get()
            .email;

    // Search for the user with the given email
    for (User user : users.values()) {
      if (user.email.equalsIgnoreCase(email)) {
        ctx.cookie("user", String.valueOf(user.userId));
        ctx.status(HttpStatus.NO_CONTENT);
        return;
      }
    }

    throw new UnauthorizedResponse("E-mail does not exist");
  }

  /**
   * Logs out the current user by removing the session cookie.
   *
   * @param ctx the Javalin context of the request
   */
  public void logout(Context ctx) {
    ctx.removeCookie("user");
    ctx.status(HttpStatus.NO_CONTENT);
  }

  /**
   * Retrieves the profile of the currently logged-in user.
   *
   * @param ctx the Javalin context of the request
   * @throws UnauthorizedResponse if the session cookie is missing or invalid
   */
  public void getProfile(Context ctx) {
    Integer userId = getUserIdFromCookie(ctx, users);
    // Get the last known modification date of the user
    LocalDateTime lastKnownModification =
        ctx.headerAsClass("If-Modified-Since", LocalDateTime.class).getOrDefault(null);

    // Check if the user has been modified since the last known modification date
    if (lastKnownModification != null && usersCache.get(userId).equals(lastKnownModification)) {
      throw new NotModifiedResponse();
    }
    User user = users.get(userId);
    if (user == null) {
      throw new UnauthorizedResponse();
    }
    LocalDateTime now;
    if (usersCache.containsKey(user.userId)) {
      // If it is already in the cache, get the last modification date
      now = usersCache.get(user.userId);
    } else {
      // Otherwise, set to the current date
      now = LocalDateTime.now();
      usersCache.put(user.userId, now);
    }

    // Add the last modification date to the response
    ctx.header("Last-Modified", String.valueOf(now));
    ctx.json(user);
  }

  /**
   * Updates the profile of the currently logged-in user. Allows partial updates of first name, last
   * name, and email.
   *
   * @param ctx the Javalin context of the request
   * @throws ConflictResponse if the new email is already in use by another user
   * @throws UnauthorizedResponse if the session cookie is missing or invalid
   */
  public void updateProfile(Context ctx) {
    Integer userId = getUserIdFromCookie(ctx, users);

    // Get the last known modification date of the user
    LocalDateTime lastKnownModification =
        ctx.headerAsClass("If-Unmodified-Since", LocalDateTime.class).getOrDefault(null);

    // Check if the user has been modified since the last known modification date
    if (lastKnownModification != null && !usersCache.get(userId).equals(lastKnownModification)) {
      throw new PreconditionFailedResponse();
    }

    // body deserialization
    SignUpOrUpdateRequest request = ctx.bodyValidator(SignUpOrUpdateRequest.class).get();

    User user = users.get(userId);

    // check if the email is already used
    if (request.email != null && !request.email.isBlank()) {
      for (User existing : users.values()) {
        if (existing.email.equalsIgnoreCase(request.email)
            && !Objects.equals(existing.userId, userId)) {
          throw new ConflictResponse("User already exists");
        }
      }
      user.email = request.email;
    }

    if (request.firstName != null && !request.firstName.isBlank()) {
      user.firstName = request.firstName;
    }

    if (request.lastName != null && !request.lastName.isBlank()) {
      user.lastName = request.lastName;
    }

    LocalDateTime now;
    if (usersCache.containsKey(user.userId)) {
      // If it is already in the cache, get the last modification date
      now = usersCache.get(user.userId);
    } else {
      // Otherwise, set to the current date
      now = LocalDateTime.now();
      usersCache.put(user.userId, now);

      // Invalidate the cache for all users
      usersCache.remove(RESERVED_ID_TO_IDENTIFY_ALL_USERS);
    }

    // Add the last modification date to the response
    ctx.header("Last-Modified", String.valueOf(now));
    ctx.json(user);
  }

  /**
   * Deletes the profile of the currently logged-in user and logs them out.
   *
   * @param ctx the Javalin context of the request
   * @throws UnauthorizedResponse if the session cookie is missing or invalid
   */
  public void deleteProfile(Context ctx) {
    Integer userId = getUserIdFromCookie(ctx, users);

    // Get the last known modification date of the user
    LocalDateTime lastKnownModification =
        ctx.headerAsClass("If-Unmodified-Since", LocalDateTime.class).getOrDefault(null);

    // Check if the user has been modified since the last known modification date
    if (lastKnownModification != null && !usersCache.get(userId).equals(lastKnownModification)) {
      throw new PreconditionFailedResponse();
    }

    // delete the notes of the user
    notes.values().removeIf(note -> note.userId.equals(userId));
    users.remove(userId);
    // Invalidate the cache for the user
    usersCache.remove(userId);

    // Invalidate the cache for all users
    usersCache.remove(RESERVED_ID_TO_IDENTIFY_ALL_USERS);
    ctx.removeCookie("user");
    ctx.status(HttpStatus.NO_CONTENT);
  }

  /** Class representing the request body for creating a user. */
  private static class SignUpOrUpdateRequest {
    public String firstName;
    public String lastName;
    public String email;
  }

  /** Class representing the request body for logging in a user. */
  private static class LoginRequest {
    public String email;
  }
}
