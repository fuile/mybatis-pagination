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
    private final SqlMappingBuilder sqlMappingBuilder = new SqlMappingBuilder();

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

    public void setDialect(String databaseId, Dialect dialect) {
        this.sqlMappingBuilder.setDialect(databaseId, dialect);
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executor = (Executor) invocation.getTarget();
        Object[] args = invocation.getArgs();
        MappedStatement ms = ObjectUtil.get(args[0]);
        Object parameterObject = ObjectUtil.get(args[1]);
        RowBounds rowBounds = ObjectUtil.get(args[2]);
        ResultHandler<?> resultHandler = ObjectUtil.get(args[3]);
        BoundSql boundSql = args.length == 4 ? ms.getBoundSql(parameterObject) : ObjectUtil.get(args[5]);
        CacheKey cacheKey = args.length == 4 ? executor.createCacheKey(ms, parameterObject, rowBounds, boundSql) : ObjectUtil.get(args[4]);

        if (ms.getConfiguration().getDatabaseId() == null) {
            return invocation.proceed();
        }

        if (parameterObject != null) {
            if (parameterObject instanceof Map) {
                Map<String, Object> newParameterObject = new HashMap<>();
                Map<String, Object> o = ObjectUtil.get(parameterObject);
                boolean isPage = false;
                for (Map.Entry<String, Object> entry : o.entrySet()) {
                    if (ObjectUtil.equals(entry.getValue(), this.paginationObject)) {
                        isPage = true;
                        newParameterObject.putAll(parsePage(executor, ms, boundSql, entry.getValue()));
                    } else {
                        DefaultPagination defaultPagination = PaginationHelper.get();
                        if (defaultPagination != null) {
                            isPage = true;
                            newParameterObject.putAll(parsePage(executor, ms, boundSql, defaultPagination));
                        } else {
                            newParameterObject.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return isPage ? sqlMappingBuilder.page(executor, ms, resultHandler, rowBounds, cacheKey, boundSql, newParameterObject) : invocation.proceed();
            } else if (ObjectUtil.equals(parameterObject, this.paginationObject)) {
                Map<String, Object> newParameter = parsePage(executor, ms, boundSql, parameterObject);
                return sqlMappingBuilder.page(executor, ms, resultHandler, rowBounds, cacheKey, boundSql, newParameter);
            } else {
                DefaultPagination defaultPagination = PaginationHelper.get();
                if (defaultPagination != null) {
                    Map<String, Object> newParameter = parsePage(executor, ms, boundSql, defaultPagination);
                    Field[] declaredFields = ObjectUtil.getDeclaredFields(parameterObject);
                    for (Field declaredField : declaredFields) {
                        newParameter.put(declaredField.getName(), declaredField.get(parameterObject));
                    }
                    return sqlMappingBuilder.page(executor, ms, resultHandler, rowBounds, cacheKey, boundSql, newParameter);
                }
            }
        } else {
            DefaultPagination defaultPagination = PaginationHelper.get();
            if (defaultPagination != null) {
                Map<String, Object> newParameter = parsePage(executor, ms, boundSql, defaultPagination);
                return sqlMappingBuilder.page(executor, ms, resultHandler, rowBounds, cacheKey, boundSql, newParameter);
            }
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
            Field offset = ObjectUtil.getDeclaredField(this.offsetProperty, parameterObject.getClass());
            Field limit = ObjectUtil.getDeclaredField(this.limitProperty, parameterObject.getClass());
            Field record = ObjectUtil.getDeclaredField(this.recordProperty, parameterObject.getClass());
            Field total = ObjectUtil.getDeclaredField(this.totalProperty, parameterObject.getClass());

            newParameter.putIfAbsent("offset", offset != null ? offset.get(parameterObject) : 0);
            newParameter.putIfAbsent("limit", limit != null ? limit.get(parameterObject) : 0);
            newParameter.put("record", record != null ? sqlMappingBuilder.count(executor, ms, boundSql).intValue() : 0);
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
