/**
 *
 */
package com.kuaidao.manageweb.controller.rule;

import com.kuaidao.aggregation.constant.AggregationConstant;
import com.kuaidao.businessconfig.constant.BusinessConfigConstant;
import com.kuaidao.businessconfig.dto.project.ProjectInfoDTO;
import com.kuaidao.businessconfig.dto.project.ProjectInfoPageParam;
import com.kuaidao.businessconfig.dto.rule.*;
import com.kuaidao.common.constant.DicCodeEnum;
import com.kuaidao.common.constant.OrgTypeConstant;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.constant.SystemCodeConstant;
import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.IdListLongReq;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.common.util.CommonUtil;
import com.kuaidao.common.util.DateUtil;
import com.kuaidao.common.util.ExcelUtil;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.config.LogRecord.OperationType;
import com.kuaidao.manageweb.constant.Constants;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.manageweb.feign.rule.ClueAssignRuleFeignClient;
import com.kuaidao.manageweb.feign.rule.ClueAutoAssignRuleFeignClient;
import com.kuaidao.manageweb.feign.user.SysSettingFeignClient;
import com.kuaidao.sys.constant.SysConstant;
import com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO;
import com.kuaidao.sys.dto.organization.OrganizationDTO;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.organization.OrganizationRespDTO;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import com.kuaidao.sys.dto.user.SysSettingDTO;
import com.kuaidao.sys.dto.user.SysSettingReq;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zxy
 *
 */

@Controller
@RequestMapping("/clueAutoAssignRule/optAutoRule")
public class OptAutoRuleController {
    private static Logger logger = LoggerFactory.getLogger(OptAutoRuleController.class);
    @Autowired
    private ClueAutoAssignRuleFeignClient clueAutoAssignRuleFeignClient;
    @Autowired
    private OrganizationFeignClient organizationFeignClient;
    @Autowired
    private DictionaryItemFeignClient dictionaryItemFeignClient;
    @Autowired
    private SysSettingFeignClient sysSettingFeignClient;
    @Autowired
    private ProjectInfoFeignClient projectInfoFeignClient;

    /***
     * 优化规则列表页
     *
     * @return
     */
    @RequestMapping("/initRuleList")
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:view")
    public String initCompanyList(HttpServletRequest request) {
        UserInfoDTO user = getUser();
        // 查询优化类资源类别集合
        request.setAttribute("clueCategoryList", getOptCategory());
        // 查询字典行业类别集合
        request.setAttribute("industryCategoryList",
                getDictionaryByCode(Constants.INDUSTRY_CATEGORY));
        // 查询字典媒介集合
        request.setAttribute("mediumList", getDictionaryByCode(Constants.MEDIUM));
        // 当前人员id
        request.setAttribute("userId", user.getId() + "");
        // 当前人员角色code
        List<RoleInfoDTO> roleList = user.getRoleList();
        if (roleList != null && roleList.size() != 0) {
            request.setAttribute("roleCode", roleList.get(0).getRoleCode());
        }
        OrganizationQueryDTO orgDto = new OrganizationQueryDTO();
        orgDto.setOrgType(OrgTypeConstant.DXZ);
        orgDto.setSystemCode(SystemCodeConstant.HUI_JU);
        // 电销小组
        JSONResult<List<OrganizationRespDTO>> dzList =
                organizationFeignClient.queryOrgByParam(orgDto);
        List<OrganizationRespDTO> data = dzList.getData();
        request.setAttribute("queryOrg", data);
        return "rule/optAutoRuleManagerPage";
    }

    /***
     * 新增优化规则页
     *
     * @return
     */
    @RequestMapping("/initCreate")
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:add")
    public String initCreateProject(HttpServletRequest request) {
        // 查询话务组
        List<OrganizationRespDTO> orgList = getTrafficGroup();
        Collections.sort(orgList,
                Comparator.comparing(OrganizationRespDTO::getCreateTime).reversed());
        request.setAttribute("trafficList", orgList);
        /*JSONResult<List<OrganizationDTO>> listBusinessLineOrg =
                organizationFeignClient.listBusinessLineOrg();*/
        // 查询所有业务线
        request.setAttribute("businessLineList", getDictionaryByCode(DicCodeEnum.BUSINESS_LINE.getCode()));

        // 查询优化类资源类别集合
        request.setAttribute("clueCategoryList", getOptCategory());
        // 查询字典行业类别集合
        request.setAttribute("industryCategoryList",
                getDictionaryByCode(Constants.INDUSTRY_CATEGORY));
        // 查询字典媒介集合
        request.setAttribute("mediumList", getDictionaryByCode(Constants.MEDIUM));
        // 项目
        ProjectInfoPageParam param = new ProjectInfoPageParam();
        param.setIsNotSign(-1);
        JSONResult<List<ProjectInfoDTO>> proJson = projectInfoFeignClient.listNoPage(param);
        if (proJson.getCode().equals(JSONResult.SUCCESS)) {
            request.setAttribute("noSingProjectList", proJson.getData());
        }
        return "rule/addOptAutoRulePage";
    }

