package com.campus.aihelp.mapper;

import com.campus.aihelp.domain.AiCallLog;
import com.campus.aihelp.domain.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogMapper {
    @Insert("insert into ai_call_log(feature,user_id,request_summary,response_summary,elapsed_ms,status) values(#{feature},#{userId},#{requestSummary},#{responseSummary},#{elapsedMs},#{status})")
    int insertAi(AiCallLog log);

    @Select("select * from ai_call_log order by created_at desc limit 50")
    List<AiCallLog> recentAi();

    @Insert("insert into operation_log(operation,username,ip,method,elapsed_ms,status) values(#{operation},#{username},#{ip},#{method},#{elapsedMs},#{status})")
    int insertOperation(OperationLog log);

    @Select("select * from operation_log order by created_at desc limit 80")
    List<OperationLog> recentOperations();
}
