package com.github.ayavuzz.rabbitspy.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSchemaUtil {

	public static boolean isObject(JsonNode jsonNode) {
		return jsonNode.get("type").toString().equals("\"object\"");
	}

	public static boolean isArray(JsonNode jsonNode) {
		return jsonNode.get("type").toString().equals("\"array\"");
	}

	public static JsonNode getPropertiesJsonNode(String schemaName) {
		String schemaDir = "data/schemas/" + schemaName + ".json";
		JsonNode rootNode = null;
		try {
			rootNode = stringToJsonNode(readFileAsString(schemaDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rootNode.get("properties");
	}

	public static JsonNode getPropertiesJsonNode(JsonNode jsonNode) {
		return jsonNode.get("properties");
	}

	public static JsonNode stringToJsonNode(String json) {
		JsonFactory factory = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(factory);
		try {
			return mapper.readTree(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<String> getFieldsFromJsonNode(JsonNode jsonNode) {
		List<String> propertiesList = new ArrayList<>();
		Iterator<Map.Entry<String, JsonNode>> fieldsIterator = jsonNode.fields();
		while (fieldsIterator.hasNext()) {
			Map.Entry<String, JsonNode> field = fieldsIterator.next();
			propertiesList.add(field.getKey());
		}
		return propertiesList;
	}

	public static String getValueWithKey(JsonNode jsonNode, String key) {
		String value = "-";
		Iterator<Map.Entry<String, JsonNode>> fieldsIterator = jsonNode.fields();
		while (fieldsIterator.hasNext()) {
			Map.Entry<String, JsonNode> field = fieldsIterator.next();
			if (field.getKey().equals(key)) {
				value = field.getValue().asText();
			}
		}
		return value;
	}

	public static JsonNode getSubNodeWithId(JsonNode jsonNode, String key) {
		return jsonNode.get(key);
	}

	private static String readFileAsString(String file) throws IOException {
		return new String(Files.readAllBytes(Paths.get(file)));
	}

	private JsonSchemaUtil() {
	}

}
