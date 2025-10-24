package guiMyPosts;

import java.sql.Timestamp;

public class PostItem {
    public final long id;
    public final String author;
    public final String thread;   // category
    public String title;
    public final String kind;     // "Question" or "Post"
    public String content;
    public final boolean isAnonymous;
    public final boolean isPrivate;
    public final Timestamp createdAt;
    public int replyCount;
    public int unreadReplyCount; // NEW
    public boolean isDeleted;

    public PostItem(long id, String author, String thread, String title, String kind,
                    String content, boolean isAnonymous, boolean isPrivate,
                    Timestamp createdAt, int replyCount) {
        this(id, author, thread, title, kind, content, isAnonymous, isPrivate, createdAt, replyCount, 0);
    }

    public PostItem(long id, String author, String thread, String title, String kind,
                    String content, boolean isAnonymous, boolean isPrivate,
                    Timestamp createdAt, int replyCount, int unreadReplyCount) {
        this.id = id; this.author = author; this.thread = thread; this.title = title;
        this.kind = kind; this.content = content; this.isAnonymous = isAnonymous;
        this.isPrivate = isPrivate; this.createdAt = createdAt; this.replyCount = replyCount;
        this.unreadReplyCount = unreadReplyCount;
    }

    public String safeTitle() {
        String t = title == null ? "" : title.trim();
        if (!t.isEmpty()) return t;
        String body = content == null ? "" : content.trim();
        return body.length() > 60 ? body.substring(0,60) + "…" : body;
    }
}