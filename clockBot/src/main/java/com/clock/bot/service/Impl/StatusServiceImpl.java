package com.clock.bot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clock.bot.mapper.StatusMapper;
import com.clock.bot.mapper.UserMapper;
import com.clock.bot.pojo.Status;
import com.clock.bot.pojo.User;
import com.clock.bot.service.StatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Component
@Service
@Slf4j
public class StatusServiceImpl implements StatusService {
    @Autowired
    StatusMapper statusMapper;

    @Override
    public Status getInitStatus(String groupId, String groupTitle) {
        Status status = selectGroupId(groupId);
        if (status==null) {
            status = new Status();
            //初始化状态信息
            status.setGroupId(groupId);
            status.setGroupTitle(groupTitle);
            status.setRiqie(true);
            insertStatus(status);
        }
        return status;
    }

    public void update(Status status){
        statusMapper.updateById(status);
    }

    @Override
    public List<Status> selcectStatusList() {
        return statusMapper.selectList(null);
    }

    public Status selectGroupId(String groupId) {
        QueryWrapper<Status> wrapper = new QueryWrapper();
        wrapper.eq("group_id", groupId);
        return statusMapper.selectOne(wrapper);
    }
    public void insertStatus(Status status) {
        statusMapper.insert(status);
    }
}
