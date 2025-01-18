package ch.heigvd.dai.users;

public class User {
  public Integer userId;
  public String firstName;
  public String lastName;
  public String email;

  public User() {
    // Empty constructor for serialisation/deserialization
  }

  public User(Integer id, String firstName, String lastName, String email) {
    this.userId = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }
}
