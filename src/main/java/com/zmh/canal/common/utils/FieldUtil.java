package com.zmh.canal.common.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zmh.canal.common.annotation.CanalTable;
import com.zmh.canal.common.handler.IEsHandler;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: FieldUtil
 * @author: zhou ming hao
 * @date: 2024年08月15日 2:03
 */
public class FieldUtil {
    /**
     * 缓存，key：CanalEntryHandler的实现类  value：CanalEntryHandler接口的泛型
     */
    private static Map<Class<? extends IEsHandler>, Class> cache = new ConcurrentHashMap<>();

    /**
     * 获取table名称
     */
    public static String getTableGenericProperties(IEsHandler entryHandler) {
        Class<?> tableClass = getTableClass(entryHandler);
        if (tableClass != null) {
            //CanalTable优先，没有才以TableName
            CanalTable canalTable = tableClass.getAnnotation(CanalTable.class);
            if (canalTable != null) {
                return canalTable.value();
            }

            TableName annotation = tableClass.getAnnotation(TableName.class);
            if (annotation != null) {
                return annotation.value();
            }
        }
        return null;
    }

    /**
     * 获取class类型
     */
    public static <T> Class<T> getTableClass(IEsHandler object) {
        Class<? extends IEsHandler> handlerClass = object.getClass();
        Class tableClass = cache.get(handlerClass);
        if (tableClass == null) {
            //获取所有实现接口
            Type[] interfaces = handlerClass.getGenericInterfaces();
            for (Type t : interfaces) {
                //获取原始类型
                Class c = (Class) ((ParameterizedType) t).getRawType();
                if (c.equals(IEsHandler.class)) {
                    tableClass = (Class<T>) ((ParameterizedType) t).getActualTypeArguments()[0];
                    cache.putIfAbsent(handlerClass, tableClass);
                    return tableClass;
                }
            }
        }
        return tableClass;
    }


    /**
     * 对象属性赋值
     *
     * @param object    对象
     * @param fieldName 属性名称
     * @param value     属性值(需要转为对应类型)
     * @throws IllegalAccessException 赋值异常
     */
    public static void setFieldValue(Object object, String fieldName, String value) throws IllegalAccessException {
        //fieldName 下划线转驼峰
        fieldName = fieldName.toLowerCase();
        fieldName = StrUtil.toCamelCase(fieldName);

        Field field = ReflectUtil.getField(object.getClass(), fieldName);

        //类型转换
        Object result = Convert.convert(field.getType(), value);

        //赋值
        ReflectUtil.setFieldValue(object, field, result);
    }
}
