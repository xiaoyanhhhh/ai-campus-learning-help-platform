package com.campus.aihelp.mapper;

import com.campus.aihelp.domain.Course;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper {
    @Select("select * from course order by name")
    List<Course> findAll();

    @Select("select * from course where name = #{name}")
    Course findByName(String name);

    @Insert("insert into course(name, description) values(#{name}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);
}
