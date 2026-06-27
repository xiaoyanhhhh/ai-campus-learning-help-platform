package com.campus.aihelp.mapper;

import com.campus.aihelp.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from users where username = #{username}")
    User findByUsername(String username);

    @Select("select * from users where id = #{id}")
    User findById(Long id);

    @Select("select r.code from role r join user_role ur on r.id = ur.role_id where ur.user_id = #{userId}")
    List<String> findRoleCodes(Long userId);

    @Select("select * from users order by created_at desc")
    List<User> findAll();

    @Select("select count(*) from users")
    int countUsers();

    @Insert("insert into users(username,password,real_name,student_no,email,status,points) values(#{username},#{password},#{realName},#{studentNo},#{email},#{status},#{points})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Insert("insert into role(code,name) select #{code}, #{name} where not exists(select 1 from role where code = #{code})")
    int insertRoleIfAbsent(@Param("code") String code, @Param("name") String name);

    @Insert("merge into user_role(user_id, role_id) key(user_id, role_id) select #{userId}, id from role where code = #{roleCode}")
    int addRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);

    @Delete("delete from user_role where user_id = #{userId}")
    int deleteRoles(Long userId);

    @Update("update users set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("update users set real_name=#{realName}, student_no=#{studentNo}, email=#{email} where id=#{id}")
    int updateProfile(User user);

    @Update("update users set points = points + #{delta} where id = #{userId}")
    int updatePoints(@Param("userId") Long userId, @Param("delta") int delta);
}
