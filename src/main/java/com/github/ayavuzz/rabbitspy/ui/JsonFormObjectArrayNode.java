package com.github.ayavuzz.rabbitspy.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.ayavuzz.rabbitspy.util.JsonSchemaUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;

public class JsonFormObjectArrayNode extends StackPane { // NOSONAR

  @FXML
  private TableView<String[]> tableOfArray;

  @FXML
  private TextField textRowSize;

  @FXML
  private Label rowNumLbl;

  @FXML
  private Button addButton;


  private JsonNode propertiesList;
  private List<String> innerColumnList;

  public JsonFormObjectArrayNode(JsonNode jsonNode) {
    this.propertiesList = jsonNode;
    this.innerColumnList = JsonSchemaUtil.getFieldsFromJsonNode(propertiesList);
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/JsonFormObjectArrayNode.fxml"));
      loader.setRoot(this);
      loader.setController(this);
      loader.load();
    } catch (IOException e) {
      // TODOAuto-generated
    }
  }

  @FXML
  public void initialize() { // neden işe yaramıyor??
    Platform.runLater(() -> {
      addBtnClicked(null);
      tableOfArray.setEditable(true);
    });
  }

  public String getJsonString() {
    if (!tableOfArray.getItems().isEmpty()) {
      StringBuilder builder = new StringBuilder();
      tableOfArray.getItems().forEach(arr -> {
        List<String> list = Arrays.asList(arr);
        builder.append("[" + "\"" + String.join("\",\"", list) + "\"" + "],");
      });
      return builder.toString().substring(0, builder.toString().length() - 1);
    } else {
      return "";
    }

  }

  private String[] convertToArr(String arrayString) {
    arrayString = arrayString.replace("[", "");
    arrayString = arrayString.replace("]", "");
    arrayString = arrayString.replace("\"", "");
    List<String> items =
        Arrays.asList(arrayString.split("\\s*,\\s*"));

    return items.toArray(new String[0]);
  }

  public void appendList(String arrayString) {
    String arraySubstr = arrayString.substring(1, arrayString.length() - 1);

    List<String[]> strArrList = new ArrayList<>();
    int startIndex = 0;
    int endIndex = 0;
    int len = arraySubstr.length();
    for (int i = 0; i < len; i++) {
      if (arraySubstr.charAt(i) == '[') {
        startIndex = i;
        for (int j = i + 1; j < len; j++) {
          if (arraySubstr.charAt(j) == ']') {
            endIndex = j;
            String[] subArr = convertToArr(arraySubstr.substring(startIndex, endIndex + 1));
            strArrList.add(subArr);
            break;
          }
        }
      }
    }

    tableOfArray.getColumns().setAll(createColumns());
    tableOfArray.getItems().addAll(strArrList);
    tableOfArray.layout();
  }

  private List<TableColumn<String[], String>> createColumns() {
    List<TableColumn<String[], String>> columns = new ArrayList<>();
    for (int i = 0; i < innerColumnList.size(); i++) {
      int c = i;
      TableColumn<String[], String> innerColumn = new TableColumn<>(innerColumnList.get(i));
      innerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
      innerColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()[c]));
      innerColumn.setOnEditCommit(
          (CellEditEvent<String[], String> t) -> {
            (t.getTableView().getItems().get(t.getTablePosition().getRow()))[c] = t.getNewValue();
          });
      columns.add(innerColumn);
    }
    return columns;
  }

  private List<String[]> generateData(int rowSize) {
    List<String[]> arr = new ArrayList<>();

    for (int i = 0; i < rowSize; i++) {
      List<String> list = new ArrayList<>();
      for (int j = 0; j < innerColumnList.size(); j++) {
        String jsonNodeType = propertiesList.get(innerColumnList.get(j)).get("type").toString().replace("\"", "");
        switch (jsonNodeType) {
          case "array":
            list.add("array");
            break;
          case "integer":
            int randomNum = new Random().nextInt(99);
            list.add(String.valueOf(randomNum));
            break;
          case "string":
            list.add("");
            break;
          default:
            list.add("");
            break;
        }
      }
      arr.add(list.toArray(new String[0]));
    }

    return arr;
  }

  @FXML
  void addBtnClicked(ActionEvent event) {
    tableOfArray.setEditable(true);

    tableOfArray.getColumns().setAll(createColumns());
    // tableOfArray.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    int rowSize = Integer.parseInt(textRowSize.getText());
    tableOfArray.getItems().addAll(generateData(rowSize));
    tableOfArray.layout();
  }

}
