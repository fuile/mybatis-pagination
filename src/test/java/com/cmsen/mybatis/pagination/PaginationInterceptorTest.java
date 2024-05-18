package com.cmsen.mybatis.pagination;

import com.cmsen.mybatis.pagination.mapper.TestMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
public class PaginationInterceptorTest {
    private static final Logger log = LoggerFactory.getLogger(PaginationInterceptorTest.class);

    private static SqlSessionFactory sqlSessionFactory;

    static {
        try (Reader resourceAsReader = Resources.getResourceAsReader("mybatis-config.xml")) {
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
            sqlSessionFactory = sqlSessionFactoryBuilder.build(resourceAsReader);
            Configuration configuration = sqlSessionFactory.getConfiguration();
            configuration.setDatabaseId("mysql");
            configuration.addInterceptor(new PaginationInterceptor());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @Test
    public void test() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            TestMapper mapper = sqlSession.getMapper(TestMapper.class);
            DefaultPagination of = PaginationHelper.of(0, 3);
            Entity o = new Entity("test entity");
            o = null;

            List<Object> select = mapper.select(new NodeEntity("1,2"));
            log.info("{}", select);
            log.info("of: {}", of.record);
        }
    }

    @Test
    public void test_pagination() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            TestMapper mapper = sqlSession.getMapper(TestMapper.class);
            Page page = Page.of(1, 20);
            Entity o = new Entity("test entity");
            List<Object> select = mapper.select(o, page);
            List<Object> select2 = mapper.select(1, 7);
            System.out.println("select = " + select);
            System.out.println("select2 = " + select2);
            System.out.println("page = " + page);
        }
    }

    public static class Entity {
        private String name;

        public Entity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

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


        @Override
        public String toString() {
            return "Page{" +
                    "offset=" + offset +
                    ", limit=" + limit +
                    ", total=" + total +
                    ", record=" + record +
                    '}';
        }
    }
}