/**
 * 
 */
package com.kuaidao.manageweb.controller.merchant.charge;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.IdListLongReq;
import com.kuaidao.common.entity.PageBean;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.ResponseBody;
import com.kuaidao.common.constant.DicCodeEnum;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.util.CommonUtil;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.config.LogRecord.OperationType;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.merchant.charge.ClueChargeFeignClient;
import com.kuaidao.merchant.dto.charge.MerchantClueChargeDTO;
import com.kuaidao.merchant.dto.charge.MerchantClueChargePageParam;
import com.kuaidao.merchant.dto.charge.MerchantClueChargeReq;
import com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;

/**
 * @author zxy
 *
 */
@Slf4j
@Controller
@RequestMapping("/merchant/clueChargeManager")
public class ClueChargeController {
    @Autowired
    private ClueChargeFeignClient clueChargeFeignClient;
    @Autowired
    private DictionaryItemFeignClient dictionaryItemFeignClient;

    /***
     * 资源资费列表页
     * 
     * @return
     */
    @RequestMapping("/initClueChargeList")
    @RequiresPermissions("merchant:clueChargeManager:view")
    public String initClueChargeList(HttpServletRequest request) {
        //资源类别
        request.setAttribute("categoryList", getDictionaryByCode(DicCodeEnum.CLUECHARGECATEGORY.getCode()));
        //资源媒介
        request.setAttribute("sourceList", getDictionaryByCode(DicCodeEnum.MEDIUM.getCode()));
        //行业类别
        request.setAttribute("industryCategoryList", getDictionaryByCode(DicCodeEnum.INDUSTRYCATEGORY.getCode()));
        return "merchant/charge/clueChargeManagerPage";
    }

    /**
     * 查询资源资费列表
     *
     * @param
     * @return
     */
    @ResponseBody
    @PostMapping("/queryPage")
    public JSONResult<PageBean<MerchantClueChargeDTO>> queryPage(@RequestBody MerchantClueChargeReq pageParam) {

        return clueChargeFeignClient.queryPage(pageParam);

    }
    /**
     * 删除资源费用
     *
     * @param idListLongReq
     * @return
     */
    @ResponseBody
    @PostMapping("/delete")
    public JSONResult<String> delete(@RequestBody IdListLongReq idListLongReq) {
        MerchantClueChargeReq merchantClueChargeReq = new MerchantClueChargeReq();
        merchantClueChargeReq.setUpdateUser(getUserId());
        merchantClueChargeReq.setUpdateTime(new Date());
        merchantClueChargeReq.setIdList(idListLongReq.getIdList());
    return clueChargeFeignClient.delete(merchantClueChargeReq);
    }

    /***
     * 资源资费列表(不分页)
     *
     * @return
     */
    @PostMapping("/listNoPage")
    @ResponseBody
    public JSONResult<List<MerchantClueChargeDTO>> listNoPage(@RequestBody MerchantClueChargePageParam merchantClueChargePageParam,
            HttpServletRequest request) {

        JSONResult<List<MerchantClueChargeDTO>> list = clueChargeFeignClient.listNoPage(merchantClueChargePageParam);

        return list;
    }



    /**
     * 保存资源资费
     * 
     * @param orgDTO
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @PostMapping("/saveClueCharge")
    @ResponseBody
    @RequiresPermissions("merchant:clueChargeManager:edit")
    @LogRecord(description = "编辑资源资费", operationType = OperationType.UPDATE, menuName = MenuEnum.CLUE_CHARGE_MANAGEMENT)
    public JSONResult saveClueCharge(@Valid @RequestBody MerchantClueChargeReq merchantClueChargeReq) {
        long userId = getUserId();
        if (null == merchantClueChargeReq.getId()) {
            merchantClueChargeReq.setCreateUser(userId);

        }
        merchantClueChargeReq.setUpdateUser(userId);
        return clueChargeFeignClient.insertOrUpdate(merchantClueChargeReq);
    }


    /**
     * 获取当前登录账号ID
     * 
     * @param orgDTO
     * @return
     */
    private long getUserId() {
        Object attribute = SecurityUtils.getSubject().getSession().getAttribute("user");
        UserInfoDTO user = (UserInfoDTO) attribute;
        return user.getId();
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

}
