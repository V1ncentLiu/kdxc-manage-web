package com.kuaidao.manageweb.controller.statistics.nextDayInvitation;

import com.kuaidao.businessconfig.dto.project.CompanyInfoDTO;
import com.kuaidao.common.constant.OrgTypeConstant;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.util.DateUtil;
import com.kuaidao.common.util.ExcelUtil;
import com.kuaidao.manageweb.controller.statistics.BaseStatisticsController;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.project.CompanyInfoFeignClient;
import com.kuaidao.manageweb.feign.statistics.nextDayInvitation.NextInvitationFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.stastics.dto.base.BaseQueryDto;
import com.kuaidao.stastics.dto.invitation.NextInvitationDto;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.organization.OrganizationRespDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 次日邀约
 * guhuitao
 * 2019-11-25
 */
@Controller
@RequestMapping("/nextInvitation")
public class NextInvitationController extends BaseStatisticsController {


    @Autowired
    private CompanyInfoFeignClient companyInfoFeignClient;
    @Autowired
    private OrganizationFeignClient organizationFeignClient;
    @Autowired
    private NextInvitationFeignClient nextInvitationFeignClient;

    @RequiresPermissions("statistics:nextInvitation:view")
    @RequestMapping("/list")
    public String deptList(HttpServletRequest request){
        initSaleDept(request);
        OrganizationQueryDTO queryDTO = new OrganizationQueryDTO();
        queryDTO.setOrgType(OrgTypeConstant.SWZ);
        JSONResult<List<OrganizationRespDTO>> json =
                organizationFeignClient.queryOrgByParam(queryDTO);
        request.setAttribute("busList", json.getData());
        //签约集团
        List<CompanyInfoDTO> listNoPage = getCyjt();
        request.setAttribute("companyList", listNoPage);
        return "reportformsBusiness/morrowBusinessVisitTable";
    }


    @RequiresPermissions("statistics:nextInvitation:view")
    @RequestMapping("/groupList")
    public String groupList(HttpServletRequest request,Long companyId,Long busAreaId,Long busGroupId,Long deptGroupId,
    Long teleGroupId,Long dateTimes){
        initSaleDept(request);
        initBaseDto(request,dateTimes,busAreaId,busGroupId,companyId,deptGroupId,teleGroupId);
        OrganizationQueryDTO queryDTO = new OrganizationQueryDTO();
        queryDTO.setOrgType(OrgTypeConstant.SWZ);
        JSONResult<List<OrganizationRespDTO>> json =
                organizationFeignClient.queryOrgByParam(queryDTO);
        request.setAttribute("busList", json.getData());
        //签约集团
        List<CompanyInfoDTO> listNoPage = getCyjt();
        request.setAttribute("companyList", listNoPage);
        return "reportformsBusiness/morrowBusinessVisitTableTeam";
    }

    /**
     * 一级页面请求
     * @param baseQueryDto
     * @return
     */
    @RequiresPermissions("statistics:nextInvitation:view")
    @RequestMapping("/queryPage")
    public @ResponseBody  JSONResult<Map<String,Object>> queryByPage(@RequestBody BaseQueryDto baseQueryDto){
      //权限控制
      initParams(baseQueryDto);
      converAddDateTime(baseQueryDto);
      return nextInvitationFeignClient.queryByPage(baseQueryDto);
    }

    /**
     * 二级页面请求
     * @param baseQueryDto
     * @return
     */
    @RequiresPermissions("statistics:nextInvitation:view")
    @RequestMapping("/queryAreaPage")
    public @ResponseBody JSONResult<Map<String,Object>> queryDeptByPage(@RequestBody BaseQueryDto baseQueryDto){
        //权限控制

        initParams(baseQueryDto);
        converAddDateTime(baseQueryDto);
        return nextInvitationFeignClient.queryAreaPage(baseQueryDto);
    }


    /**
     * 一级页面数据导出
     * @param baseQueryDto
     * @param response
     */
    @RequiresPermissions("statistics:nextInvitation:export")
    @RequestMapping("/invitationExport")
    public @ResponseBody void invitationExport(@RequestBody BaseQueryDto baseQueryDto, HttpServletResponse response){
        try{
            initParams(baseQueryDto);
            converAddDateTime(baseQueryDto);
            JSONResult<List<NextInvitationDto>> json= nextInvitationFeignClient.queryListByParams(baseQueryDto);
            if(null!=json && "0".equals(json.getCode())){
                NextInvitationDto[] dtos = json.getData().isEmpty()?new NextInvitationDto[]{}:json.getData().toArray(new NextInvitationDto[0]);
                String[] keys = {"companyName","busGroupName","invitationCount"};
                String[] hader = {"餐饮集团","商务组","次日邀约数"};
                Workbook wb = ExcelUtil.createWorkBook(dtos, keys, hader);
                String name = MessageFormat.format("次日邀约统计表_{0}.xlsx",  baseQueryDto.getDateTimes() + "");
                response.addHeader("Content-Disposition",
                        "attachment;filename=\"" + name + "\"");
                response.addHeader("fileName", URLEncoder.encode(name, "utf-8"));
                response.setContentType("application/octet-stream");
                ServletOutputStream outputStream = response.getOutputStream();
                wb.write(outputStream);
                outputStream.close();
            }
        }catch (Exception e){
            logger.error(" nextInvitation export error:",e);
        }
    }

