package com.kuaidao.manageweb.controller.statistics.busTeleDistribution;


import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.manageweb.controller.statistics.BaseStatisticsController;
import com.kuaidao.manageweb.feign.statistics.busTeleDistribution.BusTeleDistributionFeignClient;
import com.kuaidao.stastics.dto.base.BaseBusQueryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 商务报表 / 业绩报表 / 电销组织分布表
 */
@Controller
@RequestMapping("/statistics/busTeleDistribution")
public class BusTeleDistributionController extends BaseStatisticsController {

    @Autowired
    private BusTeleDistributionFeignClient busTeleDistributionFeignClient;


    /**
     * 一级页面跳转
     */
    @RequestMapping("/toTeleOrganizationDistributed")
    public String toTeleOrganizationDistributed(HttpServletRequest request) {
        //商务组
        initOrgList(request);
        //商务大区
        initBugOrg(request);
        return "reportBusPerformance/teleOrganizationDistributed";
    }

    /**
     * 二级页面跳转
     */
    @RequestMapping("/toTeleOrganizationDistributedDetail")
    public String toTeleOrganizationDistributedDetail(Long busAreaId,Long businessGroupId,Long startTime,Long endTime,Long businessManagerId,Long groupId,Long projectId,BaseBusQueryDto baseBusQueryDto ,HttpServletRequest request) {
        //商务组
        initOrgList(request);
        //商务大区
        initBugOrg(request);
        return "reportBusPerformance/teleOrganizationDistributedDetail";
    }

    /**
     *
     * 一级页面查询（不分页）
     */
    @RequestMapping("/getOneAllList")
    public JSONResult<Map<String,Object>> getOneAllList(@RequestBody BaseBusQueryDto baseBusQueryDto){
        initAuth(baseBusQueryDto);
        return busTeleDistributionFeignClient.getOneAllList(baseBusQueryDto);
    }
    /**
     *
     * 一级页面查询（分页）
     */
    @ResponseBody
    @RequestMapping("/getOnePageList")
    public JSONResult<Map<String,Object>> getOnePageList(@RequestBody BaseBusQueryDto baseBusQueryDto){
        initAuth(baseBusQueryDto);
        return busTeleDistributionFeignClient.getOnePageList(baseBusQueryDto);
    }

    /**
     *
     * 二级页面查询（不分页）
     */
    @RequestMapping("/getTwoAllList")
    public JSONResult<Map<String,Object>> getTwoAllList(@RequestBody BaseBusQueryDto baseBusQueryDto){
        initAuth(baseBusQueryDto);
        return busTeleDistributionFeignClient.getTwoAllList(baseBusQueryDto);
    }
    /**
     *
     * 二级页面查询（分页）
     */
    @RequestMapping("/getTwoPageList")
    public JSONResult<Map<String,Object>> getTwoPageList(@RequestBody BaseBusQueryDto baseBusQueryDto){
        initAuth(baseBusQueryDto);
        return busTeleDistributionFeignClient.getTwoPageList(baseBusQueryDto);
    }
}
