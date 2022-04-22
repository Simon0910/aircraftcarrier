package com.lzp;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aircraftcarrier.framework.tookit.ResourceUtil;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
 *
 * @author ext.liuzhipeng5
 */
public class JsonPathTest {

    private static Map<String, String> keys = new ConcurrentHashMap<>(200);

    @Test
    public void jsonPathTest() {
        String jsonString = ResourceUtil.getClassPathResource("test.json");
        JSONArray jsonArray = JSONUtil.parseArray(jsonString);
        parserJsonString(jsonArray, "$");
        keys.keySet().forEach(System.out::println);
        System.out.println(keys.size());

        String payload = "{}";
        DocumentContext doc = JsonPath.parse(payload);
        for (String path : keys.keySet()) {
            doc.set(path, new HashMap<>());
        }
    }

    private static void parserJsonString(Object json, String parentPath) {
        if (json instanceof JSONArray) {
            JSONArray array = (JSONArray) json;
            Iterator<Object> iterator = array.stream().iterator();
            while (iterator.hasNext()) {
                parserJsonString(JSONUtil.parse(iterator.next()), parentPath);
            }
        } else if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                String newParentPath = parentPath + "." + key;
                addKey(newParentPath);
                parserJsonString(jsonObject.get(key), newParentPath);
            }
        }
    }

    private static void addKey(String key) {
        if (key == null) {
            return;
        }
        keys.putIfAbsent(key, key);
    }

}
