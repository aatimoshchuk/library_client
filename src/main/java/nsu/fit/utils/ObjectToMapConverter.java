package nsu.fit.utils;

import lombok.RequiredArgsConstructor;
import nsu.fit.annotations.DisplayName;
import nsu.fit.annotations.FieldOrder;
import nsu.fit.annotations.HiddenField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ObjectToMapConverter {
    private static final Logger logger = LoggerFactory.getLogger(ObjectToMapConverter.class);
    public Map<String, Object> convert(Object obj) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Field> allFields = new ArrayList<>(Arrays.asList(obj.getClass().getDeclaredFields()));

        allFields.sort(Comparator.comparingInt(f -> {
            FieldOrder order = f.getAnnotation(FieldOrder.class);
            return order != null ? order.value() : Integer.MAX_VALUE;
        }));

        for (Field field : allFields) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(HiddenField.class)) {
                    continue;
                }

                Object value = field.get(obj);
                DisplayName displayName = field.getAnnotation(DisplayName.class);
                String key = displayName != null ? displayName.value() : field.getName();
                result.put(key, value);
            } catch (IllegalAccessException e) {
                logger.error("Ошибка доступа к полю {}: {}", field.getName(), e.getMessage());
            }
        }

        return result;
    }
}
