/**
 * 
 */
package com.kuaidao.manageweb.controller.im;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kuaidao.common.entity.IdEntity;
import com.kuaidao.sys.dto.organization.OrganizationDTO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kuaidao.aggregation.constant.AggregationConstant;
import com.kuaidao.businessconfig.dto.project.ProjectInfoDTO;
import com.kuaidao.businessconfig.dto.project.ProjectInfoPageParam;
import com.kuaidao.common.constant.BusinessLineConstant;
import com.kuaidao.common.constant.OrgTypeConstant;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.common.util.CommonUtil;
import com.kuaidao.custservice.constant.CustServiceConstant;
import com.kuaidao.custservice.dto.saleim.SaleImDTO;
import com.kuaidao.custservice.dto.saleim.SaleImPageParam;
import com.kuaidao.custservice.dto.saleim.SaleImReq;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.config.LogRecord.OperationType;
import com.kuaidao.manageweb.constant.Constants;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.im.SaleImFeignClient;
import com.kuaidao.manageweb.feign.merchant.user.MerchantUserInfoFeignClient;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.sys.constant.SysConstant;
import com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.organization.OrganizationRespDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import com.kuaidao.sys.dto.user.UserOrgRoleReq;

/**
 * @author zxy
 */

@Controller
@RequestMapping("/custservice/saleIm")
public class SaleImController {
    private static Logger logger = LoggerFactory.getLogger(SaleImController.class);
    @Autowired
    private SaleImFeignClient saleImFeignClient;
    @Autowired
    private ProjectInfoFeignClient projectInfoFeignClient;
    @Autowired
    private DictionaryItemFeignClient dictionaryItemFeignClient;
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    @Autowired
    private OrganizationFeignClient organizationFeignClient;
    @Autowired
    private MerchantUserInfoFeignClient merchantUserInfoFeignClient;

    /***
     * 顾问Im授权列表页
     * @return
     */
    @RequestMapping("/initListPage")
    @RequiresPermissions("custservice:saleIm:view")
    public String initListPage(HttpServletRequest request) {
        UserInfoDTO user = getUser();
        String roleCode = user.getRoleList().get(0).getRoleCode();
        List<Integer> businessLineList = new ArrayList<>();
//        businessLineList.add(BusinessLineConstant.SHANGJI);
        businessLineList.add(BusinessLineConstant.CMZSJJ);
        // 餐盟严选所有电销组
        OrganizationQueryDTO organizationQueryDTO = new OrganizationQueryDTO();
        organizationQueryDTO.setBusinessLineList(businessLineList);
        organizationQueryDTO.setOrgType(OrgTypeConstant.DXZ);
        if (RoleCodeEnum.JJJL.name().equals(roleCode)) {
            organizationQueryDTO.setId(user.getOrgId());
        }
        // 查询下级电销组(查询使用)
        JSONResult<List<OrganizationRespDTO>> listDescenDantByParentId = organizationFeignClient.queryOrgByParam(organizationQueryDTO);
        //经纪组
        List<OrganizationRespDTO> data = listDescenDantByParentId.getData();
        // 餐盟严选所有电销顾问
        List<UserInfoDTO> userList = new ArrayList<>();

        userList = getUserListByBusliness(BusinessLineConstant.CMZSJJ, RoleCodeEnum.JMJJ.name(), user);


        OrganizationQueryDTO organizationQueryDTO1 = new OrganizationQueryDTO();
        organizationQueryDTO1.setBusinessLineList(businessLineList);
        organizationQueryDTO1.setOrgType(OrgTypeConstant.HWZ);
        if (RoleCodeEnum.JMGWZG.name().equals(roleCode)) {
            organizationQueryDTO1.setId(user.getOrgId());
        }
        // 查询下级电销组(查询使用)
        JSONResult<List<OrganizationRespDTO>> listDescenDantByParentId1 = organizationFeignClient.queryOrgByParam(organizationQueryDTO1);
        //经纪顾问
        List<OrganizationRespDTO> data1 = listDescenDantByParentId1.getData();

        request.setAttribute("teleGroupList", data);
        request.setAttribute("teleSaleList", userList);
        request.setAttribute("roleCode", roleCode);
        request.setAttribute("orgId", user.getOrgId() + "");

        request.setAttribute("gwGroupList", data1);

        return "im/imAccreditManagement";
    }


