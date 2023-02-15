package im.bzh.dao;

import im.bzh.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDAO extends BaseMapper<User> {

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    @Update("update user set password = #{password} where username = #{username}")
    int updatePassword(User user);

    @Update("update user set nickname = #{nickname} where username = #{username}")
    int updateNickname(User user);

    @Delete("delete from user where username = #{username}")
    int deleteUserByUsername(String username);

}
