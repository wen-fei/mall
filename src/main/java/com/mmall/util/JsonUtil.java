package com.mmall.util;


import com.google.common.collect.Lists;
import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {

        // 序列化
        // 对象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);
        // 取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 忽略空Bean转json的情况
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        // 设置时间的格式，统一为yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));


        // 反序列化
        // 忽略在json字符串中存在，但是在java对象中不存在对应属性的情况，防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }


    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Parse object to String  error", e);
            return null;
        }
    }

    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to String error", e);
            return null;
        }
    }

    public static <T> T string2Obj(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }

        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }


    /**
     * 复杂对象的反序列化
     * @param str
     * @param typeReference
     * @param <T>
     * @return
     */
    // 注意别引错包，选jackson的
    public static <T> T string2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(str) || typeReference == null) {
            return null;
        }

        try {
            return (T)(typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    /**
     * 复杂对象的反序列化
     * @param str
     * @param collectionClass  集合的class
     * @param elementClasses   集合里面元素的class，也可以是多个参数，此时传数据就可以
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?>... elementClasses) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }


    public static void main(String[] args) {
        User u1 = new User();
        u1.setId(1);
        u1.setEmail("tengyun.cs@gmail.com");

        User u2 = new User();
        u2.setId(2);
        u2.setEmail("tengyun.cs@gmail.com");

        String user1Json = JsonUtil.obj2String(u1);

        String user1JsonPretty = JsonUtil.obj2StringPretty(u1);

        log.info("user1Json: {} ", user1Json);

        log.info("user1JsonPretty: {}", user1JsonPretty);

        User user = JsonUtil.string2Obj(user1Json, User.class);

        List<User> userList = Lists.newArrayList();
        userList.add(u1);
        userList.add(u2);
        String userListStr = JsonUtil.obj2StringPretty(userList);
        log.info("======================");
        log.info(userListStr);


        // 直接这样写是错误的，传入List.class,内部会实例化为LinkedHashMap，反序列化失败
        List<User> userListObj1 = JsonUtil.string2Obj(userListStr, new TypeReference<List<User>>() {});

        System.out.println("end");
    }


}
