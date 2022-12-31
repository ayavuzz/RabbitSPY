
package com.github.ayavuzz.rabbitspy.logic;

import java.util.List;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.ayavuzz.rabbitspy.service.RabbitDataListener;

@Component
public class RabbitMQMessageListener {

  public static final String RECEIVER_ID = "rabbitspy-data-listener";

  @Autowired
  List<RabbitDataListener> listeners;


  @RabbitListener(id = RECEIVER_ID)
  public void messageReceived(Message message) {
    String excName = message.getMessageProperties().getReceivedExchange();

    String msgBody = fixMessageBody(message.getBody());
    listeners.forEach(listener -> listener.handleData(excName, msgBody));
  }

  private String fixMessageBody(byte[] str) {
    // Json data is received like following : "{\"command\":\"START\",\"index\":7}"
    String msgBody = new String(str).replaceAll("\\\\", "");
    if (msgBody.charAt(0) == '\"') {
      return msgBody.substring(1, msgBody.length() - 1);
    }
    return msgBody;

  }

}
