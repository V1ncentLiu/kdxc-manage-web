/**
 *
 */
package com.kuaidao.manageweb.controller.merchant.user;

import com.kuaidao.businessconfig.dto.project.ProjectInfoDTO;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.constant.SettingConstant;
import com.kuaidao.common.constant.emun.sys.UserTypeEnum;
import com.kuaidao.common.entity.*;
import com.kuaidao.common.util.CommonUtil;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.config.LogRecord.OperationType;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.changeorg.ChangeOrgFeignClient;
import com.kuaidao.manageweb.feign.clue.ClueRelateFeignClient;
import com.kuaidao.manageweb.feign.clue.MyCustomerFeignClient;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.merchant.cert.MerchantCertFeignClient;
import com.kuaidao.manageweb.feign.merchant.clue.MerchantClueInfoFeignClient;
import com.kuaidao.manageweb.feign.merchant.user.MerchantUserInfoFeignClient;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.manageweb.feign.role.RoleManagerFeignClient;
import com.kuaidao.manageweb.feign.user.SysSettingFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.sys.constant.SysConstant;
import com.kuaidao.sys.dto.mechant.cert.MerchantCertReq;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import com.kuaidao.sys.dto.role.RoleQueryDTO;
import com.kuaidao.sys.dto.user.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gpc
 *
 */

@Controller
@RequestMapping("/merchant/userManager")
public class MechantUserController {
    private static Logger logger = LoggerFactory.getLogger(MechantUserController.class);
    @Autowired
    private RoleManagerFeignClient roleManagerFeignClient;

    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    @Autowired
    private OrganizationFeignClient organizationFeignClient;
    @Autowired
    private SysSettingFeignClient sysSettingFeignClient;
    @Autowired
    private DictionaryItemFeignClient dictionaryItemFeignClient;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MyCustomerFeignClient myCustomerFeignClient;
    @Autowired
    private ClueRelateFeignClient clueRelateFeignClient;
    @Autowired
    private ChangeOrgFeignClient changeOrgFeignClient;
    @Autowired
    private MerchantUserInfoFeignClient merchantUserInfoFeignClient;

    @Autowired
    private MerchantClueInfoFeignClient merchantClueInfoFeignClient;

    @Autowired
    private ProjectInfoFeignClient projectInfoFeignClient;

    @Autowired
    private MerchantCertFeignClient merchantCertFeignClient;

    @Value("${oss.url.directUpload}")
    private String ossUrl;

