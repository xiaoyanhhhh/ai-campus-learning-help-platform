package com.campus.aihelp.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper {
    @Select("select name from tag order by name")
    List<String> findAllNames();

    @Select("select id from tag where name = #{name}")
    Long findIdByName(String name);

    @Insert("merge into tag(name) key(name) values(#{name})")
    int insertIfAbsent(String name);

    @Insert("merge into resource_tag(resource_id, tag_id) key(resource_id, tag_id) values(#{resourceId}, #{tagId})")
    int linkResource(@Param("resourceId") Long resourceId, @Param("tagId") Long tagId);
}