    /***
     * 编辑优化规则页
     *
     * @return
     */
    @RequestMapping("/initUpdate")
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:edit")
    public String initUpdate(@RequestParam Long id, HttpServletRequest request) {
        // 查询优化规则信息
        JSONResult<ClueAutoAssignRuleDTO> jsonResult =
                clueAutoAssignRuleFeignClient.get(new IdEntityLong(id));
        ClueAutoAssignRuleDTO data = jsonResult.getData();
        List<OrganizationRespDTO> teleGroupList = new ArrayList<>();
        if (data != null && data.getAutoAssignRuleTeamDetailList() != null) {
            List<AutoAssignRuleTeamDetailDTO> teleList = data.getAutoAssignRuleTeamDetailList();
            List<Integer> businessLineList = new ArrayList<>();
            for (AutoAssignRuleTeamDetailDTO assignRuleTeamDTO : teleList) {
                if(assignRuleTeamDTO.getBusinessLine() !=null){
                    businessLineList.add(assignRuleTeamDTO.getBusinessLine());
                }
            }
            if(CollectionUtils.isNotEmpty(businessLineList)){
                OrganizationQueryDTO queryDTO = new OrganizationQueryDTO();
                queryDTO.setSystemCode(SystemCodeConstant.HUI_JU);
                queryDTO.setOrgType(OrgTypeConstant.DXZ);
                queryDTO.setBusinessLineList(businessLineList);
                JSONResult<List<OrganizationRespDTO>> orgList =
                        organizationFeignClient.queryOrgByParam(queryDTO);
                if(JSONResult.SUCCESS.equals(orgList.getCode()) && CollectionUtils.isNotEmpty(orgList.getData())){
                    teleGroupList = orgList.getData();
                }
            }
        }
        request.setAttribute("teleGroupList", teleGroupList);
        request.setAttribute("clueAssignRule", data);
        // 查询话务组
        List<OrganizationRespDTO> orgList = getTrafficGroup();
        Collections.sort(orgList,
                Comparator.comparing(OrganizationRespDTO::getCreateTime).reversed());
        request.setAttribute("trafficList", orgList);
//        JSONResult<List<OrganizationDTO>> listBusinessLineOrg =
//                organizationFeignClient.listBusinessLineOrg();
        // 查询所有业务线
        request.setAttribute("businessLineList", getDictionaryByCode(DicCodeEnum.BUSINESS_LINE.getCode()));
        // 查询优化类资源类别集合
        request.setAttribute("clueCategoryList", getOptCategory());
        // 查询字典行业类别集合
        request.setAttribute("industryCategoryList",
                getDictionaryByCode(Constants.INDUSTRY_CATEGORY));
        // 查询字典媒介集合
        request.setAttribute("mediumList", getDictionaryByCode(Constants.MEDIUM));
        // 项目
        ProjectInfoPageParam param = new ProjectInfoPageParam();
        param.setIsNotSign(-1);
        JSONResult<List<ProjectInfoDTO>> proJson = projectInfoFeignClient.listNoPage(param);
        if (proJson.getCode().equals(JSONResult.SUCCESS)) {
            request.setAttribute("noSingProjectList", proJson.getData());
        }
        return "rule/updateOptAutoRulePage";
    }

    /***
     * 优化规则列表
     *
     * @return
     */
    @PostMapping("/list")
    @ResponseBody
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:view")
    public JSONResult<PageBean<ClueAutoAssignRuleDTO>> list(
            @RequestBody ClueAutoAssignRulePageParam pageParam, HttpServletRequest request) {
        UserInfoDTO user = getUser();
        // 插入当前用户、角色信息
        pageParam.setUserId(user.getId());
        pageParam.setOrgId(user.getOrgId());
        // 推广所属公司 为当前账号所在机构的推广所属公司
        pageParam.setPromotionCompany(user.getPromotionCompany());
        List<RoleInfoDTO> roleList = user.getRoleList();
        if (roleList != null) {
            pageParam.setRoleCode(roleList.get(0).getRoleCode());
        }
        // 优化规则
        pageParam.setRuleType(BusinessConfigConstant.RULE_TYPE.OPT);
        JSONResult<PageBean<ClueAutoAssignRuleDTO>> list = clueAutoAssignRuleFeignClient.list(pageParam);

        return list;
    }


