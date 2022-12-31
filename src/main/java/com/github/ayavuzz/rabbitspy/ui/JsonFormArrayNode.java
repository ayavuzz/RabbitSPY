package com.github.ayavuzz.rabbitspy.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class JsonFormArrayNode extends StackPane { // NOSONAR

  @FXML
  private ListView<String> listOfArrayInput;

  @FXML
  private TextField textToAdd;

  @FXML
  private Button addButton;

  @FXML
  private Button removeButton;

  @FXML
  private Button updateButton;

  @FXML
  private Label lblSize;

  @FXML
  private TextField textArraySize;

  @FXML
  private Label lblMin;

  @FXML
  private TextField textMin;

  @FXML
  private Label lblMax;

  @FXML
  private TextField textMax;

  @FXML
  private Button generateButton;

  private EnumNodeType nodeType;

  private int selectedIndex;

  public JsonFormArrayNode(EnumNodeType nodeType) {
    this.nodeType = nodeType;
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/JsonFormArrayNode.fxml"));
      loader.setRoot(this);
      loader.setController(this);
      loader.load();
    } catch (IOException e) {
      // TODOAuto-generated
    }
  }

  public void setRandomData() {
    listOfArrayInput.getItems().clear();
    textMin.setText(String.valueOf(new Random().nextInt(125)));
    textMax.setText(String.valueOf(125 + new Random().nextInt(255)));
    textArraySize.setText(String.valueOf(new Random().nextInt(125)));

    generateBtnClicked(null);
  }

  @FXML
  public void initialize() {
    textToAdd.setPromptText(nodeType.toString());
    listOfArrayInput.setCellFactory(input -> new TextWithIndexCellFactory());// TextFieldListCell.forListView())
    listOfArrayInput.setEditable(true);
    listOfArrayInput.setOnMouseClicked(event -> {
      String selectedItem = listOfArrayInput.getSelectionModel().getSelectedItem();
      selectedIndex = listOfArrayInput.getSelectionModel().getSelectedIndex();
      textToAdd.setText(selectedItem);
    });
  }

  public String getJsonString() {
    List<String> list = listOfArrayInput.getItems();
    if (nodeType.equals(EnumNodeType.STRING)) {
      return "\"" + String.join("\",\"", list) + "\"";
    } else {
      return String.join(",", list);
    }
  }

  public void appendList(String arrayString) {
    arrayString = arrayString.replace("[", "");
    arrayString = arrayString.replace("]", "");
    List<String> items = Arrays.asList(arrayString.split("\\s*,\\s*"));
    listOfArrayInput.getItems().addAll(items);

  }

  @FXML
  void addBtnClicked(ActionEvent event) {
    if (!textToAdd.getText().isEmpty()) {
      if (textToAdd.getText().contains(",")) { // for double data
        listOfArrayInput.getItems().add(textToAdd.getText().replace(',', '.'));
      } else {
        listOfArrayInput.getItems().add(textToAdd.getText());
      }
    }
    listOfArrayInput.scrollTo(listOfArrayInput.getItems().size() - 1);
    listOfArrayInput.layout();
    textToAdd.clear();
  }

  @FXML
  void removeBtnClicked(ActionEvent event) {
    if (listOfArrayInput.getSelectionModel().getSelectedItem() != null) {
      listOfArrayInput.getItems()
          .remove(listOfArrayInput.getSelectionModel().getSelectedItem());
      textToAdd.clear();
    }
  }

  @FXML
  void updateBtnClicked(ActionEvent event) {
    String oldItem = listOfArrayInput.getSelectionModel().getSelectedItem();
    if (oldItem != null && !textToAdd.getText().isEmpty() && !textToAdd.getText().equals(oldItem)) {
      listOfArrayInput.getItems().remove(selectedIndex);
      listOfArrayInput.getItems().add(selectedIndex, textToAdd.getText());
      textToAdd.clear();
    }
  }

  @FXML
  void generateBtnClicked(ActionEvent event) {
    int min = Integer.parseInt(textMin.getText());
    int max = Integer.parseInt(textMax.getText());
    int arraySize = Integer.parseInt(textArraySize.getText());

    List<String> list = new ArrayList<>();
    for (int i = 0; i < arraySize; i++) {
      int randomNum = min + new Random().nextInt(max - min + 1);
      list.add(String.valueOf(randomNum));
    }

    listOfArrayInput.getItems().addAll(list);
    listOfArrayInput.layout();
    textArraySize.clear();
    textMax.clear();
    textMin.clear();
  }
}
