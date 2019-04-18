package com.kuaidao.manageweb.controller.pubcustomer;

import com.kuaidao.aggregation.dto.pubcusres.ClueQueryParamDTO;
import com.kuaidao.aggregation.dto.pubcusres.PublicCustomerResourcesReqDTO;
import com.kuaidao.aggregation.dto.pubcusres.PublicCustomerResourcesRespDTO;
import com.kuaidao.common.constant.DicCodeEnum;
import com.kuaidao.common.constant.OrgTypeConstant;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.constant.SystemCodeConstant;
import com.kuaidao.common.entity.IdEntity;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.constant.Constants;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.customfield.CustomFieldFeignClient;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.publiccustomer.PublicCustomerFeignClient;
import com.kuaidao.manageweb.feign.role.RoleManagerFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.sys.dto.customfield.CustomFieldQueryDTO;
import com.kuaidao.sys.dto.customfield.QueryFieldByRoleAndMenuReq;
import com.kuaidao.sys.dto.customfield.QueryFieldByUserAndMenuReq;
import com.kuaidao.sys.dto.customfield.UserFieldDTO;
import com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO;
import com.kuaidao.sys.dto.organization.OrganizationDTO;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.organization.OrganizationRespDTO;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import com.kuaidao.sys.dto.role.RoleQueryDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import com.kuaidao.sys.dto.user.UserInfoPageParam;
import com.kuaidao.sys.dto.user.UserOrgRoleReq;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author  yangbiao
 * @Date: 2019/2/11 15:08
 * @Description:
 *      公共客户资源
 */
@Controller
@RequestMapping("/aggregation/publiccustomer")
public class PublicCustomerResources {

    private static Logger logger = LoggerFactory.getLogger(PublicCustomerResources.class);

    @Autowired
    private  DictionaryItemFeignClient dictionaryItemFeignClient;

    @Autowired
    private OrganizationFeignClient organizationFeignClient;

    @Autowired
    UserInfoFeignClient userInfoFeignClient;

    @Autowired
    RoleManagerFeignClient roleManagerFeignClient;

    @Autowired
    PublicCustomerFeignClient publicCustomerFeignClient;

    @Autowired
    private CustomFieldFeignClient customFieldFeignClient;


    /**
     * 分配资源
     */
    @RequiresPermissions("PublicCustomer:allocation")
    @LogRecord(description = "公共客户资源-分配资源",operationType = LogRecord.OperationType.DISTRIBUTION,menuName = MenuEnum.TEL_CENTER_PUBLICCUSTOMER)
    @PostMapping("/allocationResource")
    @ResponseBody
    public JSONResult<Boolean>   allocationResource(@RequestBody PublicCustomerResourcesReqDTO dto){
        Long id = CommUtil.getCurLoginUser().getId();
        dto.setUpdateTime(new Date());
        dto.setUpdateUserId(id);
        return publicCustomerFeignClient.allocationResource(dto);
    }

    /**
     * 转移资源
     */
    @LogRecord(description = "公共客户资源-转移资源",operationType = LogRecord.OperationType.TRANSFER,menuName = MenuEnum.TEL_CENTER_PUBLICCUSTOMER)
    @RequiresPermissions("PublicCustomer:trans")
    @PostMapping("/transferOfResource")
    @ResponseBody
    public JSONResult<Boolean>   transferOfResource(@RequestBody PublicCustomerResourcesReqDTO dto){
        Long id = CommUtil.getCurLoginUser().getId();
        dto.setUpdateTime(new Date());
        dto.setUpdateUserId(id);
        return publicCustomerFeignClient.transferOfResource(dto);
    }

    /**
     * 释放记录
     */
    @PostMapping("/releaseRecord")
    @ResponseBody
    public JSONResult<PageBean> releaseRecord(@RequestBody PublicCustomerResourcesReqDTO dto){
        return null;
    }

