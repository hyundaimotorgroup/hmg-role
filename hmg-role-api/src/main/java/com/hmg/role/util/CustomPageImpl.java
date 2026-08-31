package com.hmg.role.util;

import java.util.List;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class CustomPageImpl<T> extends PageImpl<T> {

    private final Pageable pageable;
    private final long totalElements;

    public CustomPageImpl(List<T> content, Pageable pageable, long totalElements) {
        super(content, pageable, totalElements);
        this.totalElements = totalElements;
        this.pageable = pageable;
    }

    @Override
    public int getTotalPages() {
        return this.countTotalPage();
    }

    private int countTotalPage() {
        return (int) Math.ceil((double) this.totalElements / this.pageable.getPageSize());
    }
}
