package com.github.ayavuzz.rabbitspy.ui;

import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.ayavuzz.rabbitspy.util.JsonSchemaUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class JsonFormNode extends GridPane { // NOSONAR

  public JsonFormNode(JsonNode jsonNode) {
    ColumnConstraints col1 = new ColumnConstraints();
    ColumnConstraints col2 = new ColumnConstraints();
    this.getColumnConstraints().addAll(col1, col2);
    this.setVgap(15);
    this.setHgap(20);

    List<String> fieldNames = JsonSchemaUtil.getFieldsFromJsonNode(jsonNode);
    for (String fieldName : fieldNames) {
      JsonNode innerNode = jsonNode.get(fieldName);
      Node node = getNodeForFieldInSchema(innerNode);
      addFormItem(fieldName, node);
    }
  }

  public void setData(String publishedJsonData) {
    Platform.runLater(() -> {
      JSONParser parser = new JSONParser();
      JSONObject json = null;

      try {
        json = (JSONObject) parser.parse(publishedJsonData);
      } catch (ParseException e) {
        e.printStackTrace();
      }

      String valueForNode = "";

      for (Node currentNode : this.getChildren()) {
        if (currentNode instanceof Label) {
          Label label = (Label) currentNode;
          valueForNode = json.get(label.getText()).toString();
        } else if (currentNode instanceof TextField) {
          TextField textField = (TextField) currentNode;
          textField.setText(valueForNode);
        } else if (currentNode instanceof ComboBox<?>) {
          ComboBox<String> comboBox = (ComboBox<String>) currentNode;
          comboBox.setValue(valueForNode);
        } else if (currentNode instanceof CheckBox) {
          CheckBox checkBox = (CheckBox) currentNode;
          if (valueForNode.equals("true")) {
            checkBox.setSelected(true);
          } else if (valueForNode.equals("false")) {
            checkBox.setSelected(false);
          }
        } else if (currentNode instanceof JsonFormArrayNode) {
          JsonFormArrayNode jsonFormArrayNode = (JsonFormArrayNode) currentNode;
          jsonFormArrayNode.appendList(valueForNode);
        } else if (currentNode instanceof JsonFormTwoDimArrayNode) {
          JsonFormTwoDimArrayNode jsonFormArrayNode = (JsonFormTwoDimArrayNode) currentNode;
          jsonFormArrayNode.appendList(valueForNode);
        } else if (currentNode instanceof JsonFormNode) {
          JsonFormNode innerFormNode = (JsonFormNode) currentNode;
          innerFormNode.setData(valueForNode);
        } else if (currentNode instanceof TextArea) {
          TextArea textArea = (TextArea) currentNode;
          textArea.setText(valueForNode.replaceAll("],", "],\n"));
        } else if (currentNode instanceof JsonFormObjectArrayNode) {
          JsonFormObjectArrayNode tableView = (JsonFormObjectArrayNode) currentNode;
          tableView.appendList(valueForNode);
        }
      }

    });
  }

  private Node getNodeForFieldInSchema(JsonNode innerNode) {
    String jsonNodeType = innerNode.get("type").toString().replace("\"", "");
    Node node = null;
    switch (jsonNodeType) {
      case "number":
        TextField tfNodeNum = new TextField();
        tfNodeNum.setUserData(EnumNodeType.NUMBER);
        tfNodeNum.setPromptText(EnumNodeType.NUMBER.toString());
        node = tfNodeNum;
        break;
      case "integer":
        TextField tfNodeInt = new TextField();
        tfNodeInt.setUserData(EnumNodeType.INTEGER);
        tfNodeInt.setPromptText(EnumNodeType.INTEGER.toString());
        tfNodeInt.setMaxWidth(200.0);
        node = tfNodeInt;
        break;
      case "object":
        node = new JsonFormNode(innerNode.get("properties"));
        node.setUserData(EnumNodeType.OBJECT);
        break;
      case "boolean":
        node = new CheckBox();
        node.setUserData(EnumNodeType.BOOLEAN);
        break;
      case "array":
        String arrayType = innerNode.get("items").get("type").toString().replace("\"", "");
        if (arrayType.equals("integer") || arrayType.equals("string")
            || arrayType.equals("number")) {
          node = new JsonFormArrayNode(EnumNodeType.valueOf(arrayType.toUpperCase()));
        } else if (arrayType.equals("array")) {
          node = new JsonFormTwoDimArrayNode(
              EnumNodeType
                  .valueOf(innerNode.get("items").get("items").get("type").toString().replace("\"", "").toUpperCase()));
        }

        // TODO array of object
        else if (arrayType.equals("object")) {
          // TableView<String> tableView = new TableView<>();
          // for (String innercolumn : innercolumns) {
          // TableColumn<String, String> innercol = new TableColumn<>(innercolumn);
          // innercol.setCellValueFactory(value -> {
          // JsonNode testNode =
          // JsonSchemaUtil.stringToJsonNode(value.getValue());
          // JsonNode innerNodeValue =
          // JsonSchemaUtil.getSubNodeWithId(testNode,
          // innercolumn);
          // return new SimpleStringProperty(
          // JsonSchemaUtil.getValueWithKey(innerNodeValue, innercolumn));
          // });
          // tableView.getColumns().add(innercol);
          // }
          JsonNode propertiesList = innerNode.get("items").get("properties");
          node = new JsonFormObjectArrayNode(propertiesList);

        }

        else {
          node = new Label("Array");
        }

        node.setUserData(EnumNodeType.ARRAY);

        break;
      case "string":
        if (!(innerNode.get("enum") == null)) {
          ComboBox<String> comboBox = new ComboBox<>();
          for (JsonNode enumNode : innerNode.get("enum")) {
            comboBox.getItems().add(enumNode.toString().replaceAll("\"", ""));
          }
          node = comboBox;
          node.setUserData(EnumNodeType.ENUM);
        } else {
          node = new TextField();
          node.setUserData(EnumNodeType.STRING);
        }
        break;
      default:
        node = new Label("Tanımlanmayan alan");
    }
    return node;
  }

  private void addFormItem(String fieldName, Node node) {
    Platform.runLater(() -> {
      RowConstraints row = new RowConstraints();
      this.getRowConstraints().add(row);
      this.add(new Label(fieldName), 0, this.getRowConstraints().size() - 1);
      this.add(node, 1, this.getRowConstraints().size() - 1);
    });
  }
}