    /***
     * @return
     */
    @PostMapping("/getSaleList")
    @ResponseBody
    public JSONResult<List<UserInfoDTO>> getSaleList(@RequestBody UserOrgRoleReq userOrgRoleReq,HttpServletRequest request) {
        JSONResult<List<UserInfoDTO>> listByOrgAndRole = userInfoFeignClient.listByOrgAndRole(userOrgRoleReq);
        return listByOrgAndRole;
    }


    /***
     * 顾问Im授权列表页
     * @return
     */
    @RequestMapping("/initUpdatePage")
    @RequiresPermissions("custservice:saleIm:add")
    public String initUpdatePage(@RequestParam(required = false) Long teleSaleId, HttpServletRequest request) {
        ProjectInfoPageParam pageParam = new ProjectInfoPageParam();
        JSONResult<List<ProjectInfoDTO>> allBrandList = getAllProjectList(pageParam);
        // 所有品牌列表
        request.setAttribute("allBrandList", allBrandList.getData());
        if (teleSaleId != null) {
            JSONResult<SaleImDTO> byTeleSaleId = saleImFeignClient.getByTeleSaleId(new IdEntityLong(teleSaleId));
            request.setAttribute("saleIm", byTeleSaleId.getData());
            if (allBrandList.getData() != null && byTeleSaleId.getData().getBrandIdList() != null) {
                Map<Long, ProjectInfoDTO> allBrandMap = allBrandList.getData().stream()
                        .collect(Collectors.toMap(ProjectInfoDTO::getId, a -> a, (k1, k2) -> k1));
                List<Long> brandIdList = byTeleSaleId.getData().getBrandIdList();
                List<ProjectInfoDTO> brandList = new ArrayList<ProjectInfoDTO>();
                for (Long brandId : brandIdList) {
                    ProjectInfoDTO projectInfoDTO = allBrandMap.get(brandId);
                    if (projectInfoDTO != null) {
                        brandList.add(projectInfoDTO);
                    }
                }
                request.setAttribute("brandList", brandList);
            }
        }
        // 查询字典品类集合
        request.setAttribute("categoryList", getDictionaryByCode(Constants.PROJECT_CATEGORY));
        // 获取商家账号集合，倒叙
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserType(SysConstant.USER_TYPE_TWO);
        userInfoDTO.setStatusList(null);
        List<UserInfoDTO> userInfoList = getMerchantUser(userInfoDTO);
        request.setAttribute("userInfoList", userInfoList);

        return "im/imAccreditSitting";
    }

    private JSONResult<List<ProjectInfoDTO>> getAllProjectList(ProjectInfoPageParam pageParam) {
        List<String> list = new ArrayList<String>();
        list.add(CustServiceConstant.PROJECT_ATTRIBUTIVE_CMYXDJ);
        list.add(CustServiceConstant.PROJECT_ATTRIBUTIVE_CMYXFDJ);
        pageParam.setProjectAttributiveList(list);
        pageParam.setIsNotSign(AggregationConstant.NO);
        JSONResult<List<ProjectInfoDTO>> projectList = projectInfoFeignClient.listNoPage(pageParam);
        return projectList;
    }

    /***
     * 顾问Im授权列表
     * @return
     */
    @PostMapping("/list")
    @ResponseBody
    public JSONResult<PageBean<SaleImDTO>> list(@RequestBody SaleImPageParam pageParam, HttpServletRequest request) {
        List<Integer> businessLineList = new ArrayList<>();
//        businessLineList.add(BusinessLineConstant.SHANGJI);
        businessLineList.add(BusinessLineConstant.CMZSJJ);
        pageParam.setBusinessLineList(businessLineList);
        List<String> roleCodeList = new ArrayList<>();
        roleCodeList.add(RoleCodeEnum.JMJJ.name());
        roleCodeList.add(RoleCodeEnum.JMGW.name());
        pageParam.setRoleCodeList(roleCodeList);
        JSONResult<PageBean<SaleImDTO>> list = saleImFeignClient.list(pageParam);

        return list;
    }

    /***
     * 查询品牌列表
     * @return
     */
    @PostMapping("/getBrandList")
    @ResponseBody
    public JSONResult<List<ProjectInfoDTO>> list(@RequestBody ProjectInfoPageParam pageParam, HttpServletRequest request) {
        List<String> list = new ArrayList<String>();
        list.add(CustServiceConstant.PROJECT_ATTRIBUTIVE_CMYXDJ);
        list.add(CustServiceConstant.PROJECT_ATTRIBUTIVE_CMYXFDJ);
        pageParam.setProjectAttributiveList(list);
        pageParam.setIsNotSign(AggregationConstant.NO);
        JSONResult<List<ProjectInfoDTO>> projectList = projectInfoFeignClient.listNoPage(pageParam);
        return projectList;
    }

