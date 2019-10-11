package com.kuaidao.manageweb.controller.merchant.clue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.constant.MenuEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.kuaidao.aggregation.dto.call.CallRecordReqDTO;
import com.kuaidao.aggregation.dto.call.CallRecordRespDTO;
import com.kuaidao.aggregation.dto.project.ProjectInfoDTO;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.util.SortUtils;
import com.kuaidao.manageweb.feign.call.CallRecordFeign;
import com.kuaidao.manageweb.feign.merchant.clue.ClueInfoDetailFeignClient;
import com.kuaidao.manageweb.feign.merchant.user.MerchantUserInfoFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.merchant.dto.clue.ClueDTO;
import com.kuaidao.merchant.dto.clue.ClueQueryDTO;
import com.kuaidao.sys.constant.SysConstant;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * 维护资源信息
 * 
 * @author fanjd
 */
@Slf4j
@Controller
@RequestMapping("/clueInfo")
public class ClueInfoDetailController {
    @Value("${oss.url.directUpload}")
    private String ossUrl;
    @Autowired
    private ProjectInfoFeignClient projectInfoFeignClient;

    @Autowired
    private ClueInfoDetailFeignClient clueInfoDetailFeignClient;

    @Autowired
    private CallRecordFeign callRecordFeign;


    @Autowired
    private MerchantUserInfoFeignClient merchantUserInfoFeignClient;



    /**
     * 进入详情页面
     *
     * @param request
     * @param clueId
     * @return
     */
    @RequestMapping("/init")
    public String init(HttpServletRequest request, @RequestParam String clueId) {
        log.info("ClueInfoDetailController.customerEditInfo_clueId {{}}", clueId);
        UserInfoDTO user = getUser();
        // 项目
        JSONResult<List<ProjectInfoDTO>> proJson = projectInfoFeignClient.allProject();
        if (proJson.getCode().equals(JSONResult.SUCCESS)) {
            List<ProjectInfoDTO> result = SortUtils.sortList(proJson.getData(), "projectName");
            request.setAttribute("proSelect", result);
        } else {
            request.setAttribute("proSelect", new ArrayList());
        }
        request.setAttribute("loginUserId", user.getId());
        return "";
    }

    /**
     * 进入详情页面
     *
     * @param request
     * @param clueId
     * @return
     */
    @GetMapping("/detail")
    public JSONResult<ClueDTO> detail(HttpServletRequest request, @RequestParam String clueId) {
        log.info("ClueInfoDetailController.customerEditInfo_clueId {{}}", clueId);
        UserInfoDTO user = getUser();
        List<Long> userList = new ArrayList<>();
        // 商家主账户能看商家子账号所有的记录
        if (SysConstant.USER_TYPE_TWO.equals(user.getUserType())) {
            getSubAccountIds(userList, user.getId());
        }
        // 商家子账号看自己的记录
        if (SysConstant.USER_TYPE_THREE.equals(user.getUserType())) {
            userList.add(user.getId());
        }
        ClueQueryDTO queryDTO = new ClueQueryDTO();
        queryDTO.setClueId(Long.parseLong(clueId));
        // 获取已上传的文件数据
        ClueQueryDTO fileDto = new ClueQueryDTO();
        CallRecordReqDTO call = new CallRecordReqDTO();
        call.setClueId(clueId);
        if (CollectionUtils.isNotEmpty(userList)) {
            call.setAccountIdList(userList);
            fileDto.setIdList(userList);
        }
        JSONResult<ClueDTO> clueInfo = clueInfoDetailFeignClient.findClueInfo(queryDTO);
        if (clueInfo != null && JSONResult.SUCCESS.equals(clueInfo.getCode()) && clueInfo.getData() != null) {
            JSONResult<List<CallRecordRespDTO>> callRecord = callRecordFeign.listTmCallReacordByParamsNoPage(call);
            // 资源通话记录
            if (callRecord != null && JSONResult.SUCCESS.equals(callRecord.getCode()) && callRecord.getData() != null) {
                clueInfo.getData().setCallRecorList(callRecord.getData());
            }
        }
        return clueInfo;
    }

