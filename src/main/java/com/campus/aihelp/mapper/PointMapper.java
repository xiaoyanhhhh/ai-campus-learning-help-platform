package com.campus.aihelp.mapper;

import com.campus.aihelp.domain.PointRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PointMapper {
    @Insert("insert into point_record(user_id, change_value, source, biz_id, remark) values(#{userId},#{changeValue},#{source},#{bizId},#{remark})")
    int insert(PointRecord record);

    @Select("select pr.*, u.real_name from point_record pr join users u on pr.user_id=u.id where pr.user_id=#{userId} order by pr.created_at desc")
    List<PointRecord> findByUser(Long userId);

    @Select("select pr.*, u.real_name from point_record pr join users u on pr.user_id=u.id order by pr.created_at desc limit #{limit}")
    List<PointRecord> recent(@Param("limit") int limit);

    @Select("select real_name, points from users order by points desc limit #{limit}")
    List<java.util.Map<String, Object>> rank(@Param("limit") int limit);

    @Select("select count(*) from point_record where user_id=#{userId} and source=#{source} and biz_id=#{bizId}")
    int countByUserSourceBiz(@Param("userId") Long userId, @Param("source") String source, @Param("bizId") Long bizId);
}
