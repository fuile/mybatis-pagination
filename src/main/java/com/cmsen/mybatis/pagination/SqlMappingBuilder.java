/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

import com.cmsen.mybatis.pagination.dialect.*;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.*;

public class SqlMappingBuilder {
    private final Map<String, Dialect> dialect = new HashMap<>();

    public SqlMappingBuilder() {
        setDialect("hsqldb", new HsqldbDialect());
        setDialect("sqlite", getDialect("hsqldb"));
        setDialect("mysql", new MySqlDialect());
        setDialect("dm", getDialect("mysql"));
        setDialect("sqlserver", new SqlServerDialect());
        setDialect("postgresql", new PostgreSqlDialect());
        setDialect("oracle", new OracleDialect());
    }

    public void setDialect(String databaseId, Dialect dialect) {
        this.dialect.put(databaseId.toLowerCase(), dialect);
    }

    public Dialect getDialect(String databaseId) {
        return this.dialect.get(databaseId.toLowerCase());
    }

    public Object page(Executor executor, MappedStatement ms, ResultHandler<?> resultHandler, RowBounds rowBounds, CacheKey cacheKey, BoundSql boundSql, Map<String, Object> parameterObject) throws NoSuchFieldException, IllegalAccessException, SQLException {
        Map<String, Object> additionalParameters = ObjectUtil.getAdditionalParameters(boundSql);
        Configuration configuration = ms.getConfiguration();
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
        Dialect dialect = this.dialect.get(configuration.getDatabaseId());
        String pageSql = dialect.getPageSql(boundSql.getSql(), parameterObject, cacheKey);
        SqlSource sqlSource = sqlSourceBuilder.parse(pageSql, Object.class, additionalParameters);
        BoundSql newBoundSql = sqlSource.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = new LinkedList<>(boundSql.getParameterMappings());
        parameterMappings.addAll(newBoundSql.getParameterMappings());
        BoundSql pageBoundSql = new BoundSql(configuration, newBoundSql.getSql(), parameterMappings, parameterObject);
        for (String additionalParametersKey : additionalParameters.keySet()) {
            if ("_parameter".equals(additionalParametersKey)) {
                pageBoundSql.setAdditionalParameter(additionalParametersKey, parameterObject);
                continue;
            }
            pageBoundSql.setAdditionalParameter(additionalParametersKey, additionalParameters.get(additionalParametersKey));
        }
        MappedStatement pageMs = MappedStatementBuilder.build(ms, ms.getId() + "Page", ms.getSqlSource(), ms.getParameterMap(), ms.getResultMaps());
        return executor.query(pageMs, parameterObject, rowBounds, resultHandler, cacheKey, pageBoundSql);
    }

    public Number count(Executor executor, MappedStatement ms, BoundSql boundSql) throws NoSuchFieldException, IllegalAccessException, SQLException {
        Configuration configuration = ms.getConfiguration();
        Dialect dialect = this.dialect.get(configuration.getDatabaseId());
        BoundSql countBoundSql = new BoundSql(ms.getConfiguration(), dialect.getCountSql(boundSql.getSql()), boundSql.getParameterMappings(), boundSql.getParameterObject());
        Map<String, Object> additionalParameters = ObjectUtil.getAdditionalParameters(boundSql);
        for (String additionalParametersKey : additionalParameters.keySet()) {
            countBoundSql.setAdditionalParameter(additionalParametersKey, additionalParameters.get(additionalParametersKey));
        }
        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(new ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class, new ArrayList<>(0)).build());
        MappedStatement countMs = MappedStatementBuilder.build(ms, ms.getId() + "Count", ms.getSqlSource(), ms.getParameterMap(), resultMaps);
        CacheKey countCacheKey = executor.createCacheKey(countMs, countBoundSql.getParameterObject(), RowBounds.DEFAULT, countBoundSql);
        List<Object> countResultList = executor.query(countMs, countBoundSql.getParameterObject(), RowBounds.DEFAULT, null, countCacheKey, countBoundSql);
        return countResultList != null && !countResultList.isEmpty() ? ((Number) countResultList.get(0)).longValue() : 0;
    }
}
