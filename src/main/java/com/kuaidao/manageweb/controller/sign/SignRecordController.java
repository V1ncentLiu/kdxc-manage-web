package com.kuaidao.manageweb.controller.sign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.kuaidao.aggregation.constant.AggregationConstant;
import com.kuaidao.aggregation.dto.busmycustomer.RejectSignOrderReqDTO;
import com.kuaidao.aggregation.dto.busmycustomer.SignRecordReqDTO;
import com.kuaidao.aggregation.dto.busmycustomer.SignRecordRespDTO;
import com.kuaidao.aggregation.dto.project.ProjectInfoDTO;
import com.kuaidao.aggregation.dto.project.ProjectInfoPageParam;
import com.kuaidao.aggregation.dto.sign.PayDetailDTO;
import com.kuaidao.common.constant.OrgTypeConstant;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.constant.SystemCodeConstant;
import com.kuaidao.common.entity.IdListLongReq;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.common.util.CommonUtil;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.manageweb.feign.sign.SignRecordFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.sys.dto.organization.OrganizationDTO;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.organization.OrganizationRespDTO;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import com.kuaidao.sys.dto.user.UserOrgRoleReq;

/**
 * 签约记录
 * @author  Chen
 * @date 2019年3月1日 下午6:29:43   
 * @version V1.0
 */
@RequestMapping("/sign/signRecord")
@Controller
public class SignRecordController {
    
    private static Logger logger = LoggerFactory.getLogger(SignRecordController.class);

    @Autowired
    SignRecordFeignClient signRecordFeignClient;
    
    @Autowired
    OrganizationFeignClient organizationFeignClient;
    
    @Autowired
    UserInfoFeignClient userInfoFeignClient;
    