    /**
     * 保存优化规则
     *
     * @param
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @PostMapping("/save")
    @ResponseBody
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:add")
    @LogRecord(description = "新增自动优化规则", operationType = OperationType.INSERT,
            menuName = MenuEnum.OPT_AUTO_RULE_MANAGEMENT)
    public JSONResult save(@Valid @RequestBody ClueAutoAssignRuleReq clueAssignRuleReq,
            BindingResult result) {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        // 插入创建人信息
        UserInfoDTO user = getUser();
        clueAssignRuleReq.setCreateUser(user.getId());
        clueAssignRuleReq.setUpdateUser(user.getId());
        clueAssignRuleReq.setOrgId(user.getOrgId());
        // 推广所属公司 为当前账号所在机构的推广所属公司
        clueAssignRuleReq.setPromotionCompany(user.getPromotionCompany());
        // 插入类型为优化
        clueAssignRuleReq.setRuleType(BusinessConfigConstant.RULE_TYPE.OPT);
        return clueAutoAssignRuleFeignClient.create(clueAssignRuleReq);
    }

    /**
     * 修改优化规则
     *
     * @param
     * @return
     */
    @PostMapping("/update")
    @ResponseBody
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:edit")
    @LogRecord(description = "修改优化规则信息", operationType = OperationType.UPDATE,
            menuName = MenuEnum.OPT_AUTO_RULE_MANAGEMENT)
    public JSONResult update(@Valid @RequestBody ClueAutoAssignRuleReq clueAssignRuleReq,
            BindingResult result) {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        // 插入修改人信息
        UserInfoDTO user = getUser();
        clueAssignRuleReq.setUpdateUser(user.getId());
        Long id = clueAssignRuleReq.getId();
        if (id == null) {
            return new JSONResult().fail(SysErrorCodeEnum.ERR_ILLEGAL_PARAM.getCode(),
                    SysErrorCodeEnum.ERR_ILLEGAL_PARAM.getMessage());
        }
        clueAssignRuleReq.setRuleType(AggregationConstant.RULE_TYPE.OPT);
        return clueAutoAssignRuleFeignClient.update(clueAssignRuleReq);
    }

    /**
     * 启用规则
     *
     * @param
     * @return
     */
    @PostMapping("/updateStatusEnable")
    @ResponseBody
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:edit")
    @LogRecord(description = "启用优化规则", operationType = OperationType.ENABLE,
            menuName = MenuEnum.OPT_AUTO_RULE_MANAGEMENT)
    public JSONResult updateStatusEnable(@Valid @RequestBody ClueAutoAssignRuleReq clueAssignRuleReq,
            BindingResult result) {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }

