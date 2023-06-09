package com.kuaidao.manageweb.controller.financing;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kuaidao.aggregation.dto.financing.FinanceOverCostReqDto;
import com.kuaidao.aggregation.dto.financing.FinanceOverCostRespDto;
import com.kuaidao.common.constant.DicCodeEnum;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.area.SysRegionFeignClient;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.financing.OverCostFeignClient;
import com.kuaidao.sys.dto.area.SysRegionDTO;
import com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;

/**
 * 超成本申请
 * 
 * @author fanjd
 * @date 2020年3月13日 9:23:08
 * @version V1.0
 */

@Controller
@RequestMapping("/financing/overCost")
public class OverCostController {

    @Autowired
    private OverCostFeignClient overCostFeignClient;
    @Autowired
    private SysRegionFeignClient sysRegionFeignClient;
    @Autowired
    private DictionaryItemFeignClient dictionaryItemFeignClient;
    @Value("${oss.url.directUpload}")
    private String ossUrl;

    /**
     * 申请页面
     *
     * @return
     */
    @RequestMapping("/overCostApplyPage")
    @RequiresPermissions("financing:coverCostApply:view")
    public String balanceAccountPage(HttpServletRequest request) {
        // 查询签约店型集合
        request.setAttribute("vistitStoreTypeList", getDictionaryByCode(DicCodeEnum.VISITSTORETYPE.getCode()));
        request.setAttribute("ossUrl", ossUrl);
        return "financing/overCostApply";
    }

    /**
     * 超红线申请列表
     *
     * @return
     */
    @PostMapping("/overCostApplyList")
    @ResponseBody
    @RequiresPermissions("financing:coverCostApply:view")
    public JSONResult<PageBean<FinanceOverCostRespDto>> overCostApplyList(HttpServletRequest request,
            @RequestBody FinanceOverCostReqDto financeOverCostReqDto) {
        UserInfoDTO userInfoDTO = getUser();
        financeOverCostReqDto.setUserId(userInfoDTO.getId());
         financeOverCostReqDto.setRoleCode(userInfoDTO.getRoleCode());
        JSONResult<PageBean<FinanceOverCostRespDto>> list = overCostFeignClient.overCostApplyList(financeOverCostReqDto);
        return list;
    }

    /**
     * 超成本申请确认页面
     *
     * @return
     */
    @RequestMapping("/overCostConfirmPage")
    public String overCostconfirmPage(HttpServletRequest request) {
        // 查询签约店型集合
        request.setAttribute("vistitStoreTypeList", getDictionaryByCode(DicCodeEnum.VISITSTORETYPE.getCode()));
        // 签约省份
        request.setAttribute("provinceList", getProviceList());
        return "financing/overCostConfirmation";
    }

    /**
     * 超成本申请确认
     *
     * @author: Fanjd
     * @param
     * @return:
     * @Date: 2020/3/12 11:37
     * @since: 1.0.0
     **/
    @ResponseBody
    @RequestMapping("/confirm")
    @RequiresPermissions("financing:overCostConfirm:confirm")
    @LogRecord(description = "超成本申请确认", operationType = LogRecord.OperationType.UPDATE, menuName = MenuEnum.OVERCOST_CONFIRM)
    public JSONResult<String> confirm(@RequestBody FinanceOverCostReqDto reqDto) {
        UserInfoDTO userInfoDTO = getUser();
        // 更新人
        reqDto.setUpdateUser(userInfoDTO.getId());
        // 更新时间
        reqDto.setUpdateTime(new Date());
        return overCostFeignClient.confirm(reqDto);
    }

    /**
     * 超成本申请驳回
     *
     * @author: Fanjd
     * @param
     * @return:
     * @Date: 2020/3/12 11:37
     * @since: 1.0.0
     **/
    @ResponseBody
    @RequestMapping("/reject")
    @RequiresPermissions("financing:overCostConfirm:reject")
    @LogRecord(description = "超成本申请驳回", operationType = LogRecord.OperationType.UPDATE, menuName = MenuEnum.OVERCOST_REJECT)
    public JSONResult<String> reject(@RequestBody FinanceOverCostReqDto reqDto) {
        UserInfoDTO userInfoDTO = getUser();
        // 更新人
        reqDto.setUpdateUser(userInfoDTO.getId());
        // 更新时间
        reqDto.setUpdateTime(new Date());
        return overCostFeignClient.reject(reqDto);
    }

    /**
     * 超成本申请确认列表
     *
     * @author: Fanjd
     * @param
     * @return:
     * @Date: 2020/3/12 11:37
     * @since: 1.0.0
     **/
    @ResponseBody
    @RequestMapping("/overCostConfirmList")
    public JSONResult<PageBean<FinanceOverCostRespDto>> overCostConfirmList(@RequestBody FinanceOverCostReqDto reqDto) {
        UserInfoDTO userInfoDTO = getUser();
        reqDto.setUserId(userInfoDTO.getId());
        reqDto.setRoleCode(userInfoDTO.getRoleCode());
        JSONResult<PageBean<FinanceOverCostRespDto>> pageResult = overCostFeignClient.overCostConfirmList(reqDto);
        return pageResult;
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
     * 查询所有省份
     * 
     * @return
     */
    private List<SysRegionDTO> getProviceList() {
        JSONResult<List<SysRegionDTO>> getProviceList = sysRegionFeignClient.getproviceList();
        if (getProviceList != null && JSONResult.SUCCESS.equals(getProviceList.getCode())) {
            return getProviceList.getData();
        }
        return null;
    }

    /**
     * 查询字典表
     *
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
     * 超成本申请
     *
     * @author:
     * @param
     * @return:
     * @Date: 2020/3/12 11:37
     * @since: 1.0.0
     **/
    @PostMapping("/apply")
    @LogRecord(description = "超成本申请", operationType = LogRecord.OperationType.UPDATE, menuName = MenuEnum.OVERCOST_APPLY)
    @ResponseBody
    @RequiresPermissions("financing:coverCostApply:apply")
    public JSONResult<String> apply(@RequestBody FinanceOverCostReqDto reqDto) {
        UserInfoDTO userInfoDTO = getUser();
        reqDto.setUpdateTime(new Date());
        reqDto.setUpdateUser(userInfoDTO.getId());
        return overCostFeignClient.apply(reqDto);
    }

    /**
     * 超成本结算
     *
     * @author:
     * @param
     * @return:
     * @Date: 2020/3/12 11:37
     * @since: 1.0.0
     **/
    @PostMapping("/settlementOverCost")
    @ResponseBody
    @LogRecord(description = "超成本变为已结算", operationType = LogRecord.OperationType.UPDATE, menuName = MenuEnum.OVERCOST_SETTLEMENT)
    @RequiresPermissions("financing:coverCostApply:settlementOverCost")
    public JSONResult<String> settlementOverCost(@RequestBody FinanceOverCostReqDto reqDto) {
        UserInfoDTO userInfoDTO = getUser();
        reqDto.setUpdateTime(new Date());
        reqDto.setUpdateUser(userInfoDTO.getId());
        return overCostFeignClient.settlementOverCost(reqDto);
    }

    /**
     * 超成本申请
     *
     * @author:
     * @param
     * @return:
     * @Date: 2020/3/12 11:37
     * @since: 1.0.0
     **/
    @PostMapping("/findFinanceOverCostById")
    @ResponseBody
    public JSONResult<FinanceOverCostRespDto> findFinanceOverCostById(@RequestBody FinanceOverCostReqDto reqDto) {
        return overCostFeignClient.findFinanceOverCostById(reqDto);
    }
}
