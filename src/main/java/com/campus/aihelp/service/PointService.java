package com.campus.aihelp.service;

import com.campus.aihelp.domain.PointRecord;
import com.campus.aihelp.mapper.PointMapper;
import com.campus.aihelp.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {
    private final PointMapper pointMapper;
    private final UserMapper userMapper;

    public PointService(PointMapper pointMapper, UserMapper userMapper) {
        this.pointMapper = pointMapper;
        this.userMapper = userMapper;
    }

    public void changePoints(Long userId, int delta, String source, Long bizId, String remark) {
        userMapper.updatePoints(userId, delta);
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setChangeValue(delta);
        record.setSource(source);
        record.setBizId(bizId);
        record.setRemark(remark);
        pointMapper.insert(record);
    }

    public List<PointRecord> myRecords(Long userId) {
        return pointMapper.findByUser(userId);
    }
}
