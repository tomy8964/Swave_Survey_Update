package com.example.surveydocument.util.page;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;

public class PageImplCustom extends PageImpl {
    public PageImplCustom(List content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PageImplCustom(List content) {
        super(content);
    }

    @Override
    public int getTotalPages() {
        return super.getTotalPages();
    }

    @Override
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @Override
    public boolean hasNext() {
        return super.hasNext();
    }

    @Override
    public boolean isLast() {
        return super.isLast();
    }

    @Override
    public Page map(Function converter) {
        return super.map(converter);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
