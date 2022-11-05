package com.pzhu.mybatisplusfilter;

import com.pzhu.mybatisplusfilter.mapper.UserMapper;
import com.pzhu.mybatisplusfilter.query.SearchWrapper;
import com.pzhu.mybatisplusfilter.search.UserSearch;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@MapperScan("com.pzhu.**.mapper")
class MybatisPlusFilterApplicationTests {
    @Resource
    private UserMapper userMapper;
    @Test
    void testSearchBean(){
        User user = new User();
        user.setEmail("127@qq.com");
        user.setName("test");
        user.setAge(23);
        user.setId(7L);
        userMapper.insert(user);
        String filter = "name $sw '名字' AND dateCreated >'2017-11-27T03:16:03.94Z'";
        String orderBy  = "name desc,age";
        final QueryConditions queryConditions = new QueryConditions();
        queryConditions.setOrderBy(orderBy);
        queryConditions.setFilter(filter);
        final SearchWrapper searchWrapper = queryConditions.createSearchWrapper(UserSearch.class);
        System.out.println(searchWrapper.getSqlSegment());
        final List<UserSearch> userSearches = userMapper.testSearch(searchWrapper);
        System.out.println(userSearches);
    }

}