    /**
     * 资源还原
     */
    @PostMapping("/resourceReduction")
    @ResponseBody
    public JSONResult<Boolean> resourceReduction(@RequestBody PublicCustomerResourcesReqDTO dto){
        Long id = CommUtil.getCurLoginUser().getId();
        dto.setUpdateTime(new Date());
        dto.setUpdateUserId(id);
        return publicCustomerFeignClient.resourceReduction(dto);
    }
    /**
     * 分页查询
     */
    @RequestMapping("/listPage")
    public String listPage(HttpServletRequest request){
        logger.info("------------ 公共客户资源列表 ---------------");
        //电销组
        List dxzList = new ArrayList();
        List<Long> dxzIdsList = new ArrayList();
        List dxcygwList = new ArrayList();
//        List dxzjsList = new ArrayList();  // 页面上没有电销总监，故而去掉电销总监

        long startTime2=System.currentTimeMillis();
        // 权限相关
        UserInfoDTO user =  CommUtil.getCurLoginUser();
        List<RoleInfoDTO> roleList = user.getRoleList();
        if(roleList!=null&&roleList.get(0)!=null) {
            //修改成共有池后，对数据全选没有限制
            OrganizationQueryDTO dto = new OrganizationQueryDTO();
            dto.setSystemCode(SystemCodeConstant.HUI_JU);
            dto.setOrgType(OrgTypeConstant.DXZ);
            JSONResult<List<OrganizationRespDTO>> dzList = organizationFeignClient.queryOrgByParam(dto);
            long endTime2=System.currentTimeMillis();
            System.out.println("组织机构： "+(endTime2-startTime2)+"ms");
            dxzList = dzList.getData();
            if(dzList!=null&&dzList.getData()!=null){
                for(OrganizationRespDTO organizationRespDTO:dzList.getData()){
                    dxzIdsList.add(organizationRespDTO.getId());
                }
            }
//            dxcygwList = dxcygws(dxzIdsList); // 查询电销创业顾问
//            dxzjsList = dxzjs(dxzIdsList);    // 查询电销总监
        }
        long endTime2=System.currentTimeMillis();
        System.out.println("电销组： "+(endTime2-startTime2)+"ms");
        // 查询字典释放原因集合
        request.setAttribute("releaseReasonList", getDictionaryByCode(Constants.RELEASE_REASON));
        request.setAttribute("dzList", dxzList);
        request.setAttribute("dxgwList",dxcygwList);
//        request.setAttribute("dxzjList", dxzjsList);


        long startTime=System.currentTimeMillis();
//      公共列：
        // 根据角色查询页面字段
        QueryFieldByRoleAndMenuReq queryFieldByRoleAndMenuReq = new QueryFieldByRoleAndMenuReq();
        queryFieldByRoleAndMenuReq.setMenuCode("PublicCustomer");
        queryFieldByRoleAndMenuReq.setId(user.getRoleList().get(0).getId());
        JSONResult<List<CustomFieldQueryDTO>> queryFieldByRoleAndMenu =
                customFieldFeignClient.queryFieldByRoleAndMenu(queryFieldByRoleAndMenuReq);
        request.setAttribute("fieldList", queryFieldByRoleAndMenu.getData());
        // 根据用户查询页面字段
        QueryFieldByUserAndMenuReq queryFieldByUserAndMenuReq = new QueryFieldByUserAndMenuReq();
        queryFieldByUserAndMenuReq.setId(user.getId());
        queryFieldByUserAndMenuReq.setMenuCode("PublicCustomer");
        JSONResult<List<UserFieldDTO>> queryFieldByUserAndMenu =
                customFieldFeignClient.queryFieldByUserAndMenu(queryFieldByUserAndMenuReq);
        request.setAttribute("userFieldList", queryFieldByUserAndMenu.getData());
        long endTime=System.currentTimeMillis();
        System.out.println("公共列： "+(endTime-startTime)+"ms");

        long startTime1=System.currentTimeMillis();
        // 查询字典话务一级客户状态集合
        request.setAttribute("phCustomerStatusList",
                getDictionaryByCode(DicCodeEnum.PHCUSTOMERSTATUS.getCode()));
        // 查询字典话务二级客户状态集合
        request.setAttribute("phCustomerStatusSubList",
                getDictionaryByCode(DicCodeEnum.PHCUSTOMERSTATUSSUB.getCode()));
     // 查询字典电销一级客户状态集合
        request.setAttribute("customerStatusList",
                getDictionaryByCode(DicCodeEnum.CUSTOMERSTATUS.getCode()));
        // 查询字典电销二级客户状态集合
        request.setAttribute("customerStatusSubList",
                getDictionaryByCode(DicCodeEnum.CUSTOMERSTATUSSUB.getCode()));
        long endTime1=System.currentTimeMillis();
        System.out.println("数据字典： "+(endTime1-startTime1)+"ms");
        return "pubcustomer/publicCustomer";
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

    @PostMapping("/queryPage")
    @ResponseBody
    public JSONResult<PageBean<PublicCustomerResourcesRespDTO>> queryListPage(@RequestBody ClueQueryParamDTO dto){

//      参数验证相关
        Date date1 = dto.getCreateTime1();
        Date date2 = dto.getCreateTime2();
        if(date1!=null && date2!=null ){
            if(date1.getTime()>date2.getTime()){
                return new JSONResult().fail("-1","创建时间，开始时间大于结束时间!");
            }
        }
        Date date3 = dto.getReleaseTime1();
        Date date4 = dto.getReleaseTime2();
        if(date3!=null && date4!=null ){
            if(date3.getTime()>date4.getTime()){
                return new JSONResult().fail("-1","释放时间，开始时间大于结束时间!");
            }
        }
        UserInfoDTO user =  CommUtil.getCurLoginUser();
        List<RoleInfoDTO> roleList = user.getRoleList();
        RoleInfoDTO roleInfoDTO = roleList.get(0);
        String roleName = roleInfoDTO.getRoleName();
        if (RoleCodeEnum.DXZJ.value().equals(roleName)||RoleCodeEnum.DXCYGW.value().equals(roleName)){
            dto.setRoleCode(roleInfoDTO.getRoleCode());
        }
        return publicCustomerFeignClient.queryListPage(dto);
    }
    /**
     * 获取指定电销组的电销顾问
     */
    @ResponseBody
    @PostMapping("/findDxcygwByDxzId")
    public  JSONResult<List<UserInfoDTO>>  findDxcygwByDxzId(@RequestBody PublicCustomerResourcesReqDTO dto){
        RoleQueryDTO query = new RoleQueryDTO();
        query.setRoleCode(RoleCodeEnum.DXCYGW.name());
        JSONResult<List<RoleInfoDTO>> roleJson = roleManagerFeignClient.qeuryRoleByName(query);
        List<UserInfoDTO> resList = new ArrayList();
        if (JSONResult.SUCCESS.equals(roleJson.getCode())) {
            List<RoleInfoDTO> roleList = roleJson.getData();
            if (null != roleList && roleList.size() > 0) {
                RoleInfoDTO roleDto = roleList.get(0);
                UserInfoPageParam param = new UserInfoPageParam();
                param.setRoleId(roleDto.getId());
                param.setOrgId(dto.getTeleGroupId());
                param.setPageSize(10000);
                param.setPageNum(1);
                JSONResult<PageBean<UserInfoDTO>> userListJson = userInfoFeignClient.list(param);
                if (JSONResult.SUCCESS.equals(userListJson.getCode())) {
                    PageBean<UserInfoDTO> pageList = userListJson.getData();
                    resList = pageList.getData();
                }
            }
        }
        return new JSONResult<List<UserInfoDTO>>().success(resList);
    }


    /**
     * 查询的是当前组织下电销顾问。
     * @return
     */
    private List dxcygwsOfCurrentOrg(){
        RoleQueryDTO query = new RoleQueryDTO();
        query.setRoleCode(RoleCodeEnum.DXCYGW.name());
        UserInfoDTO user =  CommUtil.getCurLoginUser();
        JSONResult<List<RoleInfoDTO>> roleJson = roleManagerFeignClient.qeuryRoleByName(query);
        List<UserInfoDTO> resList = new ArrayList();
        if (JSONResult.SUCCESS.equals(roleJson.getCode())) {
            List<RoleInfoDTO> roleList = roleJson.getData();
            if (null != roleList && roleList.size() > 0) {
                RoleInfoDTO roleDto = roleList.get(0);
                UserInfoPageParam param = new UserInfoPageParam();
                param.setRoleId(roleDto.getId());
                param.setOrgId(user.getOrgId());
                param.setPageSize(10000);
                param.setPageNum(1);
                JSONResult<PageBean<UserInfoDTO>> userListJson = userInfoFeignClient.list(param);
                if (JSONResult.SUCCESS.equals(userListJson.getCode())) {
                    PageBean<UserInfoDTO> pageList = userListJson.getData();
                    resList = pageList.getData();
                }
            }
        }
        return resList;
    }


    /**
     * 查询的是当前组织下电销顾问。
     * @return
     */
    private List dxcygws(List<Long> orgList){

        UserOrgRoleReq param = new UserOrgRoleReq();
        param.setRoleCode(RoleCodeEnum.DXCYGW.name());
        param.setOrgIdList(orgList);
        JSONResult<List<UserInfoDTO>> userListJson = userInfoFeignClient.listByOrgAndRole(param);
        List<UserInfoDTO> resList = new ArrayList();
        if (JSONResult.SUCCESS.equals(userListJson.getCode())) {
            resList = userListJson.getData();
        }
        return resList;
//        RoleQueryDTO query = new RoleQueryDTO();
//        query.setRoleCode(RoleCodeEnum.DXCYGW.name());
//        UserInfoDTO user =  CommUtil.getCurLoginUser();
//        JSONResult<List<RoleInfoDTO>> roleJson = roleManagerFeignClient.qeuryRoleByName(query);
//        List<UserInfoDTO> resList = new ArrayList();
//        if (JSONResult.SUCCESS.equals(roleJson.getCode())) {
//            List<RoleInfoDTO> roleList = roleJson.getData();
//            if (null != roleList && roleList.size() > 0) {
//                RoleInfoDTO roleDto = roleList.get(0);
//                UserOrgRoleReq param = new UserOrgRoleReq();
//                param.setRoleId(roleDto.getId());
//                param.setOrgIdList(orgList);
//                JSONResult<List<UserInfoDTO>> userListJson = userInfoFeignClient.listByOrgAndRole(param);
//                if (JSONResult.SUCCESS.equals(userListJson.getCode())) {
//                   resList = userListJson.getData();
//                }
//            }
//        }
//        return resList;
    }

    private List dxzjs(List<Long> orgList){
        // 这个查询是不对的啊
        RoleQueryDTO query = new RoleQueryDTO();
        query.setRoleCode(RoleCodeEnum.DXZJ.name());
        UserInfoDTO user =  CommUtil.getCurLoginUser();
        JSONResult<List<RoleInfoDTO>> roleJson = roleManagerFeignClient.qeuryRoleByName(query);
        List<UserInfoDTO> resList = new ArrayList();
        if (JSONResult.SUCCESS.equals(roleJson.getCode())) {
            List<RoleInfoDTO> roleList = roleJson.getData();
            if (null != roleList && roleList.size() > 0) {
                RoleInfoDTO roleDto = roleList.get(0);
                UserInfoPageParam param = new UserInfoPageParam();
                param.setRoleId(roleDto.getId());
                param.setOrgIdList(orgList);
                param.setPageSize(10000);
                param.setPageNum(1);
                JSONResult<PageBean<UserInfoDTO>> userListJson = userInfoFeignClient.list(param);
                if (JSONResult.SUCCESS.equals(userListJson.getCode())) {
                    PageBean<UserInfoDTO> pageList = userListJson.getData();
                    resList = pageList.getData();
                }
            }
        }
        return resList;
    }
}
