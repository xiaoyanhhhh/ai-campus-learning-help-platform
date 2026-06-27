package com.campus.aihelp.mapper;

import com.campus.aihelp.domain.ResourceItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResourceMapper {
    String BASE = "select r.*, c.name course_name, u.real_name uploader_name, "
            + "(select group_concat(t.name order by t.name separator ', ') from resource_tag rt join tag t on rt.tag_id=t.id where rt.resource_id=r.id) tag_names "
            + "from resource r join course c on r.course_id=c.id join users u on r.uploader_id=u.id";

    @Select("""
            <script>
            select r.*, c.name course_name, u.real_name uploader_name,
              (select group_concat(t.name order by t.name separator ', ') from resource_tag rt join tag t on rt.tag_id=t.id where rt.resource_id=r.id) tag_names
            from resource r
            join course c on r.course_id=c.id
            join users u on r.uploader_id=u.id
            where r.audit_status='APPROVED'
            <if test="keyword != null">
              and (lower(r.title) like concat('%', lower(#{keyword}), '%')
                or lower(r.description) like concat('%', lower(#{keyword}), '%')
                or lower(r.chapter) like concat('%', lower(#{keyword}), '%'))
            </if>
            <if test="courseId != null">and r.course_id = #{courseId}</if>
            <if test="type != null">and r.type = #{type}</if>
            <if test="tag != null">
              and exists (
                select 1 from resource_tag rt
                join tag t on rt.tag_id = t.id
                where rt.resource_id = r.id and t.name = #{tag}
              )
            </if>
            <choose>
              <when test="sort == 'views'">order by r.view_count desc, r.created_at desc</when>
              <when test="sort == 'rating'">order by r.rating desc, r.created_at desc</when>
              <otherwise>order by r.created_at desc</otherwise>
            </choose>
            limit #{limit} offset #{offset}
            </script>
            """)
    List<ResourceItem> findApproved(@Param("keyword") String keyword,
                                    @Param("courseId") Long courseId,
                                    @Param("type") String type,
                                    @Param("tag") String tag,
                                    @Param("sort") String sort,
                                    @Param("limit") int limit,
                                    @Param("offset") int offset);

    @Select("""
            <script>
            select count(*)
            from resource r
            where r.audit_status='APPROVED'
            <if test="keyword != null">
              and (lower(r.title) like concat('%', lower(#{keyword}), '%')
                or lower(r.description) like concat('%', lower(#{keyword}), '%')
                or lower(r.chapter) like concat('%', lower(#{keyword}), '%'))
            </if>
            <if test="courseId != null">and r.course_id = #{courseId}</if>
            <if test="type != null">and r.type = #{type}</if>
            <if test="tag != null">
              and exists (
                select 1 from resource_tag rt
                join tag t on rt.tag_id = t.id
                where rt.resource_id = r.id and t.name = #{tag}
              )
            </if>
            </script>
            """)
    int countApproved(@Param("keyword") String keyword,
                      @Param("courseId") Long courseId,
                      @Param("type") String type,
                      @Param("tag") String tag);

    @Select(BASE + " where r.audit_status='PENDING' order by r.created_at desc")
    List<ResourceItem> findPending();

    @Select(BASE + " where r.id = #{id}")
    ResourceItem findById(Long id);

    @Select("select id from resource where lower(title) like concat('%', lower(#{keyword}), '%') order by id limit 1")
    Long findIdByTitleKeyword(String keyword);

    @Select(BASE + " where r.title = #{title}")
    ResourceItem findByTitle(String title);

    @Select(BASE + " where r.audit_status='APPROVED' order by r.view_count desc limit #{limit}")
    List<ResourceItem> hotResources(int limit);

    @Insert("insert into resource(title,type,chapter,description,file_path,course_id,uploader_id,audit_status) values(#{title},#{type},#{chapter},#{description},#{filePath},#{courseId},#{uploaderId},#{auditStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ResourceItem item);

    @Update("update resource set audit_status=#{status} where id=#{id}")
    int updateAuditStatus(@Param("id") Long id, @Param("status") String status);

    @Update("update resource set view_count = view_count + 1 where id=#{id}")
    int increaseView(Long id);

    @Insert("merge into favorite(user_id, resource_id) key(user_id, resource_id) values(#{userId}, #{resourceId})")
    int favorite(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
}