    @Autowired
    ProjectInfoFeignClient projectInfoFeignClient;

    
    /**
     * 签约记录 页面
     * @return
     */
    @RequiresPermissions("aggregation:signRecord:view")
    @RequestMapping("/signRecordPage")
    public String signRecordPage(HttpServletRequest request) {

        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
        Long orgId = curLoginUser.getOrgId();
        //签约项目
        List<ProjectInfoDTO> projectList = getProjectList();
        //商务小组
        List<OrganizationDTO> businessGroupList = getBusinessGroupList(orgId, OrgTypeConstant.SWZ);
        //电销组 
        List<OrganizationRespDTO> teleGroupList =getTeleGroupList(OrgTypeConstant.DXZ);
        //商务经理        
        List<UserInfoDTO> busManagerList = getUserInfo(orgId,RoleCodeEnum.SWJL.name());
        //电销人员
        List<UserInfoDTO> teleSaleList = getUserInfo(null, RoleCodeEnum.DXCYGW.name());
        
        request.setAttribute("projectList",projectList);
        request.setAttribute("busManagerList",busManagerList);
        request.setAttribute("businessGroupList",businessGroupList);
        request.setAttribute("teleGroupList",teleGroupList);
        request.setAttribute("teleSaleList",teleSaleList);

        return "signrecord/signRecord";
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
     * 签约记录 分页
     * @param reqDTO
     * @return
     */
    @PostMapping("/listSignRecord")
    @ResponseBody
    @RequiresPermissions("aggregation:signRecord:view")
    public JSONResult<PageBean<SignRecordRespDTO>> listSignRecord(@RequestBody SignRecordReqDTO reqDTO) {
        handleReqParam(reqDTO);
        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
        Long orgId = curLoginUser.getOrgId();
        List<RoleInfoDTO> roleList = curLoginUser.getRoleList();
        RoleInfoDTO roleInfoDTO = roleList.get(0);
        String roleName = roleInfoDTO.getRoleName();
      if(RoleCodeEnum.SWDQZJ.value().equals(roleName) ||RoleCodeEnum.SWZJ.value().equals(roleName)) {
            Long businessManagerId = reqDTO.getBusinessManagerId();
            if(businessManagerId!=null) {
                List<Long> businessManagerIdList = new ArrayList<>();
                businessManagerIdList.add(businessManagerId);
                reqDTO.setBusinessManagerIdList(businessManagerIdList);
            }else {
                List<Long> accountIdList =  getAccountIdList(orgId,RoleCodeEnum.SWJL.name());
                reqDTO.setBusinessManagerIdList(accountIdList);
            }
          
        }else {
                return new JSONResult().fail(SysErrorCodeEnum.ERR_NOTEXISTS_DATA.getCode(),"角色没有权限");
        }
       logger.info("listSignRecord{{}}",reqDTO.toString());
        return signRecordFeignClient.listSignRecord(reqDTO);
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
    
    private void handleReqParam(SignRecordReqDTO reqDTO) {
        Long signProjectId = reqDTO.getSignProjectId();
        if(signProjectId!=null) {
            List<Long>  signProjectIdList = new ArrayList<Long>();
            signProjectIdList.add(signProjectId);
            reqDTO.setSignProjectIdList(signProjectIdList);
        }
        
        Long businessGroupId = reqDTO.getBusinessGroupId();
        if(businessGroupId!=null) {
            List<Long>  businessGroupIdList = new ArrayList<Long>();
            businessGroupIdList.add(businessGroupId);  
            reqDTO.setBusinessGroupIdList(businessGroupIdList);
        }
        
        
        Long teleGroupid = reqDTO.getTeleGroupid();
        if(teleGroupid!=null) {
            List<Long> teleGroupidList  = new ArrayList<>();
            teleGroupidList.add(teleGroupid);
            reqDTO.setTeleGroupidList(teleGroupidList);
        }
        Long teleSaleId = reqDTO.getTeleSaleId();
        if(teleSaleId!=null) {
            List<Long> teleSaleIdList = new ArrayList<>();
            teleSaleIdList.add(teleSaleId);
            reqDTO.setTeleSaleIdList(teleSaleIdList);
        }
        
    }

    /**
     * 驳回签约单
     * @return
     */
    @PostMapping("/rejectSignOrder")
    @RequiresPermissions("aggregation:signRecord:reject")
    @LogRecord(description = "签约单驳回",operationType = LogRecord.OperationType.UPDATE,menuName = MenuEnum.SIGN_ORDER)
    @ResponseBody
    public JSONResult<Boolean> rejectSignOrder(@Valid @RequestBody RejectSignOrderReqDTO reqDTO,BindingResult result){
        if(result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        reqDTO.setStatus(AggregationConstant.SIGN_ORDER_STATUS.REJECT);
        return signRecordFeignClient.rejectSignOrder(reqDTO);
    }

    /**
     * 审核通过 签约单
     * @return
     */
    @PostMapping("/passAuditSignOrder")
    @RequiresPermissions("aggregation:signRecord:pass")
    @LogRecord(description = "签约单审核通过",operationType = LogRecord.OperationType.UPDATE,menuName = MenuEnum.SIGN_ORDER)
    @ResponseBody
    public JSONResult<Boolean> passAuditSignOrder(@Valid @RequestBody RejectSignOrderReqDTO reqDTO,BindingResult result){
        if(result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        reqDTO.setStatus(AggregationConstant.SIGN_ORDER_STATUS.PASS);
        
        return signRecordFeignClient.rejectSignOrder(reqDTO);
    }
    
    /**
     * 根据sign_Id 查询 付款明细
     * @param idListLongReq
     * @return
     */
    @PostMapping("/listPayDetailNoPage")
    @ResponseBody
    public JSONResult listPayDetailNoPage(@RequestBody IdListLongReq idListLongReq){
        JSONResult<List<PayDetailDTO>>  payDetailListJr = signRecordFeignClient.listPayDetailNoPage(idListLongReq);
        if(payDetailListJr==null || !JSONResult.SUCCESS.equals(payDetailListJr.getCode())) {
            return payDetailListJr;
        }
        List<PayDetailDTO> data = payDetailListJr.getData();
        if(data==null) {
            return payDetailListJr;
        }

        Map<String, List<PayDetailDTO>> payDetailMap = data.stream().collect(Collectors.groupingBy(PayDetailDTO::getPayType,Collectors.toList()));
        return  new JSONResult().success(payDetailMap);
    }
    
        
}