    /***
     * 用户列表页
     *
     * @return
     */
    @RequestMapping("/initUserList")
    @RequiresPermissions("sys:merchantUser:view")
    public String initUserList(HttpServletRequest request) {
        //查询商家端配置的角色
     /*   String roleIds = getSysSetting(SysConstant.MECHANTROLE);
        if(StringUtils.isNotBlank(roleIds)){
            List<String> list = Arrays.asList(roleIds.split(","));
            IdListReq idListReq = new IdListReq();
            idListReq.setIdList(list);
            JSONResult<List<RoleInfoDTO>> roleInfoDTOs = roleManagerFeignClient.qeuryRoleListByRoleIds(idListReq);
            request.setAttribute("roleList", roleInfoDTOs.getData());
        }*/
        //查询商家版账号对应的角色
        RoleQueryDTO sjroleQueryDTO = new RoleQueryDTO();
        sjroleQueryDTO.setRoleCode(RoleCodeEnum.SJZH.name());
        JSONResult<List<RoleInfoDTO>> sjRoleDTOs = roleManagerFeignClient.qeuryRoleByName(sjroleQueryDTO);
        if (sjRoleDTOs != null && JSONResult.SUCCESS.equals(sjRoleDTOs.getCode())) {
            request.setAttribute("roleList", sjRoleDTOs.getData());
        }
        OrganizationQueryDTO reqDto = new OrganizationQueryDTO();
        reqDto.setSource(SysConstant.MERCHANT_ORG_SOURCE_TWO);
        // 查询组织机构树
        JSONResult<List<TreeData>> treeJsonRes = organizationFeignClient.queryList(reqDto);
        // 查询组织机构树
        if (treeJsonRes != null && JSONResult.SUCCESS.equals(treeJsonRes.getCode()) && treeJsonRes.getData() != null) {
            request.setAttribute("orgData", treeJsonRes.getData());
        } else {
            logger.error("query organization tree,res{{}}", treeJsonRes);
        }
        //查询商家版子账号对应的角色
        RoleQueryDTO roleQueryDTO = new RoleQueryDTO();
        roleQueryDTO.setRoleCode(RoleCodeEnum.SJZZH.name());
        JSONResult<List<RoleInfoDTO>> subaccountRoleDTOs = roleManagerFeignClient.qeuryRoleByName(roleQueryDTO);
        if (subaccountRoleDTOs != null && JSONResult.SUCCESS.equals(subaccountRoleDTOs.getCode())) {
            request.setAttribute("subaccountRoleDTOs", subaccountRoleDTOs.getData());
        }
        IdListLongReq idListLongReq = new IdListLongReq();
        idListLongReq.setIdList(ListUtils.emptyIfNull(getMerchantUser()).stream().map(UserInfoDTO::getId).collect(Collectors.toList()));
        JSONResult<List<ProjectInfoDTO>> listJSONResult = projectInfoFeignClient.queryListByGroupId(idListLongReq);
        if(JSONResult.SUCCESS.equals(listJSONResult.getCode())){
            request.setAttribute("projectList", listJSONResult.getData());

        }
        //查询项目信息
        request.setAttribute("ossUrl",ossUrl);
        return "merchant/user/userManagePage";
    }
    /***
     * 用户列表
     *
     * @return
     */
    @PostMapping("/merchantlist")
    @ResponseBody
    public JSONResult<PageBean<UserInfoDTO>> merchantlist(@RequestBody UserInfoPageParam userInfoPageParam, HttpServletRequest request,
                                                          HttpServletResponse response) {
        if(userInfoPageParam.getProjectId()!=null){
            JSONResult<ProjectInfoDTO> projectInfoDTOJSONResult = projectInfoFeignClient.get(new IdEntityLong(userInfoPageParam.getProjectId()));
            if(JSONResult.SUCCESS.equals(projectInfoDTOJSONResult.getCode()) && projectInfoDTOJSONResult.getData()!=null){
                String groupId = projectInfoDTOJSONResult.getData().getGroupId();
                userInfoPageParam.setId(Long.parseLong(groupId));
            }
        }
        JSONResult<PageBean<UserInfoDTO>> list = merchantUserInfoFeignClient.merchantlist(userInfoPageParam);
        if(userInfoPageParam.getParentId()==null && list.getCode().equals(JSONResult.SUCCESS) && CollectionUtils.isNotEmpty(list.getData().getData())){
            List<UserInfoDTO> data = list.getData().getData();
            List<Long> userIds = ListUtils.emptyIfNull(data).stream().map(UserInfoDTO::getId).collect(Collectors.toList());
            IdListLongReq idListLongReq = new IdListLongReq();
            idListLongReq.setIdList(userIds);
            JSONResult<Map<Long, Integer>> userBalanceStatus = merchantClueInfoFeignClient.getUserBalanceStatus(idListLongReq);
            JSONResult<List<ProjectInfoDTO>> listJSONResult = projectInfoFeignClient.queryListByGroupId(idListLongReq);
            List<ProjectInfoDTO> projectInfoDTOList = new ArrayList<>();
            if(listJSONResult.getCode().equals(JSONResult.SUCCESS)) {
                projectInfoDTOList =listJSONResult.data();
            }
            Map<String, String> projectMap = ListUtils.emptyIfNull(projectInfoDTOList).stream().
                    collect(Collectors.
                            groupingBy(ProjectInfoDTO::getGroupId,
                                    Collectors.mapping(ProjectInfoDTO::getProjectName,
                                            Collectors.joining("、"))));
            if(userBalanceStatus.getCode().equals(JSONResult.SUCCESS)){
                Map<Long, Integer> map = userBalanceStatus.getData();
                for (UserInfoDTO userInfoDTO : data) {
                    userInfoDTO.setBalanceStatus(null);
                    if(map.containsKey(userInfoDTO.getId())){
                        userInfoDTO.setBalanceStatus(map.get(userInfoDTO.getId()));
                    }
                    if(projectMap.containsKey(userInfoDTO.getId()+"")){
                        userInfoDTO.setProjectName(projectMap.get(userInfoDTO.getId()+""));
                    }
                }
            }
        }
        return list;
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
        if (byCode != null && JSONResult.SUCCESS.equals(byCode.getCode()) && byCode.getData()!=null) {
            return byCode.getData().getValue();
        }
        return null;
    }

