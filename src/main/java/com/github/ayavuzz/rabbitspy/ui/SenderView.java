package com.github.ayavuzz.rabbitspy.ui;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SenderView extends VBox { // NOSONAR

  private SenderViewControl viewControl;

  @FXML
  private StackPane jsonNodeFormViewContainer;

  @FXML
  private Label periodLbl;

  @FXML
  private TextField periodText;

  @FXML
  private Button publishButton;

  @FXML
  private CheckBox continuousSendBox;

  private JsonFormNode jsonFormNode;

  private String topicName;

  private ScheduledExecutorService scheduler;

  private static final String PUBLISH_TEXT = "Publish";


  public SenderView(SenderViewControl viewControl, String topicName, JsonFormNode jsonFormNode) {
    this.jsonFormNode = jsonFormNode;
    this.topicName = topicName;
    this.viewControl = viewControl;
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/SenderView.fxml"));
      loader.setRoot(this);
      loader.setController(this);
      loader.load();
    } catch (IOException e) {
      // TODOAuto-generated
    }
  }

  @FXML
  public void initialize() {
    jsonNodeFormViewContainer.getChildren().add(jsonFormNode);
  }

  @FXML
  public void onActionPublishBtn(ActionEvent event) {
    Platform.runLater(() -> {
      if (scheduler != null && !scheduler.isShutdown() && publishButton.getText().equals("Stop")) {
        scheduler.shutdown();
        publishButton.setText(PUBLISH_TEXT);
        scheduler = Executors.newScheduledThreadPool(1);
      } else if (scheduler != null && !scheduler.isShutdown() && publishButton.getText().equals(PUBLISH_TEXT)) {
        String resultJson = filledJsonToString(jsonFormNode);

        Runnable runnable = () -> viewControl.publishMessage(topicName, resultJson);

        int period = periodText.getText().isEmpty() ? 1 : Integer.parseInt(periodText.getText());
        scheduler.scheduleAtFixedRate(runnable, 0, period, TimeUnit.SECONDS);
        publishButton.setText("Stop");
      } else {
        String resultJson = filledJsonToString(jsonFormNode);

        viewControl.publishMessage(topicName, resultJson);
      }
    });
  }

  @FXML
  public void continuousSendEvent(ActionEvent event) {

    Platform.runLater(() -> {

      if (scheduler == null || scheduler.isShutdown()) {
        scheduler = Executors.newScheduledThreadPool(1);
      } else {
        scheduler.shutdown();
        publishButton.setText(PUBLISH_TEXT);
      }
    });
  }

  @FXML
  public void onActionAutoFillBtn(ActionEvent event) {
    Platform.runLater(() -> setRandomDataForNodes(this.jsonFormNode));
  }

  private void setRandomDataForNodes(JsonFormNode formNode) {
    for (Node nodeOfInput : formNode.getChildren()) {

      if (nodeOfInput instanceof TextField) {
        TextField textField = (TextField) nodeOfInput;
        if (textField.getUserData().equals(EnumNodeType.INTEGER)
            || textField.getUserData().equals(EnumNodeType.NUMBER)) {
          int randomNum = new Random().nextInt(255);
          textField.setText(String.valueOf(randomNum));
        }
      } else if (nodeOfInput instanceof ComboBox<?>) {
        ComboBox<String> comboBox = (ComboBox<String>) nodeOfInput;
        comboBox.setValue(comboBox.getItems().get(0));
      } else if (nodeOfInput instanceof JsonFormArrayNode) {
        JsonFormArrayNode arrayNode = (JsonFormArrayNode) nodeOfInput;
        arrayNode.setRandomData();
      } else if (nodeOfInput instanceof JsonFormTwoDimArrayNode) {
        JsonFormTwoDimArrayNode arrayNode = (JsonFormTwoDimArrayNode) nodeOfInput;
        arrayNode.setRandomData();
      } else if (nodeOfInput instanceof JsonFormNode) {
        JsonFormNode node = (JsonFormNode) nodeOfInput;
        setRandomDataForNodes(node);
      }
      // Without requesting focus to any node, data cannot be seen
      nodeOfInput.requestFocus();
    }
  }

  public String filledJsonToString(JsonFormNode jsonNodeForm) {

    StringBuilder sb = new StringBuilder();
    sb.append("{");

    for (Node nodeOfInput : jsonNodeForm.getChildren()) {

      if (nodeOfInput instanceof Label) {
        Label label = (Label) nodeOfInput;
        sb.append("\"" + label.getText() + "\":");
      } else if (nodeOfInput instanceof JsonFormNode) {
        sb.append(filledJsonToString((JsonFormNode) nodeOfInput));
        sb.append(",");
      } else if (nodeOfInput instanceof TextField) {
        TextField textField = (TextField) nodeOfInput;
        if (textField.getUserData().equals(EnumNodeType.INTEGER)
            || textField.getUserData().equals(EnumNodeType.NUMBER)) {
          sb.append(textField.getText() + ",");
        } else if (textField.getUserData().equals(EnumNodeType.STRING)) {
          sb.append("\"" + textField.getText() + "\",");
        }
      } else if (nodeOfInput instanceof CheckBox) {
        CheckBox checkBox = (CheckBox) nodeOfInput;
        sb.append(checkBox.isSelected() + ",");
      } else if (nodeOfInput instanceof ComboBox<?>) {
        ComboBox<String> comboBox = (ComboBox<String>) nodeOfInput;
        sb.append("\"" + comboBox.getValue() + "\",");
      } else if (nodeOfInput instanceof JsonFormArrayNode) {
        JsonFormArrayNode arrayNode = (JsonFormArrayNode) nodeOfInput;
        sb.append("[" + arrayNode.getJsonString() + "],");
      } else if (nodeOfInput instanceof JsonFormTwoDimArrayNode) {
        JsonFormTwoDimArrayNode arrayNode = (JsonFormTwoDimArrayNode) nodeOfInput;
        sb.append("[" + arrayNode.getJsonString() + "],");
      } else if (nodeOfInput instanceof JsonFormObjectArrayNode) {
        JsonFormObjectArrayNode arrayNode = (JsonFormObjectArrayNode) nodeOfInput;
        sb.append("[" + arrayNode.getJsonString() + "],");
      }

    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("}");
    return sb.toString();
  }

  static interface SenderViewControl {
    public void publishMessage(String topicName, String jsonData);

  }

}
