package ch.heigvd.dai.notes;

public class Note {
    public Integer noteId;
    public Integer userId;
    public String noteTitle;
    public String noteContent;

    public Note() {
        // Empty constructor for serialisation/deserialization
    }

    public Note(Integer noteId, Integer userId, String noteTitle, String noteContent) {
        this.noteId = noteId;
        this.userId = userId;
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
    }
}
