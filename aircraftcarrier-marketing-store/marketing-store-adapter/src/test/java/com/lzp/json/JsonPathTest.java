package com.lzp.json;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aircraftcarrier.framework.tookit.ResourceUtil;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 把不同字段json
 * <pre>{@code
 * [{
 *    "a": "",
 *    "b": "",
 *    "list": [{
 *       "id": "",
 *       "name": ""
 *    },{
 *        "id": "",
 *        "type": ""
 *    }]
 * },{
 *    "a": "",
 *    "c": "",
 *    "list": [{
 *       "id": "",
 *       "value": ""
 *    },{
 *        "id": "",
 *        "value": ""
 *    }]
 * }]
 * }</pre>
 * 转换成统一对象格式 ==>
 * <pre>{@code
 * {
 *    "a": "",
 *    "b": "",
 *    "c": ""
 *    "list": [{
 *       "id": "",
 *       "name": "",
 *       "value": "",
 *       "type": ""
 *    }]
 * }
 * }</pre>
 * <p>
 * https://stackoverflow.com/questions/51971642/how-to-add-new-node-to-json-using-jsonpath
 * <pre>{@code
 * setJsonPointerValue(rootNode, JsonPointer.compile("/root/array/0/name"), new TextNode("John"));
 * setJsonPointerValue(rootNode, JsonPointer.compile("/root/array/0/age"), new IntNode(17));
 * setJsonPointerValue(rootNode, JsonPointer.compile("/root/array/4"), new IntNode(12));
 * setJsonPointerValue(rootNode, JsonPointer.compile("/root/object/num"), new IntNode(81));
 * setJsonPointerValue(rootNode, JsonPointer.compile("/root/object/str"), new TextNode("text"));
 * setJsonPointerValue(rootNode, JsonPointer.compile("/desc"), new TextNode("description"));
 * }</pre>
 *
 * @author ext.liuzhipeng5
 */
