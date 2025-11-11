package com.example.lecx_mobile.utils;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.function.Predicate;

public class PredicateUtils {
    public static <T> Predicate<T> containsPredicate(String fieldName, String searchValue) {
        // Chuyển giá trị tìm kiếm thành chữ thường để không phân biệt hoa thường
        // Sử dụng Locale.ROOT để đảm bảo nhất quán và xử lý tốt với tiếng Việt
        final String lowerSearchValue = normalizeString(searchValue);

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
                String fieldString = normalizeString(fieldValue.toString());
                return fieldString.contains(lowerSearchValue);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Nếu trường không tồn tại hoặc không thể truy cập, coi như không thỏa mãn
                return false;
            }
        };
    }

    /**
     * Chuẩn hóa chuỗi để so sánh không phân biệt hoa thường và không dấu
     * Hỗ trợ tìm kiếm tiếng Việt không dấu (ví dụ: "tieng" tìm ra "tiếng")
     * 
     * @param str Chuỗi cần chuẩn hóa
     * @return Chuỗi đã được chuẩn hóa (chữ thường, không dấu)
     */
    public static String normalizeString(String str) {
        if (str == null) {
            return "";
        }
        // Chuyển thành chữ thường và trim
        String normalized = str.toLowerCase(Locale.ROOT).trim();
        // Bỏ dấu tiếng Việt
        normalized = removeDiacritics(normalized);
        return normalized;
    }

    /**
     * Chuyển đổi tiếng Việt có dấu thành không dấu
     * Ví dụ: "tiếng" -> "tieng", "Nguyễn" -> "Nguyen"
     */
    private static String removeDiacritics(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Bảng chuyển đổi ký tự có dấu sang không dấu
        text = text.replace("à", "a").replace("á", "a").replace("ạ", "a")
                   .replace("ả", "a").replace("ã", "a").replace("â", "a")
                   .replace("ầ", "a").replace("ấ", "a").replace("ậ", "a")
                   .replace("ẩ", "a").replace("ẫ", "a").replace("ă", "a")
                   .replace("ằ", "a").replace("ắ", "a").replace("ặ", "a")
                   .replace("ẳ", "a").replace("ẵ", "a");
        
        text = text.replace("è", "e").replace("é", "e").replace("ẹ", "e")
                   .replace("ẻ", "e").replace("ẽ", "e").replace("ê", "e")
                   .replace("ề", "e").replace("ế", "e").replace("ệ", "e")
                   .replace("ể", "e").replace("ễ", "e");
        
        text = text.replace("ì", "i").replace("í", "i").replace("ị", "i")
                   .replace("ỉ", "i").replace("ĩ", "i");
        
        text = text.replace("ò", "o").replace("ó", "o").replace("ọ", "o")
                   .replace("ỏ", "o").replace("õ", "o").replace("ô", "o")
                   .replace("ồ", "o").replace("ố", "o").replace("ộ", "o")
                   .replace("ổ", "o").replace("ỗ", "o").replace("ơ", "o")
                   .replace("ờ", "o").replace("ớ", "o").replace("ợ", "o")
                   .replace("ở", "o").replace("ỡ", "o");
        
        text = text.replace("ù", "u").replace("ú", "u").replace("ụ", "u")
                   .replace("ủ", "u").replace("ũ", "u").replace("ư", "u")
                   .replace("ừ", "u").replace("ứ", "u").replace("ự", "u")
                   .replace("ử", "u").replace("ữ", "u");
        
        text = text.replace("ỳ", "y").replace("ý", "y").replace("ỵ", "y")
                   .replace("ỷ", "y").replace("ỹ", "y");
        
        text = text.replace("đ", "d");
        
        // Chữ hoa
        text = text.replace("À", "A").replace("Á", "A").replace("Ạ", "A")
                   .replace("Ả", "A").replace("Ã", "A").replace("Â", "A")
                   .replace("Ầ", "A").replace("Ấ", "A").replace("Ậ", "A")
                   .replace("Ẩ", "A").replace("Ẫ", "A").replace("Ă", "A")
                   .replace("Ằ", "A").replace("Ắ", "A").replace("Ặ", "A")
                   .replace("Ẳ", "A").replace("Ẵ", "A");
        
        text = text.replace("È", "E").replace("É", "E").replace("Ẹ", "E")
                   .replace("Ẻ", "E").replace("Ẽ", "E").replace("Ê", "E")
                   .replace("Ề", "E").replace("Ế", "E").replace("Ệ", "E")
                   .replace("Ể", "E").replace("Ễ", "E");
        
        text = text.replace("Ì", "I").replace("Í", "I").replace("Ị", "I")
                   .replace("Ỉ", "I").replace("Ĩ", "I");
        
        text = text.replace("Ò", "O").replace("Ó", "O").replace("Ọ", "O")
                   .replace("Ỏ", "O").replace("Õ", "O").replace("Ô", "O")
                   .replace("Ồ", "O").replace("Ố", "O").replace("Ộ", "O")
                   .replace("Ổ", "O").replace("Ỗ", "O").replace("Ơ", "O")
                   .replace("Ờ", "O").replace("Ớ", "O").replace("Ợ", "O")
                   .replace("Ở", "O").replace("Ỡ", "O");
        
        text = text.replace("Ù", "U").replace("Ú", "U").replace("Ụ", "U")
                   .replace("Ủ", "U").replace("Ũ", "U").replace("Ư", "U")
                   .replace("Ừ", "U").replace("Ứ", "U").replace("Ự", "U")
                   .replace("Ử", "U").replace("Ữ", "U");
        
        text = text.replace("Ỳ", "Y").replace("Ý", "Y").replace("Ỵ", "Y")
                   .replace("Ỷ", "Y").replace("Ỹ", "Y");
        
        text = text.replace("Đ", "D");
        
        return text;
    }
}
