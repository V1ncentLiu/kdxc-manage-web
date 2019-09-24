package com.kuaidao.manageweb.controller.statistics;

import com.kuaidao.common.constant.OrgTypeConstant;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.entity.IdEntity;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.manageweb.controller.statistics.performance.PerformanceController;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO;
import com.kuaidao.sys.dto.organization.OrganizationDTO;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.organization.OrganizationRespDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import com.kuaidao.sys.dto.user.UserOrgRoleReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: guhuitao
 * @create: 2019-08-22 14:21
 **/
@Controller
public class BaseStatisticsController {

    private static Logger logger = LoggerFactory.getLogger(BaseStatisticsController.class);
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;
    @Autowired
    private OrganizationFeignClient organizationFeignClient;
    @Autowired
    private DictionaryItemFeignClient dictionaryItemFeignClient;

    /**
     * 根据商务组id和角色查询 用户
     * @param userOrgRoleReq
     * @return
     */
    @RequestMapping("/base/getSaleList")
    @ResponseBody
    public JSONResult<List<UserInfoDTO>> getSaleList(@RequestBody UserOrgRoleReq userOrgRoleReq) {
        try {
            JSONResult<List<UserInfoDTO>> listByOrgAndRole =
                    userInfoFeignClient.listByOrgAndRole(userOrgRoleReq);
            return listByOrgAndRole;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return new JSONResult<List<UserInfoDTO>>().fail(SysErrorCodeEnum.ERR_SYSTEM.getCode(),"系统繁忙，请稍后再试");
    }

    /**
     * 根据参数查询组织机构
     * @param dto
     * @return
     */
    @RequestMapping("/base/getGroupList")
    @ResponseBody
    public JSONResult<List<OrganizationRespDTO>> getGroupList(@RequestBody OrganizationQueryDTO dto) {
        try {
            JSONResult<List<OrganizationRespDTO>> list =
                    organizationFeignClient.queryOrgByParam(dto);
            return list;
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
        return new JSONResult<List<OrganizationRespDTO>>().fail(SysErrorCodeEnum.ERR_SYSTEM.getCode(),"系统繁忙，请稍后再试");
    }


    /**
     * 统计三期页面-根据角色初始化电销事业部-及部分页面参数
     * @param request
     */
    protected void initSaleDept(HttpServletRequest request){
        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
        String roleCode=curLoginUser.getRoleList().get(0).getRoleCode();
        OrganizationQueryDTO queryDTO = new OrganizationQueryDTO();
        //查询电销事业部
        queryDTO.setOrgType(OrgTypeConstant.DZSYB);
        if(RoleCodeEnum.DXZJL.name().equals(roleCode)){
            queryDTO.setParentId(curLoginUser.getOrgId());
        }else if(RoleCodeEnum.DXFZ.name().equals(roleCode)){
            queryDTO.setId(curLoginUser.getOrgId());
            request.setAttribute("deptId",curLoginUser.getOrgId()+"");
        }else if(RoleCodeEnum.DXZJ.name().equals(roleCode) || RoleCodeEnum.DXCYGW.name().equals(roleCode)){
            IdEntity idEntity=new IdEntity();
            idEntity.setId(curLoginUser.getOrgId().toString());
            JSONResult<OrganizationDTO> jsonResult= organizationFeignClient.queryOrgById(idEntity);
            queryDTO.setId(jsonResult.getData().getParentId());
            request.setAttribute("deptId",jsonResult.getData().getParentId()+"");
            request.setAttribute("teleGroupId",curLoginUser.getOrgId()+"");
            if(RoleCodeEnum.DXCYGW.name().equals(roleCode)){
                request.setAttribute("teleSaleId",curLoginUser.getId()+"");
            }
        }else if(RoleCodeEnum.GLY.name().equals(roleCode)){
            //管理员可以查看全部
        }else{
            //other 没权限
            queryDTO.setId(-1l);
        }
        request.setAttribute("roleCode",roleCode);
        JSONResult<List<OrganizationRespDTO>> queryOrgByParam =
                organizationFeignClient.queryOrgByParam(queryDTO);
        request.setAttribute("deptList",queryOrgByParam.getData());
    }



    /**
     * 根据code 码查询字段
     * @param code
     * @return
     */
    protected List<DictionaryItemRespDTO> getDictionaryByCode(String code) {
        try{
            JSONResult<List<DictionaryItemRespDTO>> queryDicItemsByGroupCode =
                    dictionaryItemFeignClient.queryDicItemsByGroupCode(code);
            if (queryDicItemsByGroupCode != null
                    && JSONResult.SUCCESS.equals(queryDicItemsByGroupCode.getCode())) {
                return queryDicItemsByGroupCode.getData();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 按登录用户业务线查询-商务大区
     * @param request
     */
    protected void initBugOrg(HttpServletRequest request){
        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
        String roleCode=curLoginUser.getRoleList().get(0).getRoleCode();

        //查询招商中心
        OrganizationQueryDTO queryDTO = new OrganizationQueryDTO();
        queryDTO.setOrgType(OrgTypeConstant.SWDQ);
        queryDTO.setBusinessLine(curLoginUser.getBusinessLine());
        JSONResult<List<OrganizationRespDTO>> queryOrgByParam =
                organizationFeignClient.queryOrgByParam(queryDTO);
        request.setAttribute("areaList",queryOrgByParam.getData());
        request.setAttribute("curUserId",curLoginUser.getId()+"");
        request.setAttribute("roleCode",roleCode);
    }

}
