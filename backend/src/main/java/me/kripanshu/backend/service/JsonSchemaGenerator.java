package me.kripanshu.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class JsonSchemaGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();

    public String generate(String jsonBody) {
        try {
            JsonNode root = mapper.readTree(jsonBody);
            ObjectNode schema = mapper.createObjectNode();
            schema.put("$schema", "http://json-schema.org/draft-07/schema#");
            inferSchema(root, schema);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
        } catch (Exception e) {
            ObjectNode fallback = mapper.createObjectNode();
            fallback.put("type", "string");
            return fallback.toString();
        }
    }

    private void inferSchema(JsonNode node, ObjectNode schema) {
        if (node.isObject()) {
            schema.put("type", "object");
            ObjectNode properties = schema.putObject("properties");
            boolean hasRequired = false;
            schema.putArray("required");
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                ObjectNode propSchema = properties.putObject(field.getKey());
                inferSchema(field.getValue(), propSchema);
                if (!field.getValue().isNull()) {
                    hasRequired = true;
                }
            }
            if (!hasRequired) {
                schema.remove("required");
            }
        } else if (node.isArray()) {
            schema.put("type", "array");
            ArrayNode arr = (ArrayNode) node;
            if (!arr.isEmpty()) {
                ObjectNode itemsSchema = schema.putObject("items");
                inferSchema(arr.get(0), itemsSchema);
            }
        } else if (node.isTextual()) {
            schema.put("type", "string");
        } else if (node.isBoolean()) {
            schema.put("type", "boolean");
        } else if (node.isInt() || node.isLong()) {
            schema.put("type", "integer");
        } else if (node.isFloat() || node.isDouble()) {
            schema.put("type", "number");
        } else if (node.isNull()) {
            schema.put("type", "null");
        }
    }
}
