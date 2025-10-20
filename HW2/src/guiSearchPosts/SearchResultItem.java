package guiSearchPosts;

import java.sql.Timestamp;

public class SearchResultItem {
    public final long id;
    public final String author;
    public final String thread;     // category
    public final String title;
    public final String kind;       // "Question" or "Post"
    public final String content;
    public final boolean isAnonymous;
    public final boolean isPrivate;
    public final Timestamp createdAt;
    public final int replyCount;

    public SearchResultItem(long id, String author, String thread, String title, String kind,
                            String content, boolean isAnonymous, boolean isPrivate,
                            Timestamp createdAt, int replyCount) {
        this.id = id; this.author = author; this.thread = thread;
        this.title = title; this.kind = kind; this.content = content;
        this.isAnonymous = isAnonymous; this.isPrivate = isPrivate;
        this.createdAt = createdAt; this.replyCount = replyCount;
    }

    public String displayTitle() {
        String t = title == null ? "" : title.trim();
        if (!t.isEmpty()) return t;
        String body = content == null ? "" : content.trim();
        return body.length() > 60 ? body.substring(0,60) + "…" : body;
    }

    public String displayAuthor() {
        if (isAnonymous) return "Anonymous";
        return (author == null || author.isBlank()) ? "Anonymous" : author;
    }
}