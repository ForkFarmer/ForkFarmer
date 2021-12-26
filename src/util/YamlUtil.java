package util;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class YamlUtil {

    Map<String, Object> properties;
    Yaml yaml;
    File yamlFile;

    public YamlUtil() {
    }

    public YamlUtil(String filePath) {
        this(new File(filePath));
    }

    public YamlUtil(File yamlFile) {
        InputStream inputStream = null;
        try {
            this.yamlFile = yamlFile;
            inputStream = new FileInputStream(yamlFile);
            yaml = new Yaml();
            properties = yaml.loadAs(inputStream, Map.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initWithString(String content) {
        yaml = new Yaml();
        properties = yaml.loadAs(content, Map.class);
    }

    /**
     * get value from yaml
     * <p>
     * e.g.
     * 1.get Object value: full_node.rpc_port
     * 2.get value from Array by index: harvester.plot_directories[0]
     *
     * @param keyExpression - the value expression
     * @param defaultValue
     * @param <T>
     * @return
     */
    public <T> T getValueByKey(String keyExpression, T defaultValue) {
        String separator = ".";
        String[] separatorKeys = null;
        if (keyExpression.contains(separator)) {
            separatorKeys = keyExpression.split("\\.");
        } else {
            Object res = properties.get(keyExpression);
            return res == null ? defaultValue : (T) res;
        }
        Object tempObject = properties;
        for (int i = 0; i < separatorKeys.length; i++) {
            String innerKey = separatorKeys[i];
            Integer index = null;
            if (innerKey.contains("[")) {
                index = Integer.valueOf(getSubstringBetweenFF(innerKey, "[", "]"));
                innerKey = innerKey.substring(0, innerKey.indexOf("["));
            }

            Map<String, Object> mapTempObj = (Map) tempObject;
            Object object = mapTempObj.get(innerKey);
            if (object == null) {
                return defaultValue;
            }

            Object targetObj = object;
            if (index != null) {
                targetObj = ((ArrayList) object).get(index);
            }
            tempObject = targetObj;
            if (i == separatorKeys.length - 1) {
                return (T) targetObj;
            }
        }
        return null;
    }


    private int getSubstringBetweenFF(String innerKey, String s1, String s2) {
        int start = innerKey.indexOf(s1);
        int end = innerKey.indexOf(s2);
        return Integer.parseInt(innerKey.substring(start + 1, end));
    }
}
