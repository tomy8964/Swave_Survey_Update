package com.example.surveydocument.survey.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;
    private int number;
    private int size;
    private int totalPages;
    private long totalElements;
    private boolean last;
    private Sort sort;
    private int numberOfElements;
    private boolean first;
    private boolean empty;
    private Pageable pageable;

    @Data
    @NoArgsConstructor
    public static class Sort {
        private boolean empty;
        private boolean sorted;
        private boolean unsorted;
    }

    @Data
    @NoArgsConstructor
    public static class Pageable {
        private Sort sort;
        private int offset;
        private int pageNumber;
        private int pageSize;
        private boolean paged;
        private boolean unpaged;
    }
}
