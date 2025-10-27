package entityClasses;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;



/*******
 * <p> Title: PostCardCell Class. </p>
 * 
 * <p> Description: Custom ListCell implementation to render a PostItem with metadata (title, author, counts) in the posts list.</p>
 * 
 * <p> Copyright: Group 14 © 2025 </p>
 * 
 * @author Group 14
 * 
 * @version 1.00		2025-10-24 Initial documentation alignment for guiReadPosts and guiMyPosts
 */
public class PostCardCell extends ListCell<PostItem> {

    private static Function<Long, Boolean> isReadFn;
    private static BiConsumer<Long, Boolean> setReadFn;

	/**********
	 * <p> Method: setReadProviders() </p>
	 * 
	 * <p> Description: Updates internal state and/or UI controls accordingly.</p>
	 */    public static void setReadProviders(Function<Long, Boolean> isFn, BiConsumer<Long, Boolean> setFn) {
        isReadFn = isFn;
        setReadFn = setFn;
    }

    private final VBox root = new VBox(6);
    private final HBox topRow = new HBox(8);
    private final CheckBox readBox = new CheckBox();
    private final Label dot = new Label("•"); // visual status
    private final Label title = new Label();

    private final HBox metaRow = new HBox(8);
    private final Label category = new Label();
    private final Label kind = new Label();
    private final Label counts = new Label();
    private final Region spacer = new Region();

    public PostCardCell() {
        super();
        selectedProperty().addListener((obs, was, isSel) -> {
            // gray the dot when the row is selected (visual “opened”)
            dot.setStyle(isSel ? "-fx-opacity:0.4;" : "");
        });

        HBox.setHgrow(spacer, Priority.ALWAYS);

        // classes for CSS (existing theme picks these up)
        title.getStyleClass().add("post-title");
        category.getStyleClass().add("post-meta");
        kind.getStyleClass().add("post-meta");
        counts.getStyleClass().add("post-counts");
        dot.getStyleClass().add("post-unread-dot");
        root.getStyleClass().add("post-row");

        root.setPadding(new Insets(6, 8, 6, 8));
        metaRow.setAlignment(Pos.CENTER_LEFT);

        topRow.getChildren().addAll(readBox, dot, title);
        metaRow.getChildren().addAll(category, kind, spacer, counts);
        root.getChildren().addAll(topRow, metaRow);
    }

    @Override

	/**********
	 * <p> Method: updateItem() </p>
	 * 
	 * <p> Description: Updates internal state and/or UI controls accordingly.</p>
	 */    protected void updateItem(PostItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        // Author / title
        final String displayAuthor = item.isAnonymous ? "Anonymous"
                : (item.author == null || item.author.isBlank() ? "Anonymous" : item.author);
        final String safeTitle = (item.title == null || item.title.isBlank()) ? "(untitled)" : item.title.trim();
        title.setText(safeTitle);

        // Left metadata: thread • author
        final String threadText = (item.thread == null || item.thread.isBlank()) ? "General" : item.thread.trim();
        category.setText(threadText + " • " + displayAuthor);

        // Kind
        final String displayKind = (item.kind == null || item.kind.isBlank()) ? "Post" : item.kind.trim();
        kind.setText("• " + displayKind);

        // Reply counts + unread styling
        String c = item.replyCount + " repl" + (item.replyCount == 1 ? "y" : "ies");
        if (item.unreadReplyCount > 0) {
            c += " · " + item.unreadReplyCount + " unread";
            title.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");
        } else {
            title.setStyle("-fx-font-size:14px; -fx-font-weight:normal;");
        }
        counts.setText(c);

        // Checkbox wiring
        if (isReadFn != null) {
            boolean isRead = false;
            try { isRead = isReadFn.apply(item.id); } catch (Exception ignored) {}
            readBox.setSelected(isRead);
        } else {
            readBox.setSelected(false);
        }
        readBox.setOnAction(e -> {
            if (setReadFn != null) {
                try { setReadFn.accept(item.id, readBox.isSelected()); } catch (Exception ignored) {}
            }
        });

        // Subtle dim if post is deleted
        if (item.isDeleted || "[This post has been deleted]".equals(item.content)) {
            root.setOpacity(0.8);
            title.setStyle("-fx-font-size:14px; -fx-font-weight:normal;");
        } else {
            root.setOpacity(1.0);
        }

        setGraphic(root);
    }
}
