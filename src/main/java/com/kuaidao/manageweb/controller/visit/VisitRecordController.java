package com.kuaidao.manageweb.controller.visit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.github.pagehelper.PageHelper;
import com.kuaidao.aggregation.dto.busmycustomer.RejectSignOrderReqDTO;
import com.kuaidao.aggregation.dto.project.ProjectInfoDTO;
import com.kuaidao.aggregation.dto.project.ProjectInfoPageParam;
import com.kuaidao.aggregation.dto.visitrecord.RejectVisitRecordReqDTO;
import com.kuaidao.aggregation.dto.visitrecord.VisitRecordReqDTO;
import com.kuaidao.aggregation.dto.visitrecord.VisitRecordRespDTO;
import com.kuaidao.common.constant.OrgTypeConstant;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.constant.SystemCodeConstant;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.common.util.CommonUtil;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.area.SysRegionFeignClient;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.manageweb.feign.visit.TrackingOrderFeignClient;
import com.kuaidao.manageweb.feign.visit.VisitRecordFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.sys.dto.area.SysRegionDTO;
import com.kuaidao.sys.dto.organization.OrganizationDTO;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.organization.OrganizationRespDTO;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import com.kuaidao.sys.dto.user.UserOrgRoleReq;

/**
 *  訪問記錄
 * @author  Chen
 * @date 2019年3月4日 下午1:40:44   
 * @version V1.0
 */
@Controller
@RequestMapping("/visit/visitRecord")
public class VisitRecordController {
    
    private static Logger logger = LoggerFactory.getLogger(VisitRecordController.class);
    
    @Autowired
    TrackingOrderFeignClient trackingOrderFeignClient;
    
    @Autowired
    UserInfoFeignClient userInfoFeignClient;

    @Autowired
    ProjectInfoFeignClient projectInfoFeignClient;
    
    @Autowired
    OrganizationFeignClient organizationFeignClient;
    
    @Autowired
    SysRegionFeignClient sysRegionFeignClient;
    
    @Autowired
    VisitRecordFeignClient visitRecordFeignClient;

    
    @RequiresPermissions("aggregation:visitRecord:view")
    @RequestMapping("/visitRecordPage")
    public String visitRecordPage(HttpServletRequest request) {
        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
        Long orgId = curLoginUser.getOrgId();
        //签约项目
        List<ProjectInfoDTO> projectList = getProjectList();
        //商务小组
        List<OrganizationDTO> businessGroupList = getBusinessGroupList(orgId, OrgTypeConstant.SWZ);
        //商务经理        
        List<UserInfoDTO> busManagerList = getUserInfo(orgId,RoleCodeEnum.SWJL.name());
        //签约省份
        SysRegionDTO sysRegionDTO = new SysRegionDTO();
        sysRegionDTO.setType(1);;
        JSONResult<List<SysRegionDTO>> proviceJr = sysRegionFeignClient.querySysRegionByParam(sysRegionDTO);
        //公司
        //List<OrganizationDTO> companyList = getBusinessGroupList(orgId, OrgTypeConstant.ZSZX);
        OrganizationQueryDTO companyDto = new OrganizationQueryDTO();
        companyDto.setOrgType(OrgTypeConstant.ZSZX);
        JSONResult<List<OrganizationRespDTO>> companyJr = organizationFeignClient.queryOrgByParam(companyDto);
        
        request.setAttribute("projectList",projectList);
        request.setAttribute("busManagerList",busManagerList);
        request.setAttribute("businessGroupList",businessGroupList);
        request.setAttribute("proviceList", proviceJr.getData());
        request.setAttribute("companyList", companyJr.getData());
        
        return "visit/customerVisitRecord";
    }
    
    /**
     * 获取所有的项目
     * @return
     */
    private List<ProjectInfoDTO> getProjectList(){
        JSONResult<List<ProjectInfoDTO>> projectInfoListJr = projectInfoFeignClient.listNoPage(new ProjectInfoPageParam());
        if(projectInfoListJr==null || !JSONResult.SUCCESS.equals(projectInfoListJr.getCode())) {
            logger.error("signrecord ,get projectList ,res{{}}",projectInfoListJr);
            return null;
        }
        return projectInfoListJr.getData();
    }
    
    
    private List<UserInfoDTO> getUserInfo(Long orgId,String roleName){
        UserOrgRoleReq req = new UserOrgRoleReq();
        if(orgId!=null) {
            req.setOrgId(orgId);
        }
        req.setRoleCode(roleName);
        JSONResult<List<UserInfoDTO>> userJr = userInfoFeignClient.listByOrgAndRole(req);
        if(userJr==null || !JSONResult.SUCCESS.equals(userJr.getCode())) {
            logger.error("查询电销通话记录-获取组内顾问-userInfoFeignClient.listByOrgAndRole(req),param{{}},res{{}}",req,userJr);
            return null;
        } 
        return userJr.getData();
    }
    
    /**
     * 获取商务组
     * @param orgId
     * @param orgType
     * @return
     */
    private List<OrganizationDTO> getBusinessGroupList(Long orgId,Integer orgType){
        OrganizationQueryDTO busGroupReqDTO = new OrganizationQueryDTO();
        busGroupReqDTO.setSystemCode(SystemCodeConstant.HUI_JU);
        busGroupReqDTO.setParentId(orgId);
        busGroupReqDTO.setOrgType(orgType);
        JSONResult<List<OrganizationDTO>> orgJr = organizationFeignClient.listDescenDantByParentId(busGroupReqDTO);
        if(orgJr==null || !JSONResult.SUCCESS.equals(orgJr.getCode())) {
            logger.error("query org list res{{}}",orgJr);
            return null;
        }
        return orgJr.getData();
        
    }
    
