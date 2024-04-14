# MyBatis pagination plugin

### Default support database

- Hsqldb
- Sqlite
- MySql
- DM
- SqlServer
- PostgreSql
- Oracle

### Custom Other database support

```java
public class CustomOtherDb implements Dialect {
    @Override
    public String getCountSql(String sql) {
        // Count statement...
        return sql;
    }

    @Override
    public String getPageSql(String sql, Map<String, Object> parameter, CacheKey cacheKey) {
        // pagination statement...
        return sql;
    }
}
```

### MyBatis configuration

```java
public class MyBatisConfig {
    private static SqlSessionFactory sqlSessionFactory;

    static {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        PaginationInterceptor pageInterceptor = new PaginationInterceptor(Pagination.class);
        // Add database type support
        pageInterceptor.setDialect("otherDb", new CustomOtherDb());
        configuration.setDatabaseId("otherDb");
        // Pagination parameter
        pageInterceptor.setOffsetProperty("index");
        pageInterceptor.setLimitProperty("size");
        pageInterceptor.setRecordProperty("record");
        pageInterceptor.setTotalProperty("total");
        // Add pagination plug-in
        configuration.addInterceptor(pageInterceptor);
    }
}
```