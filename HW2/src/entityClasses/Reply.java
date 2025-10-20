
package entityClasses;

import java.time.LocalDateTime;

public class Reply {
    public long id;
    public long postId;
    public String author;
    public String content;
    public LocalDateTime createdAt;
}
