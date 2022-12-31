package com.github.ayavuzz.rabbitspy.service;


public interface RabbitDataPublisher {

  void publishData(String exchangeName, String jsonData);
}
