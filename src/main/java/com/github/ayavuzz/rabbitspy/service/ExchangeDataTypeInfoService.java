package com.github.ayavuzz.rabbitspy.service;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

public interface ExchangeDataTypeInfoService {

  Map<String, String> getTopicSchemaMap();

  Map<String, JsonNode> getSchemaPropertiesMap();
}
