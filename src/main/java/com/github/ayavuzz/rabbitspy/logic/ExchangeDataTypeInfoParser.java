package com.github.ayavuzz.rabbitspy.logic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.ayavuzz.rabbitspy.service.ExchangeDataTypeInfoService;
import com.github.ayavuzz.rabbitspy.service.RabbitExchangeManagementService;
import com.github.ayavuzz.rabbitspy.util.JsonSchemaUtil;
import lombok.Getter;

@Component
public class ExchangeDataTypeInfoParser implements SmartLifecycle, ExchangeDataTypeInfoService {

  private static final String MAPPING_FILE_DIR = "data/exchangeDataTypeMappings.txt";

  @Autowired
  private RabbitExchangeManagementService exchangeManagementService;

  /**
   * Mapping of topic name to JSON schema file name
   */
  @Getter
  private Map<String, String> topicSchemaMap = new HashMap<>();

  /**
   * Mapping of JSON schema file name to it's properties JsonNode
   */
  @Getter
  private Map<String, JsonNode> schemaPropertiesMap = new HashMap<>();


  @Override
  public void start() {
    try (BufferedReader br = new BufferedReader(new FileReader(MAPPING_FILE_DIR))) {
      String line = "";
      while ((line = br.readLine()) != null) {
        String exchangeName = line.split(":")[0];
        String schemaName = line.split(":")[1];

        topicSchemaMap.put(exchangeName, schemaName);
        schemaPropertiesMap.put(schemaName, JsonSchemaUtil.getPropertiesJsonNode(schemaName));

        exchangeManagementService.declareExchange(exchangeName);
        exchangeManagementService.addListenerToExchange(exchangeName);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void stop() {}

  @Override
  public boolean isRunning() {
    return false;
  }

}
