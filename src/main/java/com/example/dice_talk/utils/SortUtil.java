package com.example.dice_talk.utils;
import org.springframework.data.domain.Sort;

import java.util.Set;

public class SortUtil {

    public static Sort parseSortParam(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "requestedAt");
        }

        String[] parts = sortParam.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "requestedAt");
        }

        String sortBy = parts[0].trim();
        String direction = parts[1].trim();

        Sort.Direction sortDirection =
                direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        // 허용된 정렬 필드만 통과
        Set<String> allowedFields = Set.of("requestedAt", "amount", "paymentStatus", "completedAt");

        if (!allowedFields.contains(sortBy)) {
            sortBy = "requestedAt";
        }

        return Sort.by(sortDirection, sortBy);
    }
}