        Long id = clueAssignRuleReq.getId();
        if (id == null) {
            return new JSONResult().fail(SysErrorCodeEnum.ERR_ILLEGAL_PARAM.getCode(),
                    SysErrorCodeEnum.ERR_ILLEGAL_PARAM.getMessage());
        }
        return clueAutoAssignRuleFeignClient.updateStatus(clueAssignRuleReq);
    }

    /**
     * 禁用规则
     *
     * @param
     * @return
     */
    @PostMapping("/updateStatusDisable")
    @ResponseBody
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:edit")
    @LogRecord(description = "禁用优化规则", operationType = OperationType.DISABLE,
            menuName = MenuEnum.OPT_AUTO_RULE_MANAGEMENT)
    public JSONResult updateStatusDisable(@Valid @RequestBody ClueAutoAssignRuleReq clueAssignRuleReq,
            BindingResult result) {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        Long id = clueAssignRuleReq.getId();
        if (id == null) {
            return new JSONResult().fail(SysErrorCodeEnum.ERR_ILLEGAL_PARAM.getCode(),
                    SysErrorCodeEnum.ERR_ILLEGAL_PARAM.getMessage());
        }
        return clueAutoAssignRuleFeignClient.updateStatus(clueAssignRuleReq);
    }


    /**
     * 删除优化规则
     *
     * @param
     * @return
     */
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:delete")
    @LogRecord(description = "删除规则", operationType = OperationType.DELETE,
            menuName = MenuEnum.OPT_AUTO_RULE_MANAGEMENT)
    public JSONResult delete(@RequestBody IdListLongReq idList) {
        return clueAutoAssignRuleFeignClient.delete(idList);
    }

    /**
     * 复制规则
     *
     * @param
     * @return
     */
    @PostMapping("/copy")
    @ResponseBody
    @LogRecord(description = "复制规则", operationType = OperationType.DELETE,
            menuName = MenuEnum.OPT_AUTO_RULE_MANAGEMENT)
    public JSONResult copy(@RequestBody ClueAutoAssignRuleReq clueAssignRuleReq) {
        UserInfoDTO user = getUser();
        clueAssignRuleReq.setCreateUser(user.getId());
        return clueAutoAssignRuleFeignClient.copy(clueAssignRuleReq);
    }

    /**
     * 导出
     *
     * @param
     * @return
     */
    @RequiresPermissions("clueAutoAssignRule:optAutoRuleManager:export")
    @PostMapping("/export")
    @LogRecord(description = "优化规则导出", operationType = OperationType.EXPORT,
            menuName = MenuEnum.OPT_AUTO_RULE_MANAGEMENT)
    public void export(@RequestBody ClueAutoAssignRulePageParam pageParam, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        logger.debug("list param{}", pageParam);
        UserInfoDTO user = getUser();
        // 插入当前用户、角色信息
        pageParam.setUserId(user.getId());
        pageParam.setOrgId(user.getOrgId());

        List<RoleInfoDTO> roleList = user.getRoleList();
        if (roleList != null) {
            pageParam.setRoleCode(roleList.get(0).getRoleCode());
        }
        // 优化规则
        pageParam.setRuleType(BusinessConfigConstant.RULE_TYPE.OPT);
        // 查询规则数据不分页
        JSONResult<List<ClueAutoAssignRuleDTO>> listNoPage =
                clueAutoAssignRuleFeignClient.listNoPage(pageParam);
        List<List<Object>> dataList = new ArrayList<List<Object>>();
        dataList.add(getHeadTitleList());

        if (JSONResult.SUCCESS.equals(listNoPage.getCode()) && listNoPage.getData() != null
                && listNoPage.getData().size() != 0) {

            List<ClueAutoAssignRuleDTO> resultList = listNoPage.getData();
            int size = resultList.size();

            for (int i = 0; i < size; i++) {
                ClueAutoAssignRuleDTO ClueAutoAssignRuleDTO = resultList.get(i);
                List<Object> curList = new ArrayList<>();
                curList.add(i + 1);
                curList.add(ClueAutoAssignRuleDTO.getRuleName());
                curList.add(ClueAutoAssignRuleDTO.getSourceName());
                curList.add(ClueAutoAssignRuleDTO.getCategoryName());
                curList.add(ClueAutoAssignRuleDTO.getIndustryCategoryName());
                curList.add(ClueAutoAssignRuleDTO.getSearchWord());
                curList.add(ClueAutoAssignRuleDTO.getNotSearchWord());
                curList.add(ClueAutoAssignRuleDTO.getProvince());
                curList.add(ClueAutoAssignRuleDTO.getNotProvince());
                curList.add(getTimeStr(ClueAutoAssignRuleDTO.getUpdateTime()));
                curList.add(getTimeStr(ClueAutoAssignRuleDTO.getCreateTime()));
                curList.add(getTimeStr(ClueAutoAssignRuleDTO.getStartTime()));
                curList.add(getTimeStr(ClueAutoAssignRuleDTO.getEndTime()));
                curList.add(ClueAutoAssignRuleDTO.getCreateUserName());
                curList.add(ClueAutoAssignRuleDTO.getUpdateUserName());
                curList.add(ClueAutoAssignRuleDTO.getTeamName());
                curList.add(ClueAutoAssignRuleDTO.getStatusName());
                dataList.add(curList);
            }

        } else {
            logger.error("export rule_report res{{}}", listNoPage);
        }
        XSSFWorkbook workBook = new XSSFWorkbook();// 创建一个工作薄
        XSSFSheet sheet = workBook.createSheet();// 创建一个工作薄对象sheet

        sheet.setColumnWidth(1, 8000);// 设置第二列的宽度为
        sheet.setColumnWidth(5, 6000);// 设置第二列的宽度为
        sheet.setColumnWidth(6, 6000);// 设置第二列的宽度为
        sheet.setColumnWidth(7, 6000);// 设置第二列的宽度为
        sheet.setColumnWidth(8, 6000);// 设置第二列的宽度为
        sheet.setColumnWidth(9, 6000);// 设置第二列的宽度为
        sheet.setColumnWidth(10, 6000);// 设置第二列的宽度为
        sheet.setColumnWidth(11, 6000);// 设置第二列的宽度为
        sheet.setColumnWidth(12, 6000);// 设置第二列的宽度为
        sheet.setColumnWidth(14, 4000);// 设置第二列的宽度为
        sheet.setColumnWidth(15, 4000);// 设置第二列的宽度为
        XSSFWorkbook wbWorkbook = ExcelUtil.creat2007ExcelWorkbook(workBook, dataList);


        String name = "自动优化规则" + DateUtil.convert2String(new Date(), DateUtil.ymd) + ".xlsx";
        response.addHeader("Content-Disposition",
                "attachment;filename=" + new String(name.getBytes("UTF-8"), "ISO8859-1"));
        response.addHeader("fileName", URLEncoder.encode(name, "utf-8"));
        response.setContentType("application/octet-stream");
        try (ServletOutputStream outputStream = response.getOutputStream();) {

            wbWorkbook.write(outputStream);
        } catch (Exception e) {
            logger.error("导出异常{}", e);
        }

    }

    private List<Object> getHeadTitleList() {
        List<Object> headTitleList = new ArrayList<>();
        headTitleList.add("序号");
        headTitleList.add("规则名称");
        headTitleList.add("媒介");
        headTitleList.add("资源类别");
        headTitleList.add("行业类别");
        headTitleList.add("包含搜索词");
        headTitleList.add("不包含搜索词");
        headTitleList.add("包含省份");
        headTitleList.add("不包含省份");
        headTitleList.add("最后编辑时间");
        headTitleList.add("创建时间");
        headTitleList.add("规则有效开始时间");
        headTitleList.add("规则有效结束时间");
        headTitleList.add("创建人");
        headTitleList.add("最后编辑人");
        headTitleList.add("分配电销组");
        headTitleList.add("状态");
        return headTitleList;
    }

    private String getTimeStr(Date date) {
        if (date == null) {
            return "";
        }
        return DateUtil.convert2String(date, DateUtil.ymdhms);
    }

    /**
     * 获取当前登录账号
     *
     * @param
     * @return
     */
    private UserInfoDTO getUser() {
        Object attribute = SecurityUtils.getSubject().getSession().getAttribute("user");
        UserInfoDTO user = (UserInfoDTO) attribute;
        return user;
    }

    /**
     * 查询系统参数
     *
     * @param code
     * @return
     */
    private String getSysSetting(String code) {
        SysSettingReq sysSettingReq = new SysSettingReq();
        sysSettingReq.setCode(code);
        JSONResult<SysSettingDTO> byCode = sysSettingFeignClient.getByCode(sysSettingReq);
        if (byCode != null && JSONResult.SUCCESS.equals(byCode.getCode())) {
            return byCode.getData().getValue();
        }
        return null;
    }

    /***
     * 查询话务组的集合
     *
     * @return
     */
    private List<OrganizationRespDTO> getTrafficGroup() {
        OrganizationQueryDTO organizationQueryDTO = new OrganizationQueryDTO();
        organizationQueryDTO.setOrgType(OrgTypeConstant.HWZ);
        JSONResult<List<OrganizationRespDTO>> trafficResult =
                organizationFeignClient.queryOrgByParam(organizationQueryDTO);
        return trafficResult.getData();
    }

    /**
     * 查询系统参数优化资源类别
     *
     * @param
     * @return
     */
    private List<DictionaryItemRespDTO> getOptCategory() {
        // 系统参数优化资源类别
        String reminderTime = getSysSetting(SysConstant.OPT_CATEGORY);
        List<DictionaryItemRespDTO> dictionaryByCode = getDictionaryByCode(Constants.CLUE_CATEGORY);
        List<DictionaryItemRespDTO> notOptCategory = new ArrayList<DictionaryItemRespDTO>();
        if (StringUtils.isNotBlank(reminderTime) && dictionaryByCode != null) {
            String[] split = reminderTime.split(",");
            for (DictionaryItemRespDTO dictionaryItemRespDTO : dictionaryByCode) {
                for (int i = 0; i < split.length; i++) {
                    if (split[i].equals(dictionaryItemRespDTO.getValue())) {
                        notOptCategory.add(dictionaryItemRespDTO);
                        continue;
                    }
                }
            }
        }
        return notOptCategory;
    }

    /**
     * 查询字典表
     *
     * @param code
     * @return
     */
    private List<DictionaryItemRespDTO> getDictionaryByCode(String code) {
        JSONResult<List<DictionaryItemRespDTO>> queryDicItemsByGroupCode =
                dictionaryItemFeignClient.queryDicItemsByGroupCode(code);
        if (queryDicItemsByGroupCode != null
                && JSONResult.SUCCESS.equals(queryDicItemsByGroupCode.getCode())) {
            return queryDicItemsByGroupCode.getData();
        }
        return null;
    }

}
