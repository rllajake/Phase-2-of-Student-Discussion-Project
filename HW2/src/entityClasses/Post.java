
package entityClasses;

import java.time.LocalDateTime;

public class Post {
    public long id;
    public String author;
    public String thread;
    public String content;
    public boolean deleted;
    public LocalDateTime createdAt;
    public int replyCount;
    public int unreadCount;
}
