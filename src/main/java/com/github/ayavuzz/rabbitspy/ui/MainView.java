
package com.github.ayavuzz.rabbitspy.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.ayavuzz.rabbitspy.service.ExchangeDataTypeInfoService;
import com.github.ayavuzz.rabbitspy.util.JsonSchemaUtil;
import com.sun.codemodel.JCodeModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;

public class MainView extends GridPane { // NOSONAR

  @FXML
  private ListView<String> topicListView;

  @FXML
  private StackPane spTableContainer;

  @FXML
  private Button btnOpenSenderWindow;

  @FXML
  private Button clearBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button startBtn;

  private Map<String, TableView<String>> topicNameTableMap = new HashMap<>();

  private MainViewControl viewControl;

  private ExchangeDataTypeInfoService dataTypeInfoService;

  private List<String> activeListenerList = new ArrayList<>();

  public MainView(MainViewControl viewControl) {
    this.viewControl = viewControl;
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
      loader.setRoot(this);
      loader.setController(this);
      loader.load();
    } catch (IOException e) {
      // TODOAuto-generated
    }
  }

  public void handleValue(String key, String value) {
    Platform.runLater(() -> {
      topicNameTableMap.get(key).getItems().add(value);
      topicNameTableMap.get(key).refresh();
    });
  }

  @FXML
  public void initialize() {
    List<String> topicNameList = new ArrayList<>();
    dataTypeInfoService = viewControl.getDataTypeInfoService();

    btnOpenSenderWindow.setVisible(false);

    startBtn.setDisable(true);
    stopBtn.setDisable(true);

    for (String topicName : dataTypeInfoService.getTopicSchemaMap().keySet()) {
      topicNameList.add(topicName);
      String schemaName = dataTypeInfoService.getTopicSchemaMap().get(topicName);
      viewControl.stopListener(topicName);

      List<String> columns = JsonSchemaUtil.getFieldsFromJsonNode(
          dataTypeInfoService.getSchemaPropertiesMap().get(schemaName));

      TableView<String> table = new TableView<>();
      table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

      for (String column : columns) {
        TableColumn<String, String> col = new TableColumn<>(column);
        col.setCellValueFactory(value -> {
          JsonNode testNode = JsonSchemaUtil.stringToJsonNode(value.getValue());
          return new SimpleStringProperty(
              JsonSchemaUtil.getValueWithKey(testNode, column));
        });
        table.getColumns().add(col);

        JsonNode innerNode = dataTypeInfoService.getSchemaPropertiesMap().get(schemaName)
            .get(column);

        if (JsonSchemaUtil.isObject(innerNode)) {
          List<String> innercolumns = JsonSchemaUtil
              .getFieldsFromJsonNode(innerNode.get("properties"));
          for (String innercolumn : innercolumns) {
            TableColumn<String, String> innercol = new TableColumn<>(innercolumn);
            innercol.setCellValueFactory(value -> {
              JsonNode testNode = JsonSchemaUtil.stringToJsonNode(value.getValue());
              JsonNode innerNodeValue = JsonSchemaUtil.getSubNodeWithId(testNode,
                  column);
              return new SimpleStringProperty(
                  JsonSchemaUtil.getValueWithKey(innerNodeValue, innercolumn));
            });
            col.getColumns().add(innercol);
          }
        } else if (JsonSchemaUtil.isArray(innerNode)) {
          col.setCellValueFactory(value -> {
            return new SimpleStringProperty("[...]");
          });
        }

      }
      table.setVisible(false);
      topicNameTableMap.put(topicName, table);
      spTableContainer.getChildren().add(table);

      table.setOnMouseClicked((MouseEvent event) -> {
        if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
          viewControl.openDisplayWindow(topicName,
              table.getSelectionModel().getSelectedItem());
        }
      });

    }

    topicListView.getItems().addAll(topicNameList);

    topicListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldName, selectedTopic) -> {
          topicNameTableMap.get(selectedTopic).toFront();
          topicNameTableMap.get(selectedTopic).setVisible(true);
          btnOpenSenderWindow.setVisible(true);
          if (!activeListenerList.contains(selectedTopic)) {
            stopBtn.setDisable(true);
            startBtn.setDisable(false);
          } else {
            stopBtn.setDisable(false);
            startBtn.setDisable(true);
          }
        });

    topicListView.setCellFactory(param -> new ListCell<String>() { // NOSONAR
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
          setText(null);
          setStyle("-fx-control-inner-background: derive(-fx-base,80%);");
        } else {
          setText(item);
          if (activeListenerList.contains(item)) {
            setStyle(
                "-fx-control-inner-background: derive(palegreen, 50%); -fx-border-color: green;");
          } else {
            setStyle("-fx-control-inner-background: derive(-fx-base,80%);");
          }
        }
      }
    });

  }

  @FXML
  public void onActionBtnSenderWindow(ActionEvent event) {
    String selectedTopic = topicListView.getSelectionModel().getSelectedItem();
    viewControl.openSenderWindow(selectedTopic);
  }

  @FXML
  public void clearTable(ActionEvent event) {
    String selectedTopic = topicListView.getSelectionModel().getSelectedItem();
    topicNameTableMap.get(selectedTopic).getItems().clear();
  }

  @FXML
  public void stopListener(ActionEvent event) {
    String selectedTopic = topicListView.getSelectionModel().getSelectedItem();
    viewControl.stopListener(selectedTopic);
    activeListenerList.remove(selectedTopic);

    stopBtn.setDisable(true);
    startBtn.setDisable(false);

  }

  @FXML
  public void startListener(ActionEvent event) {
    String selectedTopic = topicListView.getSelectionModel().getSelectedItem();
    viewControl.startListener(selectedTopic);
    activeListenerList.add(selectedTopic);

    startBtn.setDisable(true);
    stopBtn.setDisable(false);

  }

  @FXML
  public void exportClicked(ActionEvent event) {
    DirectoryChooser dc = new DirectoryChooser();
    File selectedFolder = dc.showDialog(this.getScene().getWindow());

    JCodeModel codeModel = new JCodeModel();
    GenerationConfig config = new DefaultGenerationConfig() {
      @Override
      public boolean isGenerateBuilders() {
        return true;
      }
    };
    SchemaMapper mapper = new SchemaMapper(
        new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()),
        new SchemaGenerator());

    for (String schemaName : viewControl.getDataTypeInfoService().getTopicSchemaMap()
        .values()) {
      try {
        mapper.generate(codeModel, "ClassName", "class",
            new File("data/schemas/" + schemaName + ".json").toURI().toURL());
        codeModel.build(selectedFolder);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static interface MainViewControl {
    void openSenderWindow(String topicName);

    void openDisplayWindow(String topicName, String jsonData);

    ExchangeDataTypeInfoService getDataTypeInfoService();

    void stopListener(String topicName);

    void startListener(String topicName);
  }

}
