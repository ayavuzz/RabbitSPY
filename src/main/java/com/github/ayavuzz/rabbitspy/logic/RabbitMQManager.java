
package com.github.ayavuzz.rabbitspy.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.github.ayavuzz.rabbitspy.service.RabbitDataPublisher;
import com.github.ayavuzz.rabbitspy.service.RabbitExchangeManagementService;

@Component
public class RabbitMQManager implements RabbitExchangeManagementService, RabbitDataPublisher {

  @Autowired
  private RabbitAdmin rabbitAdmin;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

  private Map<String, Binding> topicBindingMap = new HashMap<>();


  @Override
  public void publishData(String exchangeName, String jsonData) {
    rabbitTemplate.convertAndSend(exchangeName, "", jsonData);
  }

  @Override
  public void addListenerToExchange(String exchangeName) {
    String queueName = exchangeName + "_SPY_Q";
    Queue queue = new Queue(queueName, true, false, false);
    Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, "", null);

    topicBindingMap.put(exchangeName, binding);
    rabbitAdmin.declareQueue(queue);
    rabbitAdmin.declareBinding(binding);

    // check if queue is already exist
    if (!Arrays.asList(this.getMessageListenerContainer(RabbitMQMessageListener.RECEIVER_ID).getQueueNames())
        .contains(queueName)) {
      this.getMessageListenerContainer(RabbitMQMessageListener.RECEIVER_ID).addQueueNames(queueName);
    }
  }

  @Override
  public void removeListenerFromExchange(String exchangeName) {
    rabbitAdmin.removeBinding(topicBindingMap.get(exchangeName));
  }

  @Override
  public void declareExchange(String exchangeName) {
    FanoutExchange exchange = new FanoutExchange(exchangeName);
    rabbitAdmin.declareExchange(exchange);
  }

  private AbstractMessageListenerContainer getMessageListenerContainer(String receiverID) {
    return ((AbstractMessageListenerContainer) this.rabbitListenerEndpointRegistry
        .getListenerContainer(receiverID));
  }

}
