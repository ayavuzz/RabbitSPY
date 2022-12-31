package com.github.ayavuzz.rabbitspy.service;

public interface RabbitExchangeManagementService {

  void declareExchange(String exchangeName);

  void addListenerToExchange(String exchangeName);

  void removeListenerFromExchange(String exchangeName);
}
