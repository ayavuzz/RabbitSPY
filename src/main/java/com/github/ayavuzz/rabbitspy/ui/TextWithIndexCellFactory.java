package com.github.ayavuzz.rabbitspy.ui;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public class TextWithIndexCellFactory extends ListCell<String> { // NOSONAR

  private HBox hbox = new HBox(10);
  private Label label = new Label();
  private Label text = new Label();

  public TextWithIndexCellFactory() {
    super();
    label.setStyle("-fx-font-weight: bold;");
    hbox.getChildren().addAll(label, text);
  }

  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);
    setText(null);
    if (empty) {
      setGraphic(null);
    } else {
      text.setText(item != null ? item : "null");
      label.setText(String.valueOf(getIndex() + 1));
      setGraphic(hbox);
    }
  }

}
