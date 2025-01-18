package ch.heigvd.dai.notes;

import ch.heigvd.dai.users.User;
import io.javalin.http.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NotesController {
    private final ConcurrentHashMap<Integer, Note> notes;
    private final ConcurrentHashMap<Integer, User> users;
    private final AtomicInteger noteIdCounter = new AtomicInteger(1);

    public NotesController(ConcurrentHashMap<Integer, User> users, ConcurrentHashMap<Integer, Note> notes) {
        this.users = users;
        this.notes = notes;
    }

    public void create(Context ctx) {
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

    private static class CreateOrUpdateNoteRequest {
        public String noteTitle;
        public String noteContent;
    }
}