    /**
     * 保存顾问Im授权
     * @param orgDTO
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @PostMapping("/save")
    @ResponseBody
    @RequiresPermissions("custservice:saleIm:add")
    @LogRecord(description = "顾问Im授权保存", operationType = OperationType.INSERT, menuName = MenuEnum.SALE_IM)
    public JSONResult save(@Valid @RequestBody SaleImReq saleImReq, BindingResult result) {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        long userId = getUserId();
        if (saleImReq.getId() == null) {
            saleImReq.setCreateUser(userId);
        } else {
            saleImReq.setUpdateUser(userId);
        }
        return saleImFeignClient.save(saleImReq);
    }

    /**
     * 解绑顾问Im授权
     * @param orgDTO
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @PostMapping("/untie")
    @ResponseBody
    @RequiresPermissions("custservice:saleIm:edit")
    @LogRecord(description = "解绑Im授权", operationType = OperationType.UPDATE, menuName = MenuEnum.SALE_IM)
    public JSONResult untie(@Valid @RequestBody SaleImReq saleImReq, BindingResult result) {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        return saleImFeignClient.untie(saleImReq);
    }

    /**
     * 获取当前登录账号ID
     * @param orgDTO
     * @return
     */
    private long getUserId() {
        Object attribute = SecurityUtils.getSubject().getSession().getAttribute("user");
        UserInfoDTO user = (UserInfoDTO) attribute;
        return user.getId();
    }

    /**
     * 获取当前登录账号
     * @param orgDTO
     * @return
     */
    private UserInfoDTO getUser() {
        Object attribute = SecurityUtils.getSubject().getSession().getAttribute("user");
        UserInfoDTO user = (UserInfoDTO) attribute;
        return user;
    }

    /**
     * 查询字典表
     * @param code
     * @return
     */
    private List<DictionaryItemRespDTO> getDictionaryByCode(String code) {
        JSONResult<List<DictionaryItemRespDTO>> queryDicItemsByGroupCode = dictionaryItemFeignClient.queryDicItemsByGroupCode(code);
        if (queryDicItemsByGroupCode != null && JSONResult.SUCCESS.equals(queryDicItemsByGroupCode.getCode())) {
            return queryDicItemsByGroupCode.getData();
        }
        return null;
    }

    /**
     * @Description 查询商家账号
     * @param userInfoDTO
     * @Return java.util.List<com.kuaidao.sys.dto.user.UserInfoDTO>
     * @Author xuyunfeng
     * @Date 2019/10/15 17:19
     **/
    private List<UserInfoDTO> getMerchantUser(UserInfoDTO userInfoDTO) {
        JSONResult<List<UserInfoDTO>> merchantUserList = merchantUserInfoFeignClient.merchantUserList(userInfoDTO);
        List<UserInfoDTO> userInfoDTOList = merchantUserList.getData();
        if (userInfoDTOList != null & userInfoDTOList.size() > 0) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            userInfoDTOList.sort((a1, a2) -> {
                try {
                    return df.parse(sdf.format(a2.getCreateTime())).compareTo(df.parse(sdf.format(a1.getCreateTime())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 1;
            });
        }
        return userInfoDTOList;
    }

    /**
     * 根据机构和角色类型获取用户
     * @param user
     * @return
     */
    private List<UserInfoDTO> getUserListByBusliness(Integer businessLine, String roleCode, UserInfoDTO user) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(SysConstant.USER_STATUS_ENABLE);
        statusList.add(SysConstant.USER_STATUS_LOCK);
        UserOrgRoleReq userOrgRoleReq = new UserOrgRoleReq();
        userOrgRoleReq.setRoleCode(roleCode);
        userOrgRoleReq.setStatusList(statusList);
        userOrgRoleReq.setBusinessLine(businessLine);
        if (RoleCodeEnum.JJJL.name().equals(user.getRoleList().get(0).getRoleCode())) {
            userOrgRoleReq.setOrgId(user.getOrgId());
        }
        JSONResult<List<UserInfoDTO>> listByOrgAndRole = userInfoFeignClient.listByOrgAndRole(userOrgRoleReq);
        return listByOrgAndRole.getData();
    }
}
