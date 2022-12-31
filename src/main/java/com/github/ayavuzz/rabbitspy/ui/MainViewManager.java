
package com.github.ayavuzz.rabbitspy.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import com.github.ayavuzz.rabbitspy.SpyFXApplication.StageReadyEvent;
import com.github.ayavuzz.rabbitspy.service.ExchangeDataTypeInfoService;
import com.github.ayavuzz.rabbitspy.service.RabbitDataListener;
import com.github.ayavuzz.rabbitspy.service.RabbitExchangeManagementService;
import com.github.ayavuzz.rabbitspy.ui.MainView.MainViewControl;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Component
public class MainViewManager implements ApplicationListener<StageReadyEvent>, RabbitDataListener, MainViewControl {

  @Autowired
  private ExchangeDataTypeInfoService dataTypeInfoService;

  @Autowired
  private RabbitExchangeManagementService rabbitExchangeService;

  @Autowired
  private SenderViewManager senderViewManager;

  private MainView mainView;


  @Override
  public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
    Stage stage = stageReadyEvent.getStage();
    mainView = new MainView(this);
    Scene scene = new Scene(mainView);
    stage.setScene(scene);
    stage.setTitle("Rabbit-SPY");
    stage.show();
  }

  @Override
  public void handleData(String topicId, String value) {
    if (mainView != null) {
      mainView.handleValue(topicId, value);
    }
  }

  @Override
  public void openSenderWindow(String topicName) {
    senderViewManager.displaySenderView(topicName);
  }

  @Override
  public void openDisplayWindow(String topicName, String jsonData) {
    senderViewManager.displayFormView(topicName, jsonData);
  }

  @Override
  public ExchangeDataTypeInfoService getDataTypeInfoService() {
    return dataTypeInfoService;
  }

  @Override
  public void stopListener(String topicName) {
    rabbitExchangeService.removeListenerFromExchange(topicName);
  }

  @Override
  public void startListener(String topicName) {
    rabbitExchangeService.addListenerToExchange(topicName);
  }

}
