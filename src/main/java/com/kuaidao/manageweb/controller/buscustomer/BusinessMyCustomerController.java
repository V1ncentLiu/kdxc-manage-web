package com.kuaidao.manageweb.controller.buscustomer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.kuaidao.aggregation.dto.busmycustomer.BusMyCustomerReqDTO;
import com.kuaidao.aggregation.dto.busmycustomer.BusMyCustomerRespDTO;
import com.kuaidao.aggregation.dto.busmycustomer.MyCustomerParamDTO;
import com.kuaidao.aggregation.dto.clue.ClueBasicDTO;
import com.kuaidao.aggregation.dto.project.CompanyInfoDTO;
import com.kuaidao.aggregation.dto.project.ProjectInfoDTO;
import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.manageweb.feign.busmycustomer.BusMyCustomerFeignClient;
import com.kuaidao.manageweb.feign.project.CompanyInfoFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.sys.dto.organization.OrganizationDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;

/**
 * @author yangbiao 接口层 Created on 2019-2-12 15:06:38 商务模块--我的客户
 */

@Controller
@RequestMapping("/aggregation/businessMyCustomer")
public class BusinessMyCustomerController {

    private static Logger logger = LoggerFactory.getLogger(BusinessMyCustomerController.class);

    @Autowired
    CompanyInfoFeignClient companyInfoFeignClient;

    @Autowired
    BusMyCustomerFeignClient busMyCustomerFeignClient;

    @Autowired
    private ProjectInfoFeignClient projectInfoFeignClient;

    @RequestMapping("/listPage")
    public String listPage(HttpServletRequest request) {
        logger.info("------------ 商务：我的客户列表 ---------------");
        UserInfoDTO user = CommUtil.getCurLoginUser();
        // 电销组
        List teleGroupList = new ArrayList();
        Map<Long, OrganizationDTO> groupMap = new HashMap();
        List teleSaleList = new ArrayList();
        Map<Long, OrganizationDTO> saleMap = new HashMap();
        List tasteProList = new ArrayList();
        Map tasteMap = new HashMap();
        MyCustomerParamDTO dto = new MyCustomerParamDTO();
        List<RoleInfoDTO> roleList = user.getRoleList();
        if (roleList != null && RoleCodeEnum.GLY.name().equals(roleList.get(0).getRoleCode())) {
        }else{
            dto.setBusSaleId(user.getId());
        }

        JSONResult<List<BusMyCustomerRespDTO>> resList = busMyCustomerFeignClient.queryList(dto);
        if (JSONResult.SUCCESS.equals(resList.getCode())) {
            List<BusMyCustomerRespDTO> datas = resList.getData();
            for (BusMyCustomerRespDTO myCustomerRespDTO : datas) {

                if (myCustomerRespDTO.getTeleGorupId() != null) {
                    OrganizationDTO organizationDTO = new OrganizationDTO();
                    organizationDTO.setId(myCustomerRespDTO.getTeleGorupId());
                    organizationDTO.setName(myCustomerRespDTO.getTeleGorupName());
                    // teleGroupList.add(organizationDTO);
                    groupMap.put(organizationDTO.getId(), organizationDTO);
                }

                // 创业顾问
                if (myCustomerRespDTO.getTeleSaleId() != null) {
                    OrganizationDTO organizationDTO = new OrganizationDTO();
                    organizationDTO.setId(myCustomerRespDTO.getTeleSaleId());
                    organizationDTO.setName(myCustomerRespDTO.getTeleSaleName());
                    // teleSaleList.add(organizationDTO);
                    saleMap.put(myCustomerRespDTO.getTeleSaleId(), organizationDTO);
                }
                // 品尝项目
                // if(myCustomerRespDTO.getTasteProjectId()!=null){
                // OrganizationDTO organizationDTO = new OrganizationDTO();
                // organizationDTO.setId(myCustomerRespDTO.getTasteProjectId());
                // organizationDTO.setName(myCustomerRespDTO.getTasteProjectName());
                // tasteProList.add(organizationDTO);
                // }
            }
            for (Map.Entry<Long, OrganizationDTO> entry : groupMap.entrySet()) {
                teleGroupList.add(entry.getValue());
            }
            for (Map.Entry<Long, OrganizationDTO> entry : saleMap.entrySet()) {
                teleSaleList.add(entry.getValue());
            }
        }

        // 项目
        JSONResult<List<ProjectInfoDTO>> proJson = projectInfoFeignClient.allProject();
        if (JSONResult.SUCCESS.equals(proJson.getCode())) {
            request.setAttribute("proSelect", proJson.getData());
        }

        JSONResult<List<CompanyInfoDTO>> listJSONResult = companyInfoFeignClient.allCompany();
        if (JSONResult.SUCCESS.equals(listJSONResult.getCode())) {
            request.setAttribute("companySelect", proJson.getData());
        }

        request.setAttribute("teleGroupList", teleGroupList);
        request.setAttribute("teleSaleList", teleSaleList);
        request.setAttribute("tasteProList", tasteProList);
        request.setAttribute("loginUserId", user.getId());
        return "bus_mycustomer/mycustomerList";
    }

    @PostMapping("/queryPage")
    @ResponseBody
    public JSONResult<PageBean<BusMyCustomerRespDTO>> queryListPage(
            @RequestBody MyCustomerParamDTO param) {
        logger.info("============分页数据查询==================");

        Date date1 = param.getReserveTime1();
        Date date2 = param.getReserveTime2();
        if (date1 != null && date2 != null) {
            if (date1.getTime() > date2.getTime()) {
                return new JSONResult().fail("-1", "邀约来访时间，结束时间不能早于开始时间!");
            }
        }

        Date date3 = param.getAllocateTime1();
        Date date4 = param.getAllocateTime2();
        if (date3 != null && date4 != null) {
            if (date3.getTime() > date4.getTime()) {
                return new JSONResult().fail("-1", "接收客户时间，结束时间不能早于开始时间!");
            }
        }

        Date date5 = param.getCreateTime1();
        Date date6 = param.getCreateTime2();
        if (date5 != null && date6 != null) {
            if (date5.getTime() > date6.getTime()) {
                return new JSONResult().fail("-1", "提交邀约时间，结束时间不能早于开始时间!");
            }
        }
        UserInfoDTO user = CommUtil.getCurLoginUser();
        /**
         * 下回代码注释掉，请记得给我改回来
         */
        List<RoleInfoDTO> roleList = user.getRoleList();
        if (roleList != null && RoleCodeEnum.GLY.name().equals(roleList.get(0).getRoleCode())) {
        }else{
            param.setBusSaleId(user.getId());
        }
        return busMyCustomerFeignClient.queryPageList(param);
    }

    /**
     * 标记未到访
     */
    @PostMapping("/notVisit")
    @ResponseBody
    public JSONResult<Boolean> notVisit(@RequestBody BusMyCustomerReqDTO param) {
        return busMyCustomerFeignClient.notVisit(param);
    }

    /**
     * 未到访原因查看
     */
    @PostMapping("/notVisitReason")
    @ResponseBody
    public JSONResult<ClueBasicDTO> notVisitReason(@RequestBody IdEntityLong idEntityLong) {
        return busMyCustomerFeignClient.notVisitReason(idEntityLong);
    }

    @PostMapping("/listNoPage")
    @ResponseBody
    public JSONResult<List<CompanyInfoDTO>> listNoPage() {
        JSONResult<List<CompanyInfoDTO>> list = companyInfoFeignClient.allCompany();
        return list;
    }

}
