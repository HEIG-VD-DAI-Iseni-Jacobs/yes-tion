package ch.heigvd.dai.users;

import static ch.heigvd.dai.utils.CookieUtils.getUserIdFromCookie;

import ch.heigvd.dai.notes.Note;
import io.javalin.http.*;
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
  private final AtomicInteger userIdCounter = new AtomicInteger(1);

  public UsersController(
      ConcurrentHashMap<Integer, User> users, ConcurrentHashMap<Integer, Note> notes) {
    this.users = users;
    this.notes = notes;
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

    ctx.status(HttpStatus.CREATED);
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
    User user = users.get(userId);
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
    // delete the notes of the user
    notes.values().removeIf(note -> note.userId.equals(userId));
    users.remove(userId);
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
