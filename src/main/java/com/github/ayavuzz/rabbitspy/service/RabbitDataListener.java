package com.github.ayavuzz.rabbitspy.service;

public interface RabbitDataListener {

  void handleData(String topicId, String value);
}
