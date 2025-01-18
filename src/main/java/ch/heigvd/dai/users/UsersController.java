package ch.heigvd.dai.users;

import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UsersController {

  private final ConcurrentHashMap<Integer, User> users;
  private final AtomicInteger userIdCounter = new AtomicInteger(1);

  public UsersController(ConcurrentHashMap<Integer, User> users) {
    this.users = users;
  }

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
    User user = new User();
    user.userId = userIdCounter.getAndIncrement();
    user.firstName = request.firstName;
    user.lastName = request.lastName;
    user.email = request.email;

    // store the user
    users.put(user.userId, user);

    ctx.status(HttpStatus.CREATED);
    ctx.json(user);
  }

  private static class CreateUserRequest {
    public String firstName;
    public String lastName;
    public String email;
  }
}
