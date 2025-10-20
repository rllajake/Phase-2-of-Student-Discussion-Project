package guiSearchPosts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;

public class SearchResultCell extends ListCell<SearchResultItem> {

    private final VBox root = new VBox(6);
    private final HBox titleRow = new HBox(8);
    private final Label avatar = new Label(); // placeholder circle/letter
    private final Label title = new Label();
    private final HBox metaRow = new HBox(8);
    private final Label category = new Label();
    private final Label authorTime = new Label();
    private final Label preview = new Label();

    public SearchResultCell() {
        // ---- classes for CSS ----
        root.getStyleClass().add("search-row");
        title.getStyleClass().add("search-title");
        category.getStyleClass().add("search-meta-primary");
        authorTime.getStyleClass().add("search-meta");
        preview.getStyleClass().add("search-preview");

        avatar.setText("●");
        avatar.getStyleClass().add("search-avatar");

        titleRow.getChildren().addAll(avatar, title);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        metaRow.getChildren().addAll(category, authorTime);

        preview.setWrapText(true);

        root.getChildren().addAll(titleRow, preview, metaRow);
        root.setPadding(new Insets(10));
        // transparent so ListView's bg shows through
        root.setStyle("-fx-background-color: transparent;");
        setPadding(new Insets(8, 12, 8, 12));
    }

    @Override
    protected void updateItem(SearchResultItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        title.setText(item.displayTitle());
        preview.setText(snippet(item.content));
        category.setText(item.thread);
        authorTime.setText(item.displayAuthor() + "  " + timeAgo(item.createdAt));
        setGraphic(root);
    }

    private static String snippet(String s) {
        if (s == null) return "";
        String t = s.trim().replace("\n", " ");
        return t.length() > 120 ? t.substring(0,120) + "…" : t;
    }

    private static String timeAgo(java.sql.Timestamp ts) {
        if (ts == null) return "";
        long sec = Math.max(1, (java.time.Duration.between(ts.toInstant(), java.time.Instant.now()).getSeconds()));
        if (sec < 60) return sec + "s ago";
        long min = sec / 60; if (min < 60) return min + "m ago";
        long hr = min / 60; if (hr < 24) return hr + "h ago";
        long d = hr / 24; if (d < 7) return d + "d ago";
        long w = d / 7; if (w < 5) return w + "w ago";
        long mo = d / 30; if (mo < 12) return mo + "mo ago";
        long y = d / 365; return y + "y ago";
    }
}
