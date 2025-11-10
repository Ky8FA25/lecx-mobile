package com.example.lecx_mobile.utils;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class PredicateUtils {
    public static <T> Predicate<T> containsPredicate(String fieldName, String searchValue) {
        // Chuyển giá trị tìm kiếm thành chữ thường để không phân biệt hoa thường
        final String lowerSearchValue = searchValue.toLowerCase();

        return entity -> {
            try {
                // Dùng Reflection để truy cập trường theo tên
                Field field = entity.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object fieldValue = field.get(entity);

                if (fieldValue == null) {
                    return false;
                }

                // Chuyển giá trị của trường thành String và kiểm tra 'contains'
                String fieldString = fieldValue.toString().toLowerCase();
                return fieldString.contains(lowerSearchValue);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Nếu trường không tồn tại hoặc không thể truy cập, coi như không thỏa mãn
                return false;
            }
        };
    }
}