public class JsonPathTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static Map<String, String> keys = new ConcurrentHashMap<>(200);

    private static List<NewNode> nodeList = new ArrayList<>(200);

    private static DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static int id = 1;
    private static int ruleComponentNo = 1;
    private static int ruleComponentId = 1;


    @Test
    public void splitJson() {
        String jsonString = ResourceUtil.getClassPathResource("res.json");
        JSONArray jsonArray = JSONUtil.parseArray(jsonString);
        splitJson(jsonArray);
        System.out.println(jsonArray);
    }

    @Test
    public void jsonPathTest() {
        String jsonString = ResourceUtil.getClassPathResource("res.json");
        JSONArray jsonArray = JSONUtil.parseArray(jsonString);
        parserJsonStringPath(jsonArray, "$");
        System.out.println(jsonArray);
    }

    @Test
    public void test() throws JsonProcessingException {
        String jsonString = ResourceUtil.getClassPathResource("test.json");
        JSONArray jsonArray = JSONUtil.parseArray(jsonString);
        parserJsonString(jsonArray, "");

        List<NewNode> nodes = nodeList.stream().sorted(Comparator.comparing(NewNode::getNewPath)).filter(e -> !e.getNewPath().endsWith("0")).collect(Collectors.toList());
        nodes.forEach(e -> System.out.println(e.getNewPath()));
        System.out.println(nodes.size());

        ObjectNode rootNode = mapper.createObjectNode();
        for (NewNode node : nodes) {
            setJsonPointerValue(rootNode, JsonPointer.compile(node.getNewPath()), node.getValueNode());
        }
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));
    }

    private static void splitJson(Object json) {
        if (json instanceof JSONArray) {
            JSONArray array = (JSONArray) json;
            Iterator<Object> iterator = array.stream().iterator();
            while (iterator.hasNext()) {
                splitJson(JSONUtil.parse(iterator.next()));
            }
        } else if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                Object obj = jsonObject.get(key);
                if (key.equals("components")) {
                    Object id = jsonObject.get("id");
                    String rule_component_type = "";
                    if ("8".equals(id)) {
                        rule_component_type = "COUPON_BASE";
                    } else if ("19".equals(id)) {
                        rule_component_type = "COUPON_RECEIVE";
                    } else if ("58".equals(id)) {
                        rule_component_type = "COUPON_USE";
                    } else if ("62".equals(id)) {
                        rule_component_type = "RESOURCE_PACKAGE_BASE";
                    } else if ("72".equals(id)) {
                        rule_component_type = "RESOURCE_PACKAGE_RECEIVE";
                    } else if ("77".equals(id)) {
                        rule_component_type = "RESOURCE_PACKAGE_USE";
                    } else if ("102".equals(id)) {
                        rule_component_type = "LABEL_SHOW";
                    }
                    JSONArray array = (JSONArray) obj;
                    Iterator<Object> iterator = array.stream().iterator();
                    while (iterator.hasNext()) {
                        Object next = iterator.next();
                        JSONObject jsonObjectNext = (JSONObject) next;
                        String ruleComponentNo = (String) jsonObjectNext.get("ruleComponentNo");
                        String ruleComponentId = (String) jsonObjectNext.get("ruleComponentId");
                        String start = LocalDateTime.now().format(df);
                        String end = LocalDateTime.now().format(df);
                        String jsonSchema = JSON.toJSONString(next);
                        System.out.println("INSERT INTO mkt_rule_component  ( rule_component_no, rule_component_id, rule_component_type, `schema`, expression, creator, modifier, created_time, modified_time )  VALUES  ( \"" + ruleComponentNo + "\", \"" + ruleComponentId + "\", \"" + rule_component_type + "\", \'" + jsonSchema + "\', \"expression\", \"admin\", \"admin\", \"" + start + "\", \"" + end + "\" );");
                    }
                }
                splitJson(obj);
            }
        }
    }

    private static void parserJsonStringPath(Object json, String parentPath) {
        if (json instanceof JSONArray) {
            JSONArray array = (JSONArray) json;
            Iterator<Object> iterator = array.stream().iterator();
            while (iterator.hasNext()) {
                parserJsonStringPath(JSONUtil.parse(iterator.next()), parentPath);
            }
        } else if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                String newParentPath = parentPath + "." + key;
                addKey(newParentPath);
                if (key.equals("id")) {
                    jsonObject.set(key, String.valueOf(id++));
                } else if (key.equals("ruleComponentNo")) {
                    jsonObject.set(key, String.valueOf(ruleComponentNo++));
                } else if (key.equals("ruleComponentId")) {
                    jsonObject.set(key, String.valueOf(ruleComponentId++));
                }
                parserJsonStringPath(jsonObject.get(key), newParentPath);
            }
        }
    }

    private static void addKey(String key) {
        if (key == null) {
            return;
        }
        keys.putIfAbsent(key, key);
    }

    private static void parserJsonString(Object json, String parentPath) {
        if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            Iterator<Object> iterator = jsonArray.stream().iterator();
            while (iterator.hasNext()) {
                parserJsonString(JSONUtil.parse(iterator.next()), parentPath);
            }
        } else if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                Object valueObj = jsonObject.get(key);
                String newParentPath = newParentPath(parentPath, key, valueObj);
                parserJsonString(valueObj, newParentPath);
            }
        }
    }


    private static String getType(Object obj) {
        return obj.getClass().getTypeName();
    }

    private static String newParentPath(String parentPath, String key, Object valueObj) {

        String newPath = "";
        ValueNode valueNode;

        if (valueObj == null) {
            newPath = parentPath + "/" + key;
            valueNode = NullNode.getInstance();
        } else {
            String type = getType(valueObj);
            if (String.class.getTypeName().equals(type)) {
                newPath = parentPath + "/" + key;
//                valueNode = new TextNode("");
                valueNode = NullNode.getInstance();
            } else if (Integer.class.getTypeName().equals(type)) {
                newPath = parentPath + "/" + key;
                valueNode = new IntNode(0);
            } else if (Boolean.class.getTypeName().equals(type)) {
                newPath = parentPath + "/" + key;
                valueNode = BooleanNode.getFalse();
            } else if (Double.class.getTypeName().equals(type)) {
                newPath = parentPath + "/" + key;
                valueNode = new DoubleNode(0);
            } else if (Float.class.getTypeName().equals(type)) {
                newPath = parentPath + "/" + key;
                valueNode = new FloatNode(0);
            } else if (Long.class.getTypeName().equals(type)) {
                newPath = parentPath + "/" + key;
                valueNode = new LongNode(0);
            } else if (JSONArray.class.getTypeName().equals(type)) {
                newPath = parentPath + "/" + key + "/0";
                valueNode = NullNode.getInstance();
            } else if (JSONObject.class.getTypeName().equals(type)) {
                newPath = parentPath + "/" + key;
                valueNode = NullNode.getInstance();
            } else {
                throw new RuntimeException("wow");
            }
        }

        String s = keys.get(newPath);
        if (s == null) {
            keys.put(newPath, newPath);
            nodeList.add(new NewNode(newPath, valueNode));
        }

        return newPath;
    }


    private void setJsonPointerValue(ObjectNode node, JsonPointer pointer, JsonNode value) {
        JsonPointer parentPointer = pointer.head();
        JsonNode parentNode = node.at(parentPointer);
        String fieldName = pointer.last().toString().substring(1);

        if (parentNode.isMissingNode() || parentNode.isNull()) {
            parentNode = StringUtils.isNumeric(fieldName) ? mapper.createArrayNode() : mapper.createObjectNode();
            setJsonPointerValue(node, parentPointer, parentNode); // recursively reconstruct hierarchy
        }

        if (parentNode.isArray()) {
            assert parentNode instanceof ArrayNode;
            ArrayNode arrayNode = (ArrayNode) parentNode;
            int index = Integer.parseInt(fieldName);
            // expand array in case index is greater than array size (like JavaScript does)
            for (int i = arrayNode.size(); i <= index; i++) {
                arrayNode.addNull();
            }
            arrayNode.set(index, value);
        } else if (parentNode.isObject()) {
            ((ObjectNode) parentNode).set(fieldName, value);
        } else {
            throw new IllegalArgumentException("`" + fieldName + "` can't be set for parent node `"
                    + parentPointer + "` because parent is not a container but " + parentNode.getNodeType().name());
        }
    }
}
