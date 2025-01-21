package ch.heigvd.dai.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestApplication {

  private final String BASE_URL = "http://localhost:8080";

  @BeforeAll
  public void setup() throws IOException {
    // Add setup if needed, like clearing the database or resetting the app
  }

  @Test
  public void testSignUpAndLogin() throws IOException {
    // Sign up a new user
    String signupPayload =
        "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\"}";
    HttpURLConnection signupConnection = makeRequest("/signup", "POST", signupPayload);

    assertEquals(201, signupConnection.getResponseCode(), "Signup should return 201");

    // Login with the created user
    String loginPayload = "{\"email\":\"john.doe@example.com\"}";
    HttpURLConnection loginConnection = makeRequest("/login", "POST", loginPayload);

    assertEquals(204, loginConnection.getResponseCode(), "Login should return 204");

    // Check if the cookie is set
    String setCookieHeader = loginConnection.getHeaderField("Set-Cookie");
    assertNotNull(setCookieHeader, "Login should set a cookie");
    assertTrue(setCookieHeader.contains("user="), "Cookie should contain 'user'");
  }

  @Test
  public void testCreateAndRetrieveNote() throws IOException {
    // Login to get a cookie
    String loginPayload = "{\"email\":\"john.doe@example.com\"}";
    HttpURLConnection loginConnection = makeRequest("/login", "POST", loginPayload);
    String cookie = loginConnection.getHeaderField("Set-Cookie");

    // Create a note
    String notePayload = "{\"noteTitle\":\"Test Note\",\"noteContent\":\"This is a test.\"}";
    HttpURLConnection createNoteConnection = makeRequest("/notes", "POST", notePayload, cookie);
    assertEquals(201, createNoteConnection.getResponseCode(), "Creating a note should return 201");

    // Get all notes
    HttpURLConnection getNotesConnection = makeRequest("/notes", "GET", null, cookie);
    assertEquals(200, getNotesConnection.getResponseCode(), "Fetching notes should return 200");

    String response = readResponse(getNotesConnection);
    assertTrue(response.contains("Test Note"), "Response should contain 'Test Note'");
  }

  @Test
  public void testLogout() throws IOException {
    // Login to get a cookie
    String loginPayload = "{\"email\":\"john.doe@example.com\"}";
    HttpURLConnection loginConnection = makeRequest("/login", "POST", loginPayload);
    String cookie = loginConnection.getHeaderField("Set-Cookie");

    // Logout
    HttpURLConnection logoutConnection = makeRequest("/logout", "POST", null, cookie);
    assertEquals(204, logoutConnection.getResponseCode(), "Logout should return 204");
    cookie = logoutConnection.getHeaderField("Set-Cookie");

    // Try accessing profile after logout
    HttpURLConnection profileConnection = makeRequest("/profile", "GET", null, cookie);
    assertEquals(
        401,
        profileConnection.getResponseCode(),
        "Accessing profile after logout should return 401");
  }

  private HttpURLConnection makeRequest(String endpoint, String method, String payload)
      throws IOException {
    return makeRequest(endpoint, method, payload, null);
  }

  private HttpURLConnection makeRequest(
      String endpoint, String method, String payload, String cookie) throws IOException {
    URL url = new URL(BASE_URL + endpoint);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setRequestProperty("Content-Type", "application/json");
    if (cookie != null) {
      connection.setRequestProperty("Cookie", cookie);
    }

    if (payload != null) {
      connection.setDoOutput(true);
      try (OutputStream os = connection.getOutputStream()) {
        os.write(payload.getBytes(StandardCharsets.UTF_8));
        os.flush();
      }
    }

    return connection;
  }

  private String readResponse(HttpURLConnection connection) throws IOException {
    try (BufferedReader in =
        new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      StringBuilder response = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      return response.toString();
    }
  }
}
