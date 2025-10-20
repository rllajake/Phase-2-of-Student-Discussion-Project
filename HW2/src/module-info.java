module FoundationsF25 {
	requires javafx.controls;
	requires java.sql;
	requires javafx.fxml;
	
	opens applicationMain to javafx.graphics, javafx.fxml;
}