    /**
     * 保存用户
     *
     * @param
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @PostMapping("/saveUser")
    @ResponseBody
    @RequiresPermissions("sys:merchantUser:add")
    @LogRecord(description = "新增商家账号", operationType = OperationType.INSERT, menuName = MenuEnum.MERCHANT_USER_MANAGEMENT)
    public JSONResult saveUser(@Valid @RequestBody UserInfoReq userInfoReq, BindingResult result) {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        if(userInfoReq.getUserType().equals(UserTypeEnum.商家子账号.getType())&& userInfoReq.getSmsStatus()==1){
            userInfoReq.setMerchantType(SysConstant.MerchantType.TYPE2);
            String merchantUserMsgCount = getSysSetting(SettingConstant.MERCHANT_USER_MSG_COUNT);
            Long count = 2L;
            try {
                if(StringUtils.isNotBlank(merchantUserMsgCount)){
                    count = Long.parseLong(merchantUserMsgCount);
                }
            } catch (NumberFormatException e) {
              logger.error("saveUser merchantUserMsgCount{},e:{}",merchantUserMsgCount,e);
            }
            //查询现在有的

            if(  count<=getMerchantSmsCount(userInfoReq.getParentId())  ){
                return new JSONResult().fail("-1","接收新客户短信提醒账号已达到上限"+count+"!");
            }
        }

        return merchantUserInfoFeignClient.create(userInfoReq);
    }

    private Long getMerchantSmsCount(Long parentId) {

        //查询现在有的
        IdEntityLong idEntityLong = new IdEntityLong();
        idEntityLong.setId(parentId);
        JSONResult<Long> jsonResult =  merchantUserInfoFeignClient.getMerchantSmsCount(idEntityLong);
        return jsonResult.getData();
    }

    /**
     * 查询用户根据id
     *
     * @param
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @PostMapping("/getMechantUserById")
    @ResponseBody
    public JSONResult<UserInfoReq> getMechantUserById(@RequestBody UserInfoReq userInfoReq) {
        return merchantUserInfoFeignClient.getMechantUserById(userInfoReq);
    }

    /**
     * 保存用户
     *
     * @param
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @PostMapping("/updateUser")
    @ResponseBody
    @RequiresPermissions("sys:merchantUser:edit")
    @LogRecord(description = "新增商家账号", operationType = OperationType.UPDATE, menuName = MenuEnum.MERCHANT_USER_MANAGEMENT)
    public JSONResult updateUser(@Valid @RequestBody UserInfoReq userInfoReq, BindingResult result) {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        if( userInfoReq.getSmsStatus().intValue()==1){
            UserInfoReq param= new UserInfoReq();
            param.setId(userInfoReq.getId());
            JSONResult<UserInfoReq> jsonResult = merchantUserInfoFeignClient.getUserInfo(param);
            if(JSONResult.SUCCESS.equals(jsonResult.getCode()) && jsonResult.getData()!=null){
                if(jsonResult.getData().getSmsStatus()!=null &&  jsonResult.getData().getSmsStatus().intValue()==0 ) {
                    String merchantUserMsgCount = getSysSetting(SettingConstant.MERCHANT_USER_MSG_COUNT);
                    Long count = 2L;
                    try {
                        if(StringUtils.isNotBlank(merchantUserMsgCount)){
                            count = Long.parseLong(merchantUserMsgCount);
                        }
                    } catch (NumberFormatException e) {
                        logger.error("saveUser merchantUserMsgCount{},e:{}",merchantUserMsgCount,e);
                    }
                    //查询现在有的
                    Long parentId= userInfoReq.getParentId();
                    if(userInfoReq.getUserType().intValue()==SysConstant.USER_TYPE_TWO.intValue()){
                        parentId=userInfoReq.getId();
                    }
                    if(count<=getMerchantSmsCount(parentId)){
                        return new JSONResult().fail("-1","接收新客户短信提醒账号已达到上限"+count+"!");
                    }
                }
            }
        }

        return merchantUserInfoFeignClient.updateUser(userInfoReq);
    }

    /***
     * 子账号列表
     *
     * @return
     */
    @RequestMapping("/subaccountUserPage")
    public String subaccountUserPage(HttpServletRequest request) {
        String parentId = request.getParameter("parentId");
        String name = request.getParameter("name");
        String orgId = request.getParameter("orgId");
        getSysSetting("mechantRole");
        //查询商家端配置的角色
        String roleIds = getSysSetting(SysConstant.MECHANTROLE);
        if(StringUtils.isNotBlank(roleIds)){
            List<String> list = Arrays.asList(roleIds.split(","));
            IdListReq idListReq = new IdListReq();
            idListReq.setIdList(list);
            JSONResult<List<RoleInfoDTO>> roleInfoDTOs = roleManagerFeignClient.qeuryRoleListByRoleIds(idListReq);
            request.setAttribute("roleList", roleInfoDTOs.getData());
        }
        //查询商家版子账号对应的角色
        RoleQueryDTO roleQueryDTO = new RoleQueryDTO();
        roleQueryDTO.setRoleCode(RoleCodeEnum.SJZZH.name());
        JSONResult<List<RoleInfoDTO>> subaccountRoleDTOs = roleManagerFeignClient.qeuryRoleByName(roleQueryDTO);
        if (subaccountRoleDTOs != null && JSONResult.SUCCESS.equals(subaccountRoleDTOs.getCode())) {
            request.setAttribute("subaccountRoleDTOs", subaccountRoleDTOs.getData());
        }
        IdEntityLong idEntityLong = new IdEntityLong();
        idEntityLong.setId(Long.parseLong(orgId));
        JSONResult<List<TreeData>> accountDataTrees = organizationFeignClient.listOrgTreeDataByParentId(idEntityLong);
        if (accountDataTrees != null && JSONResult.SUCCESS.equals(accountDataTrees.getCode())) {
            request.setAttribute("accountDataTrees", accountDataTrees.getData());
        }
        request.setAttribute("parentId", parentId);
        request.setAttribute("name", name);
        request.setAttribute("orgId", orgId);
        return "merchant/user/subaccountUserPage";
    }


