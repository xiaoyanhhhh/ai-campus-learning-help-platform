package com.campus.aihelp.mapper;

import com.campus.aihelp.domain.HelpComment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HelpCommentMapper {
    @Select("""
            select hc.*, u.real_name user_name
            from help_comment hc
            join users u on hc.user_id = u.id
            where hc.help_id = #{helpId}
            order by hc.created_at asc
            """)
    List<HelpComment> findByHelpId(Long helpId);

    @Select("select count(*) from help_comment where help_id=#{helpId} and user_id=#{userId} and content=#{content}")
    int countSame(@Param("helpId") Long helpId, @Param("userId") Long userId, @Param("content") String content);

    @Insert("insert into help_comment(help_id,user_id,content) values(#{helpId},#{userId},#{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HelpComment comment);
}
