package ch.heigvd.dai.utils;

import ch.heigvd.dai.users.User;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import java.util.concurrent.ConcurrentHashMap;

public class CookieUtils {

  /**
   * Retrieves the user ID from the cookie.
   *
   * @param ctx the Javalin context of the request
   * @return the authenticated user's ID
   * @throws UnauthorizedResponse if the user cookie is missing, invalid, or does not map to a user
   */
  public static Integer getUserIdFromCookie(Context ctx, ConcurrentHashMap<Integer, User> users) {
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

    return userId;
  }
}