    /**
     * 查询组织机构树
     *
     * @return
     */
    @PostMapping("/getAccountOrg")
    @ResponseBody
    public JSONResult<List<TreeData>> getAccountOrg(@RequestBody UserInfoReq userInfoReq) {
        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();

        RoleInfoDTO roleInfoDTO = curLoginUser.getRoleList().get(0);
        String roleCode = roleInfoDTO.getRoleCode();
        IdEntityLong idEntityLong = new IdEntityLong();
        idEntityLong.setId(userInfoReq.getOrgId());
        return organizationFeignClient.listOrgTreeDataByParentId(idEntityLong);
    }

    /***
     * 用户信息页
     *
     * @return
     */
    @RequestMapping("/userInfo")
    public String userInfo(HttpServletRequest request) {
        Subject subject = SecurityUtils.getSubject();
        UserInfoDTO user = (UserInfoDTO) subject.getSession().getAttribute("user");
        Long id = user.getId();
        request.setAttribute("ossUrl", ossUrl);
        request.setAttribute("userId", String.valueOf(id));
        return "merchant/merchantInfo/merchantInfo";
    }

    /**
     * 修改用户头像
     */
    @PostMapping("/updateIcon")
    @ResponseBody
    public JSONResult updateIcon(@RequestBody UserInfoReq userInfoReq) {
        if (null == userInfoReq.getId()) {
            return CommonUtil.getParamIllegalJSONResult();
        }
        //如果提交头像为空，直接返回
        if(StringUtils.isBlank(userInfoReq.getMerchantIcon())){
            return new JSONResult().success(null);
        }
        JSONResult<String> jsonResult = userInfoFeignClient.update(userInfoReq);
        return jsonResult;
    }

    /**
     * @description: 修改用户上传状态
     * @author fengyixuan
     * @date 2021/7/13 11:28 上午
     * @param merchantCertReq
     * @returns com.kuaidao.common.entity.JSONResult
    */
    @PostMapping("/updateAuditStatus")
    @ResponseBody
    public JSONResult<String> updateAuditStatus(@RequestBody MerchantCertReq merchantCertReq) {
        if (null == merchantCertReq.getUserId() || merchantCertReq.getAuditStatus()==null) {
            return CommonUtil.getParamIllegalJSONResult();
        }
        JSONResult<String> jsonResult = merchantCertFeignClient.updateAuditStatus(merchantCertReq);
        return jsonResult;
    }

    /**
     * 查询商家账号
     *
     * @return
     */
    private List<UserInfoDTO> getMerchantUser() {
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserType(SysConstant.USER_TYPE_TWO);
        JSONResult<List<UserInfoDTO>> merchantUserList =
                merchantUserInfoFeignClient.merchantUserList(userInfoDTO);
        return merchantUserList.getData();
    }

    /**
     *  新增修改证件信息
     */
    @RequestMapping(value = "/addOrUpdate")
    @ResponseBody
    public JSONResult<Boolean> addOrUpdate(@RequestBody MerchantCertReq merchantCertReq) {
        JSONResult<Boolean> jsonResult = merchantCertFeignClient.addOrUpdate(merchantCertReq);
        return jsonResult;
    }
}
