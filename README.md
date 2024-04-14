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

### Usage

```java

@Mapper
public interface TestMapper {
    List<Object> select();

    // Using built-in class
    List<Object> select(DefaultPagination page);

    List<Object> select(Object entity, DefaultPagination page);

    // Custom page class
    List<Object> select(Page page);

    List<Object> select(Object entity, Page page);

    // Using annotations
    List<Object> select(@Offset int offset, @Limit int limit);
}
```

### Custom page

```java
public static class Page extends AbstractPagination {
    //        int offset;
    //        int limit;
    //        int total;
    //        int record;

    public Page(int limit) {
        this.limit = limit;
    }

    public Page(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public static Page of(int limit) {
        return new Page(limit);
    }

    public static Page of(int offset, int limit) {
        return new Page(offset, limit);
    }
}
```

```java
public class TestUsing {
    private TestMapper mapper;

    @Test
    public void paginationHelper() {
        DefaultPagination pagination = PaginationHelper.of(1, 30);
        List<Object> results = mapper.select();
        System.out.println("pagination = " + pagination);
        System.out.println("results = " + results);
    }

    @Test
    public void usingAnnotation() {
        List<Object> results = mapper.select(1, 30);
        System.out.println("results = " + results);
    }

    @Test
    public void customPage() {
        Page page = Page.of(1, 20);
        Object entity = new HashMap<>();
        // List<Object> results = mapper.select(page);
        List<Object> results = mapper.select(entity, page);
        System.out.println("page = " + page);
        System.out.println("results = " + results);
    }
}
```