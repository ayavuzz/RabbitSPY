package com.github.ayavuzz.rabbitspy.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

public class JsonFormTwoDimArrayNode extends StackPane { // NOSONAR

  @FXML
  private TableView<String[]> tableOfArray;

  @FXML
  private TableView<String[]> tableOfIndex;

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

  @FXML
  private TextField textNmbOfRow;

  @FXML
  private TextField textNmbOfColumn;

  @FXML
  private Button cleanBtn;

  private EnumNodeType nodeType;

  private int nmbOfRow;

  private int nmbOfColumn;

  private ScrollBar tableOfArraySBar;

  private ScrollBar tableOfIndexSBar;

  private static final String SCROLL_BAR_VERTICAL = ".scroll-bar:vertical";

  public JsonFormTwoDimArrayNode(EnumNodeType nodeType) {
    this.nodeType = nodeType;
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/JsonFormTwoDimArrayNode.fxml"));
      loader.setRoot(this);
      loader.setController(this);
      loader.load();
    } catch (IOException e) {
      // TODOAuto-generated
    }
  }

  public void setRandomData() {
    cleanBtnClicked(null);

    textMin.setText(String.valueOf(new Random().nextInt(125)));
    textMax.setText(String.valueOf(125 + new Random().nextInt(255)));
    textNmbOfRow.setText(String.valueOf(new Random().nextInt(125)));
    textNmbOfColumn.setText(String.valueOf(new Random().nextInt(125)));

    generateBtnClicked(null);
  }

  @FXML
  public void initialize() {
    tableOfArray.setEditable(true);
    tableOfArray.setPlaceholder(new Label(""));
    List<TableColumn<String[], String>> indexColumnList = createColumns(1);
    tableOfIndex.getColumns().setAll(indexColumnList);
    tableOfIndex.setPlaceholder(new Label(""));

    tableOfIndex.setRowFactory(new Callback<TableView<String[]>, TableRow<String[]>>() { // NOSONAR
      @Override
      public TableRow<String[]> call(TableView<String[]> param) {
        return new TableRow<String[]>() { // NOSONAR
          @Override
          protected void updateItem(String[] item, boolean empty) {
            if (item != null) {
              setStyle("-fx-font-weight: bold");
            }
          }
        };
      }
    });
  }

  public String getJsonString() {
    StringBuilder builder = new StringBuilder();
    if (nodeType.equals(EnumNodeType.STRING)) {
      tableOfArray.getItems().forEach(arr -> {
        List<String> list = Arrays.asList(arr);
        builder.append("[" + "\"" + String.join("\",\"", list) + "\"" + "],");
      });
    } else {
      tableOfArray.getItems().forEach(arr -> {
        List<String> list = Arrays.asList(arr);
        builder.append("[" + String.join(",", list) + "],");
      });
    }
    return builder.toString().substring(0, builder.toString().length() - 1);
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

    nmbOfRow = strArrList.size();
    nmbOfColumn = strArrList.get(0).length;
    textNmbOfRow.setText(String.valueOf(nmbOfRow));
    textNmbOfColumn.setText(String.valueOf(nmbOfColumn));

    tableOfArray.getColumns().setAll(createColumns(nmbOfColumn));
    tableOfArray.getItems().addAll(strArrList);
    tableOfArray.layout();

    tableOfIndex.getItems().addAll(generateIndex(nmbOfRow));

    Platform.runLater(() -> {
      tableOfArraySBar = (ScrollBar) tableOfArray.lookup(SCROLL_BAR_VERTICAL);
      tableOfIndexSBar = (ScrollBar) tableOfIndex.lookup(SCROLL_BAR_VERTICAL);
      tableOfIndexSBar.setStyle(
          "-fx-min-width: 0; -fx-pref-width: 0; -fx-max-width: 0; -fx-min-height: 0; -fx-pref-height: 0;-fx-max-height: 0;");
      tableOfArraySBar.valueProperty().bindBidirectional(tableOfIndexSBar.valueProperty());
    });
  }

  private String[] convertToArr(String arrayString) {
    arrayString = arrayString.replace("[", "");
    arrayString = arrayString.replace("]", "");
    List<String> items =
        Arrays.asList(arrayString.split("\\s*,\\s*"));

    return items.toArray(new String[0]);
  }


  @FXML
  void cleanBtnClicked(ActionEvent event) {
    tableOfArray.getItems().clear();
    tableOfArray.getColumns().clear();
    tableOfIndex.getItems().clear();
  }

  @FXML
  void generateBtnClicked(ActionEvent event) {
    nmbOfRow = Integer.parseInt(textNmbOfRow.getText());
    nmbOfColumn = Integer.parseInt(textNmbOfColumn.getText());

    String minStr = textMin.getText();
    String maxStr = textMax.getText();
    tableOfArray.getColumns().setAll(createColumns(nmbOfColumn));


    if (minStr.contains(".") || minStr.contains(",")) {
      if (minStr.contains(",")) {
        minStr = minStr.replace(",", ".");
        maxStr = maxStr.replace(",", ".");
      }
      double min = Double.parseDouble(minStr);
      double max = Double.parseDouble(maxStr);
      tableOfArray.getItems().addAll(generateData(nmbOfRow, nmbOfColumn, min, max));
    } else {
      int min = Integer.parseInt(minStr);
      int max = Integer.parseInt(maxStr);
      tableOfArray.getItems().addAll(generateData(nmbOfRow, nmbOfColumn, min, max));
    }

    tableOfArray.layout();
    textMax.clear();
    textMin.clear();

    tableOfIndex.getItems().addAll(generateIndex(nmbOfRow));

    tableOfArraySBar = (ScrollBar) tableOfArray.lookup(SCROLL_BAR_VERTICAL);
    tableOfIndexSBar = (ScrollBar) tableOfIndex.lookup(SCROLL_BAR_VERTICAL);
    tableOfIndexSBar.setStyle(
        "-fx-min-width: 0; -fx-pref-width: 0; -fx-max-width: 0; -fx-min-height: 0; -fx-pref-height: 0; -fx-max-height: 0;");
    tableOfArraySBar.valueProperty().bindBidirectional(tableOfIndexSBar.valueProperty());
  }


  private List<String[]> generateData(int rowNmb, int columnNmb, int min, int max) {
    List<String[]> arr = new ArrayList<>();

    for (int i = 0; i < rowNmb; i++) {
      List<String> list = new ArrayList<>();
      for (int j = 0; j < columnNmb; j++) {
        int randomNum = min + new Random().nextInt(max - min + 1);
        list.add(String.valueOf(randomNum));
      }
      arr.add(list.toArray(new String[0]));
    }

    return arr;
  }

  private List<String[]> generateData(int rowNmb, int columnNmb, double min, double max) {
    List<String[]> arr = new ArrayList<>();

    for (int i = 0; i < rowNmb; i++) {
      List<String> list = new ArrayList<>();
      for (int j = 0; j < columnNmb; j++) {
        double randomNum = min + new Random().nextDouble() * (max - min);
        list.add(String.format(Locale.ROOT, "%.2f", randomNum));
      }
      arr.add(list.toArray(new String[0]));
    }

    return arr;
  }

  private List<String[]> generateIndex(int rowNmb) {
    List<String[]> arr = new ArrayList<>();

    for (int j = 1; j <= rowNmb; j++) {
      List<String> list = new ArrayList<>();
      list.add(String.valueOf(j));
      arr.add(list.toArray(new String[0]));
    }

    return arr;
  }

  private List<TableColumn<String[], String>> createColumns(int columnNum) {
    return IntStream.range(0, columnNum)
        .mapToObj(this::createColumn)
        .collect(Collectors.toList());
  }

  private TableColumn<String[], String> createColumn(int c) {
    TableColumn<String[], String> col = new TableColumn<>(String.valueOf(c + 1));
    col.setCellFactory(TextFieldTableCell.forTableColumn());
    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()[c]));
    col.setOnEditCommit(
        (CellEditEvent<String[], String> t) -> {
          (t.getTableView().getItems().get(t.getTablePosition().getRow()))[c] = t.getNewValue();
        });

    return col;
  }

}
