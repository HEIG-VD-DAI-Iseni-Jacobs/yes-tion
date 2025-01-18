package ch.heigvd.dai.auth;

import ch.heigvd.dai.users.User;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import java.util.concurrent.ConcurrentHashMap;

public class AuthController {

  private final ConcurrentHashMap<Integer, User> users;

  public AuthController(ConcurrentHashMap<Integer, User> users) {
    this.users = users;
  }

  /**
   * POST /login Body attendu : { "email": "..." } Réponses : 204 si tout va bien 400 si email
   * manquant 401 si l'utilisateur n'existe pas
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

  /** POST /logout Réponse : 204 */
  public void logout(Context ctx) {
    ctx.removeCookie("user");
    ctx.status(HttpStatus.NO_CONTENT);
  }

  /**
   * GET /profile Réponses : 200 : renvoie l'utilisateur (JSON) 401 : si le cookie "user" est
   * manquant ou ne correspond à aucun utilisateur
   */
  public void profile(Context ctx) {
    String userIdCookie = ctx.cookie("user");

    if (userIdCookie == null) {
      throw new UnauthorizedResponse("Missing user cookie");
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

    ctx.json(user);
  }

  private static class LoginRequest {
    public String email;
  }
}
