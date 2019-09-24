package com.kuaidao.manageweb.controller.merchant.recharge;

import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.manageweb.constant.Constants;
import com.kuaidao.manageweb.controller.merchant.charge.ClueChargeController;
import com.kuaidao.manageweb.feign.merchant.recharge.MerchantRechargePreferentialFeignClient;
import com.kuaidao.merchant.dto.recharge.MerchantRechargePreferentialDTO;
import com.kuaidao.merchant.dto.recharge.MerchantRechargePreferentialReq;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created on: 2019-09-23-9:33
 */
@Controller
@RequestMapping("/merchant/merchantRechargePreferential")
public class MerchantRechargePreferentialController {

    private static Logger logger = LoggerFactory.getLogger(MerchantRechargePreferentialController.class);

    @Autowired
    private MerchantRechargePreferentialFeignClient merchantRechargePreferentialFeignClient;

    /***
     * 线下付款页面
     *
     * @return
     */
    //todo
    @RequestMapping("/initOfflinePayment")
    @RequiresPermissions("???")
    public String initOfflinePayment(HttpServletRequest request) {
        request.setAttribute("paymentName",
            Constants.PAYMENT_NAME);
        request.setAttribute("paymentAccount",
            Constants.PAYMENT_ACCOUNT);
        request.setAttribute("bank",
            Constants.BANK);
        return "???";
    }

    /***
     * 充值优惠设置页面
     *
     * @return
     */
    @RequestMapping("/initRechargePreferential")
    @RequiresPermissions("merchant:merchantRechargePreferential:view")
    public String initRechargePreferential(HttpServletRequest request) {

        return "merchant/chargeSetting/chargeSetting";
    }

    /***
     * 批量保存充值优惠
     *
     * @return
     */
    @RequestMapping("/saveBatchRechargePreferential")
    @RequiresPermissions("merchant:merchantRechargePreferential:save")
    @ResponseBody
    public JSONResult saveBatchRechargePreferential(@RequestBody List<MerchantRechargePreferentialDTO> list) {
        return merchantRechargePreferentialFeignClient.saveBatchRechargePreferential(list);
    }

    /***
     * 批量更新充值优惠
     *
     * @return
     */
    @RequestMapping("/updateBatchRechargePreferential")
    @RequiresPermissions("merchant:merchantRechargePreferential:edit")
    @ResponseBody
    public JSONResult updateBatchRechargePreferential(@RequestBody List<MerchantRechargePreferentialDTO> list) {
        return merchantRechargePreferentialFeignClient.updateBatchRechargePreferential(list);
    }

    /***
     * 删除充值优惠
     *
     * @return
     */
    @RequestMapping("/deleteBatchRechargePreferential")
    @ResponseBody
    public JSONResult deleteBatchRechargePreferential(@RequestBody IdEntityLong idEntityLong) {
        return merchantRechargePreferentialFeignClient.deleteBatchRechargePreferential(idEntityLong);
    }

    /***
     * 查询所有优惠金额
     *
     * @return
     */
    @RequestMapping("/findAllRechargePreferential")
    @ResponseBody
    public JSONResult<List<MerchantRechargePreferentialDTO>> findAllRechargePreferential(
        @RequestBody MerchantRechargePreferentialReq req) {
        return merchantRechargePreferentialFeignClient.findAllRechargePreferential(req);
    }
}
