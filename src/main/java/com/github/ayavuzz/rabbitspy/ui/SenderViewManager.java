package com.github.ayavuzz.rabbitspy.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.ayavuzz.rabbitspy.logic.RabbitMQMessageListener;
import com.github.ayavuzz.rabbitspy.service.ExchangeDataTypeInfoService;
import com.github.ayavuzz.rabbitspy.service.RabbitDataPublisher;
import com.github.ayavuzz.rabbitspy.ui.SenderView.SenderViewControl;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Component
public class SenderViewManager implements SenderViewControl {

  @Autowired
  private ExchangeDataTypeInfoService dataTypeInfoService;

  @Autowired
  private RabbitDataPublisher publisherService;

  RabbitMQMessageListener receiver = new RabbitMQMessageListener();

  public void displaySenderView(String topicName) {
    String schemaName = dataTypeInfoService.getTopicSchemaMap().get(topicName);

    Stage stage = new Stage();

    JsonNode jsonNode = dataTypeInfoService.getSchemaPropertiesMap().get(schemaName);
    JsonFormNode jsonFormNode = new JsonFormNode(jsonNode);
    SenderView formView = new SenderView(this, topicName, jsonFormNode) { // NOSONAR
      private boolean needsResize = false;

      /**
       * resizes stage according to its dynamically filled children
       */
      @Override
      protected void layoutChildren() {
        super.layoutChildren();
        if (!needsResize) {
          needsResize = true;
          Platform.runLater(() -> {
            if (needsResize) {
              stage.sizeToScene();
              needsResize = false;
            }
          });
        }
      }
    };
    Scene scene = new Scene(formView);
    stage.setScene(scene);
    stage.setTitle("Sender");
    stage.show();
  }

  public void displayFormView(String topicName, String jsonData) {
    String schemaName = dataTypeInfoService.getTopicSchemaMap().get(topicName);

    Stage stage = new Stage();
    JsonNode jsonNode = dataTypeInfoService.getSchemaPropertiesMap().get(schemaName);
    JsonFormNode jsonFormNode = new JsonFormNode(jsonNode);
    jsonFormNode.setData(jsonData);
    SenderView formView = new SenderView(this, topicName, jsonFormNode) { // NOSONAR
      private boolean needsResize = false;

      /**
       * resizes stage according to its dynamically filled children
       */
      @Override
      protected void layoutChildren() {
        super.layoutChildren();
        if (!needsResize) {
          needsResize = true;
          Platform.runLater(() -> {
            if (needsResize) {
              stage.sizeToScene();
              needsResize = false;
            }
          });
        }
      }
    };
    Scene scene = new Scene(formView);
    stage.setScene(scene);
    stage.setTitle("Display");
    stage.show();

  }

  @Override
  public void publishMessage(String topicName, String jsonData) {
    publisherService.publishData(topicName, jsonData);
  }

}
