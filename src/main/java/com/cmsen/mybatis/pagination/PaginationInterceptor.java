/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class PaginationInterceptor implements Interceptor {
    private Class<?> paginationObject = AbstractPagination.class;
    private String offsetProperty = "offset";
    private String limitProperty = "limit";
    private String recordProperty = "record";
    private String totalProperty = "total";
    private boolean disableCount;

    public PaginationInterceptor() {
    }

    public PaginationInterceptor(Class<?> paginationObject) {
        this.paginationObject = paginationObject;
    }

    public void setOffsetProperty(String offsetProperty) {
        this.offsetProperty = offsetProperty;
    }

    public void setLimitProperty(String limitProperty) {
        this.limitProperty = limitProperty;
    }

    public void setRecordProperty(String recordProperty) {
        this.recordProperty = recordProperty;
    }

    public void setTotalProperty(String totalProperty) {
        this.totalProperty = totalProperty;
    }

    public void setDisableCount(boolean disableCount) {
        this.disableCount = disableCount;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
        CacheKey cacheKey;
        BoundSql boundSql;
        if (args.length == 4) {
            boundSql = ms.getBoundSql(parameterObject);
            cacheKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
        } else {
            cacheKey = (CacheKey) args[4];
            boundSql = (BoundSql) args[5];
        }

        if (parameterObject != null) {
            if (parameterObject instanceof Map) {
                Map<String, Object> newParameterObject = new HashMap<>();
                Map<String, Object> o = ObjectUtil.get(parameterObject);
                for (Map.Entry<String, Object> entry : o.entrySet()) {
                    if (ObjectUtil.equals(entry.getValue(), this.paginationObject)) {
                        newParameterObject.putAll(parsePage(executor, ms, boundSql, entry.getValue()));
                    } else {
                        newParameterObject.put(entry.getKey(), entry.getValue());
                    }
                }
                PageSqlWrapper pageSqlWrapper = new PageSqlWrapper(executor, ms, resultHandler, rowBounds, cacheKey, boundSql, newParameterObject);
                return pageSqlWrapper.getValue();
            } else if (ObjectUtil.equals(parameterObject, this.paginationObject)) {
                Map<String, Object> newParameter = parsePage(executor, ms, boundSql, parameterObject);
                PageSqlWrapper pageSqlWrapper = new PageSqlWrapper(executor, ms, resultHandler, rowBounds, cacheKey, boundSql, newParameter);
                return pageSqlWrapper.getValue();
            }
        } else if (PaginationHelper.get() != null) {
            Map<String, Object> newParameter = parsePage(executor, ms, boundSql, PaginationHelper.get());
            PaginationHelper.clear();
            PageSqlWrapper pageSqlWrapper = new PageSqlWrapper(executor, ms, resultHandler, rowBounds, cacheKey, boundSql, newParameter);
            return pageSqlWrapper.getValue();
        }

        return invocation.proceed();
    }

    private Map<String, Object> parsePage(Executor executor, MappedStatement ms, BoundSql boundSql, Object parameterObject) throws IllegalAccessException, NoSuchFieldException, SQLException {
        Map<String, Object> newParameter = new HashMap<>();
        Field[] declaredFields = ObjectUtil.getDeclaredFields(parameterObject);
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Offset.class)) {
                newParameter.put("offset", declaredField.get(parameterObject));
            } else if (declaredField.isAnnotationPresent(Limit.class)) {
                newParameter.put("limit", declaredField.get(parameterObject));
            } else {
                newParameter.put(declaredField.getName(), declaredField.get(parameterObject));
            }
        }
        if (!disableCount) {
            CountSqlWrapper countSqlWrapper = new CountSqlWrapper(executor, ms, boundSql);
            Field offset = ObjectUtil.getDeclaredField(this.offsetProperty, parameterObject.getClass());
            Field limit = ObjectUtil.getDeclaredField(this.limitProperty, parameterObject.getClass());
            Field record = ObjectUtil.getDeclaredField(this.recordProperty, parameterObject.getClass());
            Field total = ObjectUtil.getDeclaredField(this.totalProperty, parameterObject.getClass());

            newParameter.putIfAbsent("offset", offset != null ? offset.get(parameterObject) : 0);
            newParameter.putIfAbsent("limit", limit != null ? limit.get(parameterObject) : 0);
            newParameter.put("record", record != null ? countSqlWrapper.getValue().intValue() : 0);
            newParameter.put("total", total != null ? (int) Math.ceil(1.0 * ((Number) newParameter.get("record")).intValue() / ((Number) newParameter.get("limit")).intValue()) : 0);

            if (record != null) {
                record.set(parameterObject, newParameter.get("record"));
            }
            if (total != null) {
                total.set(parameterObject, newParameter.get("total"));
            }
        } else {
            Field offset = ObjectUtil.getDeclaredField(this.offsetProperty, parameterObject.getClass());
            Field limit = ObjectUtil.getDeclaredField(this.limitProperty, parameterObject.getClass());
            newParameter.putIfAbsent("offset", offset != null ? offset.get(parameterObject) : 0);
            newParameter.putIfAbsent("limit", limit != null ? limit.get(parameterObject) : 0);
        }
        return newParameter;
    }
}