    /**
     * 维护资源提交
     *
     * @param request
     * @param dto
     * @return
     */
    @RequestMapping("/updateCustomerClue")
    @ResponseBody
    @LogRecord(description = "维护客户资源提交", operationType = LogRecord.OperationType.UPDATE, menuName = MenuEnum.CUSTOMER_INFO)
    public JSONResult<String> updateCustomerClue(HttpServletRequest request, @RequestBody ClueDTO dto) {

        Subject subject = SecurityUtils.getSubject();
        UserInfoDTO user = (UserInfoDTO) subject.getSession().getAttribute("user");
        if (null != user) {
            dto.setUpdateUser(user.getId());
            dto.setOrg(user.getOrgId());
            if (dto.getClueCustomer().getPhoneCreateTime() != null && StringUtils.isNotBlank(dto.getClueCustomer().getPhone())) {
                dto.getClueCustomer().setPhoneCreateUser(user.getId());
            }
            if (dto.getClueCustomer().getPhone2CreateTime() != null && StringUtils.isNotBlank(dto.getClueCustomer().getPhone2())) {
                dto.getClueCustomer().setPhone2CreateUser(user.getId());
            }
            if (dto.getClueCustomer().getPhone3CreateTime() != null && StringUtils.isNotBlank(dto.getClueCustomer().getPhone3())) {
                dto.getClueCustomer().setPhone3CreateUser(user.getId());
            }
            if (dto.getClueCustomer().getPhone4CreateTime() != null && StringUtils.isNotBlank(dto.getClueCustomer().getPhone4())) {
                dto.getClueCustomer().setPhone4CreateUser(user.getId());
            }
            if (dto.getClueCustomer().getPhone5CreateTime() != null && StringUtils.isNotBlank(dto.getClueCustomer().getPhone5())) {
                dto.getClueCustomer().setPhone5CreateUser(user.getId());
            }
        }
        return clueInfoDetailFeignClient.updateCustomerClue(dto);
    }

    /**
     * 获取当前登录账号
     * 
     * @return
     */
    private UserInfoDTO getUser() {
        Object attribute = SecurityUtils.getSubject().getSession().getAttribute("user");
        UserInfoDTO user = (UserInfoDTO) attribute;
        return user;
    }

    /**
     * 获取商家主账户下的子账号
     * 
     * @author: Fanjd
     * @param subIds 用户集 合userId 用户id
     * @return: void
     * @Date: 2019/10/10 20:30
     * @since: 1.0.0
     **/
    private void getSubAccountIds(List<Long> subIds, Long userId) {

        // 获取商家主账号下的子账号列表
        UserInfoDTO userReqDto = buildQueryReqDto(SysConstant.USER_TYPE_THREE, userId);
        JSONResult<List<UserInfoDTO>> merchantUserList = merchantUserInfoFeignClient.merchantUserList(userReqDto);
        if (merchantUserList.getCode().equals(JSONResult.SUCCESS)) {
            if (CollectionUtils.isNotEmpty(merchantUserList.getData())) {
                // 获取子账号id放入子账号集合中
                subIds.addAll(merchantUserList.getData().stream().map(UserInfoDTO::getId).collect(Collectors.toList()));
            }
        }

    }

    /**
     * 构建商家子账户查询实体
     * 
     * @author: Fanjd
     * @param userType 用户类型
     * @param id 用户id
     * @return: com.kuaidao.sys.dto.user.UserInfoDTO
     * @Date: 2019/10/10 20:28
     * @since: 1.0.0
     **/
    private UserInfoDTO buildQueryReqDto(Integer userType, Long id) {

        // 获取商家主账号下的子账号列表
        UserInfoDTO userReqDto = new UserInfoDTO();
        // 商家主账户
        userReqDto.setUserType(userType);
        // 启用和锁定
        List<Integer> statusList = new ArrayList<>();
        statusList.add(SysConstant.USER_STATUS_ENABLE);
        statusList.add(SysConstant.USER_STATUS_LOCK);
        userReqDto.setStatusList(statusList);
        // 商家主账号id
        userReqDto.setParentId(id);
        return userReqDto;
    }
}
