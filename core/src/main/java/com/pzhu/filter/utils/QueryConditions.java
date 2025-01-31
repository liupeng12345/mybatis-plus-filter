package com.pzhu.filter.utils;

import com.pzhu.filter.metadata.FilterBeanField;
import com.pzhu.filter.metadata.FilterBeanInfo;
import com.pzhu.filter.metadata.FilterBeanInfoHelper;
import com.pzhu.filter.wrapper.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@NoArgsConstructor
public abstract class QueryConditions<T extends QueryWrapper> {

    public static final String FILTER = "filter";
    public static final String ORDER = "order";
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGE = "page";

    protected int page = 1;

    protected int pageSize = 20;

    protected String filter;

    protected String order;

    protected FilterBeanInfo filterBeanInfo;

    protected abstract void pageInfo(T queryWrapper);

    public T wrapper(Class<?> searchBeanClass) {
        return Optional.ofNullable(FilterBeanInfoHelper.getInfo(searchBeanClass))
                .map(searchBean -> {
                    T t = create();
                    filterBeanInfo = searchBean;
                    loadFilter(t);
                    pageInfo(t);
                    loadOrderBy(t);
                    return t;
                })
                .orElseThrow();
    }

    public void init(int page, int pageSize, String filter, String order) {
        this.page = page;
        this.pageSize = pageSize;
        this.filter = filter;
        this.order = order;
    }

    public abstract T create();

    protected void loadOrderBy(T queryWrapper) {
        decodeInfo(order)
                .ifPresentOrElse(
                        orderDecode -> {
                            order = orderDecode;
                            doLoadOrderBy(queryWrapper);
                        },
                        () -> {});
    }

    protected void loadFilter(T queryWrapper) {
        decodeInfo(filter)
                .ifPresentOrElse(
                        decodeInfo -> {
                            filter = decodeInfo;
                            doLoadFilter(queryWrapper);
                        },
                        () -> {});
    }

    protected abstract void doLoadOrderBy(T queryWrapper);

    protected abstract void doLoadFilter(T queryWrapper);

    public QueryConditions(int page, int pageSize, String filter, String order) {
        this.page = page;
        this.pageSize = pageSize;
        this.filter = filter;
        this.order = order;
    }

    public QueryConditions(String filter, String order) {
        this.filter = filter;
        this.order = order;
    }

    protected enum OrderType {
        ASC,
        DESC;

        public static OrderType form(Object type) {
            Optional.ofNullable(type).orElseThrow();
            if (type instanceof OrderType) {
                return (OrderType) type;
            }
            return switch (type.toString().toLowerCase()) {
                case "ase" -> ASC;
                case "desc" -> DESC;
                default -> null;
            };
        }
    }

    /**
     * 封装
     *
     * @return
     */
    protected List<OrderByCondition> orderByCondition() {
        return Arrays.stream(order.split(","))
                .map(orderInfo -> {
                    final Map<String, FilterBeanField> searchBeanFieldMap = filterBeanInfo.getSearchBeanFieldMap();
                    final String[] split = orderInfo.split(String.format("%s\\+", ' '));
                    final FilterBeanField filterBeanField = searchBeanFieldMap.get(split[0]);
                    if (filterBeanField != null) {
                        if (split.length == 1) {
                            return new OrderByCondition(filterBeanField.getDbField(), OrderType.ASC);
                        } else if (split.length == 2) {
                            final OrderType type = OrderType.form(split[1].trim());
                            return new OrderByCondition(filterBeanField.getDbField(), type);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @AllArgsConstructor
    @Data
    protected static class OrderByCondition {
        private String field;
        private OrderType orderType;

        @Override
        public String toString() {
            return String.format(" %s %s ", field, orderType.name());
        }
    }

    protected Optional<String> decodeInfo(String info) {
        return Optional.ofNullable(info)
                .filter(StringUtils::isNotBlank)
                .map(orderInfo -> URLDecoder.decode(orderInfo, StandardCharsets.UTF_8));
    }
}