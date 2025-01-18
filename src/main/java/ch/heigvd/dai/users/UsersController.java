package ch.heigvd.dai.users;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller responsible for managing users. Supports user creation and ensures that email
 * addresses are unique.
 */
public class UsersController {

  private final ConcurrentHashMap<Integer, User> users;
  private final AtomicInteger userIdCounter = new AtomicInteger(1);

  public UsersController(ConcurrentHashMap<Integer, User> users) {
    this.users = users;
  }

  /**
   * Creates a new user if the email address is not already in use.
   *
   * @param ctx the Javalin context of the request
   * @throws ConflictResponse if a user with the same email already exists
   * @throws BadRequestResponse if any of the required fields (firstName, lastName, email) are
   *     missing
   */
  public void createUser(Context ctx) {
    // body deserialization
    CreateUserRequest request =
        ctx.bodyValidator(CreateUserRequest.class)
            .check(req -> req.firstName != null, "Missing firstName")
            .check(req -> req.lastName != null, "Missing lastName")
            .check(req -> req.email != null, "Missing email")
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

  /** Class representing the request body for creating a user. */
  private static class CreateUserRequest {
    public String firstName;
    public String lastName;
    public String email;
  }
}
