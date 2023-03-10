package com.example.modules.stu.service.impl;

import com.example.core.base.service.impl.BaseServiceimpl;
import com.example.modules.stu.dto.CourseScoreStatDTO;
import com.example.modules.stu.dto.ScoreStatDTO;
import com.example.modules.stu.entity.Score;
import com.example.modules.stu.mapper.ScoreMapper;
import com.example.modules.stu.service.IScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 学生成绩
 * @Author: jeecg-boot
 * @Date: 2022-12-29
 * @Version: V1.0
 */
@Service
public class ScoreServiceImpl extends BaseServiceimpl<ScoreMapper, Score> implements IScoreService {

    @Autowired
    private ScoreMapper scoreMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMain(Score score) {
        scoreMapper.insert(score);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMain(Score score) {
        scoreMapper.updateById(score);

        //1.先删除子表数据

        //2.子表数据重新插入
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delMain(String id) {
        scoreMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchMain(Collection<? extends Serializable> idList) {
        for (Serializable id : idList) {
            scoreMapper.deleteById(id);
        }
    }

    @Override
    public ScoreStatDTO getStuScoreStat(String userId) {
        List<ScoreStatDTO> list = scoreMapper.getStuScoreStatAll(new ArrayList<String>(Arrays.asList(userId)));
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Map<String, ScoreStatDTO> getStuScoreStat(List<String> userIds) {
        List<ScoreStatDTO> stuScoreStats = scoreMapper.getStuScoreStatAll(userIds);
        return stuScoreStats.stream().collect(Collectors.toMap(ScoreStatDTO::getUserId, e -> e));
    }

    @Override
    public Map<String, CourseScoreStatDTO> getCourseScoreStat(List<String> courseIds) {
        List<CourseScoreStatDTO> list = scoreMapper.getCourseScoreStat(courseIds);
        return list.stream().collect(Collectors.toMap(CourseScoreStatDTO::getId, e -> e));
    }
}
