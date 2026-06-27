package com.campus.aihelp.service;

import com.campus.aihelp.aop.AuditLog;
import com.campus.aihelp.domain.HelpRequest;
import com.campus.aihelp.mapper.HelpRequestMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HelpService {
    private final HelpRequestMapper helpRequestMapper;
    private final PointService pointService;

    public HelpService(HelpRequestMapper helpRequestMapper, PointService pointService) {
        this.helpRequestMapper = helpRequestMapper;
        this.pointService = pointService;
    }

    public List<HelpRequest> all() {
        return helpRequestMapper.findAll();
    }

    public HelpRequest get(Long id) {
        return helpRequestMapper.findById(id);
    }

    @AuditLog("发布学习求助")
    public void create(HelpRequest request) {
        request.setStatus("OPEN");
        helpRequestMapper.insert(request);
    }

    @AuditLog("认领学习求助")
    public void claim(Long id, Long helperId) {
        if (helpRequestMapper.claim(id, helperId) == 0) {
            throw new IllegalStateException("当前求助不可认领");
        }
    }

    @AuditLog("提交互助解答")
    public void submit(Long id, Long helperId, String solution) {
        if (helpRequestMapper.submitSolution(id, helperId, solution) == 0) {
            throw new IllegalStateException("当前求助不可提交解答");
        }
    }

    @AuditLog("确认互助完成")
    @Transactional
    public void complete(Long id, Long publisherId, String evaluation) {
        HelpRequest request = helpRequestMapper.findById(id);
        if (request == null || request.getHelperId() == null) {
            throw new IllegalStateException("求助记录不存在或尚未认领");
        }
        if (helpRequestMapper.complete(id, publisherId, evaluation) == 0) {
            throw new IllegalStateException("当前求助不可确认完成");
        }
        int bounty = request.getBountyPoints() == null ? 0 : request.getBountyPoints();
        if (bounty > 0) {
            pointService.changePoints(request.getPublisherId(), -bounty, "HELP_BOUNTY", id, "支付互助悬赏积分");
            pointService.changePoints(request.getHelperId(), bounty, "HELP_BOUNTY", id, "完成互助获得悬赏积分");
        }
        pointService.changePoints(request.getHelperId(), 10, "HELP_COMPLETED", id, "互助完成奖励");
    }
}
