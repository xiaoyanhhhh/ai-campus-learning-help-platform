package com.campus.aihelp.mapper;

import com.campus.aihelp.domain.HelpRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface HelpRequestMapper {
    String BASE = "select h.*, c.name course_name, p.real_name publisher_name, hp.real_name helper_name from help_request h join course c on h.course_id=c.id join users p on h.publisher_id=p.id left join users hp on h.helper_id=hp.id";

    @Select(BASE + " order by h.created_at desc")
    List<HelpRequest> findAll();

    @Select(BASE + " where h.id=#{id}")
    HelpRequest findById(Long id);

    @Select(BASE + " where h.title = #{title}")
    HelpRequest findByTitle(String title);

    @Select(BASE + " where h.status = #{status} order by h.created_at desc")
    List<HelpRequest> findByStatus(String status);

    @Select("select status, count(*) cnt from help_request group by status")
    List<java.util.Map<String, Object>> countByStatus();

    @Insert("insert into help_request(title,description,course_id,tags,publisher_id,status,bounty_points,deadline) values(#{title},#{description},#{courseId},#{tags},#{publisherId},#{status},#{bountyPoints},#{deadline})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HelpRequest request);

    @Update("update help_request set helper_id=#{helperId}, status='CLAIMED', claimed_at=current_timestamp where id=#{id} and status='OPEN'")
    int claim(@Param("id") Long id, @Param("helperId") Long helperId);

    @Update("update help_request set solution=#{solution}, status='WAIT_CONFIRM', submitted_at=current_timestamp where id=#{id} and helper_id=#{helperId} and status='CLAIMED'")
    int submitSolution(@Param("id") Long id, @Param("helperId") Long helperId, @Param("solution") String solution);

    @Update("update help_request set evaluation=#{evaluation}, status='COMPLETED', completed_at=current_timestamp where id=#{id} and publisher_id=#{publisherId} and status='WAIT_CONFIRM'")
    int complete(@Param("id") Long id, @Param("publisherId") Long publisherId, @Param("evaluation") String evaluation);

    @Update("update help_request set status='CLOSED' where id=#{id} and publisher_id=#{publisherId} and status in ('OPEN','CLAIMED')")
    int close(@Param("id") Long id, @Param("publisherId") Long publisherId);
}
