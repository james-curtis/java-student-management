package com.example.modules.stu.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.core.base.controller.BaseRestController;
import com.example.modules.stu.bo.SysUserBO;
import com.example.modules.stu.dto.ScoreStatDTO;
import com.example.modules.stu.entity.Score;
import com.example.modules.stu.entity.TeachingPlan;
import com.example.modules.stu.manager.SysUserManager;
import com.example.modules.stu.service.IScoreService;
import com.example.modules.stu.service.ITeachingPlanService;
import com.example.modules.stu.vo.ScoreStatVO;
import com.example.modules.stu.vo.ScoreVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: ????????????
 * @Author: jeecg-boot
 * @Date: 2022-12-29
 * @Version: V1.0
 */
@Api(tags = "????????????")
@RestController
@RequestMapping("/stu/score")
@Slf4j
public class ScoreController extends BaseRestController<Score, String> {

    @Autowired
    private IScoreService scoreService;

    @Autowired
    private SysUserManager sysUserManager;

    @Autowired
    private ITeachingPlanService teachingPlanService;

    /**
     * ??????????????????
     *
     * @param score
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    // @AutoLog(value = "????????????-??????????????????")
    @ApiOperation(value = "????????????-??????????????????", notes = "????????????-??????????????????")
    @GetMapping(value = "/list")
    public Result<IPage<Score>> queryPageList(Score score,
        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<Score> queryWrapper = QueryGenerator.initQueryWrapper(score, req.getParameterMap());
        Page<Score> page = new Page<Score>(pageNo, pageSize);
        IPage<Score> pageList = scoreService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @ApiOperation(value = "????????????-??????????????????", notes = "????????????????????????")
    @GetMapping("/score_stat")
    public Result<ScoreStatVO> getScoreStat(@RequestParam String userId) {
        SysUserBO sysUser = sysUserManager.getById(userId);
        if (null == sysUser) {
            return Result.error("???????????????");
        }
        ScoreStatVO scoreStatVO = new ScoreStatVO();
        scoreStatVO.setUser(sysUser);
        ScoreStatDTO scoreStatDTO = scoreService.getStuScoreStat(userId);
        BeanUtils.copyProperties(scoreStatDTO, scoreStatVO);
        return Result.ok(scoreStatVO);
    }

    /**
     * ??????
     *
     * @param scoreVO
     * @return
     */
    @AutoLog(value = "????????????-??????")
    @ApiOperation(value = "????????????-??????", notes = "????????????-??????")
    @RequiresPermissions("stu:stu_score:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ScoreVO scoreVO) {
        LambdaQueryWrapper<Score> query = new LambdaQueryWrapper<>();
        query.eq(Score::getCourseId, scoreVO.getCourseId());
        query.eq(Score::getStudentId, scoreVO.getStudentId());
        Score scoreExist = scoreService.getOne(query);
        if (scoreExist != null) {
            return Result.error("??????????????????");
        }
        Score score = new Score();
        BeanUtils.copyProperties(scoreVO, score);
        // ??????????????????
        TeachingPlan course = teachingPlanService.getById(scoreExist.getCourseId());
        score.setIsPass(0);
        if (score.getScore() > course.getPassMark()) {
            score.setIsPass(1);
        }
        scoreService.saveMain(score);
        return Result.OK("???????????????");
    }

    /**
     * ??????
     *
     * @param scoreVO
     * @return
     */
    @AutoLog(value = "????????????-??????")
    @ApiOperation(value = "????????????-??????", notes = "????????????-??????")
    @RequiresPermissions("stu:stu_score:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ScoreVO scoreVO) {
        Score score = new Score();
        BeanUtils.copyProperties(scoreVO, score);
        Score scoreEntity = scoreService.getById(score.getId());
        if (scoreEntity == null) {
            return Result.error("?????????????????????");
        }
        // ??????????????????
        if (!scoreEntity.getScore().equals(score.getScore())) {
            TeachingPlan course = teachingPlanService.getById(score.getCourseId());
            score.setIsPass(0);
            if (score.getScore() > course.getPassMark()) {
                score.setIsPass(1);
            }
        }
        scoreService.updateMain(score);
        return Result.OK("????????????!");
    }

    /**
     * ??????id??????
     *
     * @param id
     * @return
     */
    @AutoLog(value = "????????????-??????id??????")
    @ApiOperation(value = "????????????-??????id??????", notes = "????????????-??????id??????")
    @RequiresPermissions("stu:stu_score:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        scoreService.delMain(id);
        return Result.OK("????????????!");
    }

    /**
     * ????????????
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "????????????-????????????")
    @ApiOperation(value = "????????????-????????????", notes = "????????????-????????????")
    @RequiresPermissions("stu:stu_score:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.scoreService.delBatchMain(Arrays.asList(ids.split(",")));
        return Result.OK("?????????????????????");
    }

    /**
     * ??????id??????
     *
     * @param id
     * @return
     */
    // @AutoLog(value = "????????????-??????id??????")
    @ApiOperation(value = "????????????-??????id??????", notes = "????????????-??????id??????")
    @GetMapping(value = "/queryById")
    public Result<Score> queryById(@RequestParam(name = "id", required = true) String id) {
        Score score = scoreService.getById(id);
        if (score == null) {
            return Result.error("?????????????????????");
        }
        return Result.OK(score);

    }

    /**
     * ??????excel
     *
     * @param request
     * @param score
     */
    @RequiresPermissions("stu:stu_score:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Score score) {
        // Step.1 ??????????????????????????????
        QueryWrapper<Score> queryWrapper = QueryGenerator.initQueryWrapper(score, request.getParameterMap());
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();

        // ??????????????????????????????
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            List<String> selectionList = Arrays.asList(selections.split(","));
            queryWrapper.in("id", selectionList);
        }
        // Step.2 ??????????????????
        List<Score> scoreList = scoreService.list(queryWrapper);

        // Step.3 ??????pageList
        List<ScoreVO> pageList = new ArrayList<ScoreVO>();
        for (Score main : scoreList) {
            ScoreVO vo = new ScoreVO();
            BeanUtils.copyProperties(main, vo);
            pageList.add(vo);
        }

        // Step.4 AutoPoi ??????Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "??????????????????");
        mv.addObject(NormalExcelConstants.CLASS, ScoreVO.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("??????????????????", "?????????:" + sysUser.getRealname(), "????????????"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    /**
     * ??????excel????????????
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("stu:stu_score:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // ????????????????????????
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<ScoreVO> list = ExcelImportUtil.importExcel(file.getInputStream(), ScoreVO.class, params);
                for (ScoreVO page : list) {
                    Score po = new Score();
                    BeanUtils.copyProperties(page, po);
                    scoreService.saveMain(po);
                }
                return Result.OK("?????????????????????????????????:" + list.size());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("??????????????????:" + e.getMessage());
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.OK("?????????????????????");
    }

}