    /**
     * 获取所有的 组
     * @param orgType
     * @return
     */
    private List<OrganizationRespDTO> getTeleGroupList(Integer orgType){
        OrganizationQueryDTO busGroupReqDTO = new OrganizationQueryDTO();
        busGroupReqDTO.setSystemCode(SystemCodeConstant.HUI_JU);
        busGroupReqDTO.setOrgType(orgType);
        JSONResult<List<OrganizationRespDTO>> orgJr = organizationFeignClient.queryOrgByParam(busGroupReqDTO);
        if(orgJr==null || !JSONResult.SUCCESS.equals(orgJr.getCode())) {
            logger.error("query org list res{{}}",orgJr);
            return null;
        }
        return orgJr.getData();
    }
    
    /**
     * 查询 客户到访记录
     * @param visitRecordReqDTO
     * @return
     */
    @RequiresPermissions("aggregation:visitRecord:view")
    @PostMapping("/listVisitRecord")
    @ResponseBody
    public JSONResult<PageBean<VisitRecordRespDTO>> listVisitRecord(@RequestBody VisitRecordReqDTO visitRecordReqDTO){
        handleReqParam(visitRecordReqDTO);
        
        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
        Long orgId = curLoginUser.getOrgId();
        List<RoleInfoDTO> roleList = curLoginUser.getRoleList();
        RoleInfoDTO roleInfoDTO = roleList.get(0);
        String roleName = roleInfoDTO.getRoleName();
        if(RoleCodeEnum.SWDQZJ.value().equals(roleName) ||RoleCodeEnum.SWZJ.value().equals(roleName)) {
            Long busManagerId = visitRecordReqDTO.getBusManagerId();
            if(busManagerId==null) {
                List<Long> accountIdList =  getAccountIdList(orgId,RoleCodeEnum.SWJL.name());
                visitRecordReqDTO.setBusManagerIdList(accountIdList);
            }else {
                List<Long> busManagerIdList = new ArrayList<>();
                busManagerIdList.add(busManagerId);
                visitRecordReqDTO.setBusManagerIdList(busManagerIdList);
            }
           
        }else {
            return new JSONResult().fail(SysErrorCodeEnum.ERR_NOTEXISTS_DATA.getCode(),"角色没有权限");
        }
        
       return visitRecordFeignClient.listVisitRecord(visitRecordReqDTO);
    }
    
    /**
     * 获取当前组织机构下  角色信息
     * @param orgId
     * @param roleCode
     * @return
     */
    private List<Long> getAccountIdList(Long orgId,String roleCode){
        UserOrgRoleReq req = new UserOrgRoleReq();
        req.setOrgId(orgId);
        req.setRoleCode(roleCode);
        JSONResult<List<UserInfoDTO>> userJr = userInfoFeignClient.listByOrgAndRole(req);
        if(userJr==null || !JSONResult.SUCCESS.equals(userJr.getCode())) {
            logger.error("查询电销通话记录-获取组内顾问-userInfoFeignClient.listByOrgAndRole(req),param{{}},res{{}}",req,userJr);
            return null;
        }
        List<UserInfoDTO> userInfoDTOList = userJr.getData();
        if(userInfoDTOList!=null && userInfoDTOList.size()!=0) {
            List<Long> idList = userInfoDTOList.stream().map(UserInfoDTO::getId).collect(Collectors.toList());
           return idList;
        }
        return null;
    }
    
    private void handleReqParam(VisitRecordReqDTO visitRecordReqDTO) {
        Long projectId = visitRecordReqDTO.getProjectId();
        if(projectId!=null) {
            List<Long> projectIdList = new ArrayList<Long>();
            projectIdList.add(projectId);
            visitRecordReqDTO.setProjectIdList(projectIdList);
        }
        Long busGroupId = visitRecordReqDTO.getBusGroupId();
        if(busGroupId!=null) {
            List<Long> busGroupIdList = new ArrayList<>();
            busGroupIdList.add(busGroupId);
            visitRecordReqDTO.setBusGroupIdList(busGroupIdList);
        }
        Long companyId = visitRecordReqDTO.getCompanyId();
        if(companyId!=null) {
            List<Long> companyIdList = new ArrayList<>();
            companyIdList.add(companyId);
            visitRecordReqDTO.setCompanyIdList(companyIdList);
        }
        
    }

    /**
     * 驳回签约单
     * @return
     */
    @RequiresPermissions("aggregation:visitRecord:reject")
    @PostMapping("/rejectVisitRecord")
    @LogRecord(description = "来访记录驳回",operationType = LogRecord.OperationType.UPDATE,menuName = MenuEnum.CUSTOMER_VISIT_RECORD)
    @ResponseBody
    public JSONResult<Boolean> rejectVisitRecord(@Valid @RequestBody RejectVisitRecordReqDTO reqDTO,BindingResult result){
        if(result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
         reqDTO.setStatus(0);
         return visitRecordFeignClient.rejectVisitRecord(reqDTO);
    }
    
    
    /**
     * 审核通过 签约单
     * @return
     */
    @RequiresPermissions("aggregation:visitRecord:pass")
    @PostMapping("/passAuditSignOrder")
    @LogRecord(description = "来访记录审核通过",operationType = LogRecord.OperationType.UPDATE,menuName = MenuEnum.CUSTOMER_VISIT_RECORD)
    @ResponseBody
    public JSONResult<Boolean> passAuditSignOrder(@Valid @RequestBody RejectVisitRecordReqDTO reqDTO,BindingResult result){
        if(result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        reqDTO.setStatus(2);
        
        return visitRecordFeignClient.rejectVisitRecord(reqDTO);
    }
}