    /**
     * 一级页面数据导出
     * @param baseQueryDto
     * @param response
     */
    @RequiresPermissions("statistics:nextInvitation:export")
    @RequestMapping("/areaExport")
    public @ResponseBody void deptExport(@RequestBody BaseQueryDto baseQueryDto, HttpServletResponse response){
        try{
            initParams(baseQueryDto);
            converAddDateTime(baseQueryDto);
            JSONResult<List<NextInvitationDto>> json= nextInvitationFeignClient.queryAreaList(baseQueryDto);
            if(null!=json && "0".equals(json.getCode())){
                NextInvitationDto[] dtos = json.getData().isEmpty()?new NextInvitationDto[]{}:json.getData().toArray(new NextInvitationDto[0]);
                String[] keys = {"deptGroupName","teleGroupName","invitationCount"};
                String[] hader = {"事业部","电销组","次日邀约数"};
                Workbook wb = ExcelUtil.createWorkBook(dtos, keys, hader);
                String name = MessageFormat.format("大区次日邀约统计表_{0}.xlsx",baseQueryDto.getDateTimes() + "");
                response.addHeader("Content-Disposition",
                        "attachment;filename=\"" + name + "\"");
                response.addHeader("fileName", URLEncoder.encode(name, "utf-8"));
                response.setContentType("application/octet-stream");
                ServletOutputStream outputStream = response.getOutputStream();
                wb.write(outputStream);
                outputStream.close();
            }
        }catch (Exception e){
            logger.error(" nextInvitation export error:",e);
        }
    }



    public void initBaseDto(HttpServletRequest request,Long dateTime,Long busAreaId,Long groupId,Long companyId,
                            Long deptGroupId,Long teleGroupId){
        BaseQueryDto dto=new BaseQueryDto();
        dto.setTeleDeptId(deptGroupId);
        dto.setTeleGroupId(teleGroupId);
        dto.setDateTimes(dateTime);
        dto.setCompanyId(companyId);
        dto.setBusAreaId(busAreaId);
        dto.setBusGroupId(groupId);
        request.setAttribute("baseQueryDto",dto);
    }

    /**
     * 参数控制权限-已经显示结果
     * 一级列表所有权限筛选由 组id控制
     * @param dto
     */
    public void initParams(BaseQueryDto dto){
        //筛选组
        if(null!=dto.getTeleDeptId()){
            List<Long> ids= Arrays.asList(dto.getTeleGroupId());
            dto.setTeleDeptIds(ids);
            return ;
        }
        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
        //电销组
        String roleCode=curLoginUser.getRoleList().get(0).getRoleCode();

        if(RoleCodeEnum.DXZJL.name().equals(roleCode)){
            if(null!=dto.getTeleDeptId()){
                dto.setTeleDeptIds(new ArrayList<>(Arrays.asList(dto.getTeleDeptId())));
            }else {
                OrganizationQueryDTO queryDTO = new OrganizationQueryDTO();
                //总监理查看下属所有事业部
                queryDTO.setOrgType(OrgTypeConstant.DZSYB);
                queryDTO.setParentId(curLoginUser.getOrgId());
                JSONResult<List<OrganizationRespDTO>> result =
                        organizationFeignClient.queryOrgByParam(queryDTO);
                List<Long> ids = result.getData().stream().map(c -> c.getId()).collect(Collectors.toList());
                dto.setTeleDeptIds(ids);
            }
        }else if(RoleCodeEnum.DXFZ.name().equals(roleCode)){
            //副总查看当前事业部
            dto.setTeleDeptIds(new ArrayList<>(Arrays.asList(curLoginUser.getOrgId())));
        }else if(RoleCodeEnum.DXZJ.name().equals(roleCode)){
            //总监查看自己组
            dto.setTeleGroupId(curLoginUser.getOrgId());
        }else if(RoleCodeEnum.GLY.name().equals(roleCode) || RoleCodeEnum.DXZC.name().equals(roleCode)){
            //管理员可以查看全部
            if(null!=dto.getTeleDeptId()){
                dto.setTeleDeptIds(new ArrayList<>(Arrays.asList(dto.getTeleDeptId())));
            }
        }else{
            //other
            dto.setTeleGroupId(curLoginUser.getOrgId());
        }
    }


    /**
     * 时间加一天
     * @param baseQueryDto
     */
    private void converAddDateTime(BaseQueryDto baseQueryDto){
        if(null!=baseQueryDto.getDateTimes()){
            Date date=DateUtil.convert2Date(baseQueryDto.getDateTimes()+"","yyyyMMdd");
            String time= DateUtil.convert2String(new Date(date.getTime()+24*60*60*1000l),"yyyyMMdd");
            baseQueryDto.setDateTimes(Long.parseLong(time));
        }
    }


}
