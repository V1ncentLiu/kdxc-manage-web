package com.kuaidao.manageweb.feign.financing;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.kuaidao.aggregation.dto.financing.ReconciliationConfirmDTO;
import com.kuaidao.aggregation.dto.financing.ReconciliationConfirmPageParam;
import com.kuaidao.aggregation.dto.financing.ReconciliationConfirmReq;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;

/**
 * 对账结算确认
 * @author: zxy
 * @date: 2019年1月4日
 * @version V1.0
 */
@FeignClient(name = "aggregation-service", path = "/aggregation/reconciliationConfirm",
        fallback = ReconciliationConfirmFeignClient.HystrixClientFallback.class)
public interface ReconciliationConfirmFeignClient {

    /**
     * 对账结算确认列表
     * @param param
     * @return
     */
    @PostMapping("/list")
    JSONResult<PageBean<ReconciliationConfirmDTO>> list(@RequestBody ReconciliationConfirmPageParam param);

    /**
     * 对账结算确认列表
     * @param param
     * @return
     */
    @PostMapping("/listNoPage")
    JSONResult<List<ReconciliationConfirmDTO>> listNoPage(@RequestBody ReconciliationConfirmPageParam param);

    /**
     * 对账、结算确认
     * @param req
     * @return
     */
    @PostMapping("/reconciliationConfirm")
    JSONResult<Void> reconciliationConfirm(@RequestBody ReconciliationConfirmReq req);

    /**
     * 已结算佣金总计
     * @param param
     * @return
     */
    @PostMapping("/sumCommissionMoney")
    JSONResult<BigDecimal> sumCommissionMoney(@RequestBody ReconciliationConfirmPageParam param);

    /**
     * 对账结算申请列表
     * @param param
     * @return
     */
    @PostMapping("/applyList")
    public JSONResult<PageBean<ReconciliationConfirmDTO>> applyList(@RequestBody ReconciliationConfirmPageParam param);

    /**
     * 对账结算申请列表
     * @param param
     * @return
     */
    @PostMapping("/applyListNoPage")
    JSONResult<List<ReconciliationConfirmDTO>> applyListNoPage(@RequestBody ReconciliationConfirmPageParam param);

    /**
     * 驳回
     * @param req
     * @return
     */
    @PostMapping("/rejectApply")
    JSONResult<Void> rejectApply(@RequestBody ReconciliationConfirmReq req);

    /**
     * 根据对账申请表id获取已对账的佣金之和
     * @author: Fanjd
     * @param signId 签约单id
     * @return: com.kuaidao.common.entity.JSONResult<java.lang.Void>
     * @Date: 2019/6/14 18:25
     * @since: 1.0.0
     **/
    @PostMapping("/getConfirmCommission")
    JSONResult<BigDecimal> getConfirmCommission(@RequestParam("signId") Long signId);

    /**
     * 根据付款明细id获取提交对账确认结算金额初始化
     * @author: Fanjd
     * @param idEntityLong 请求实体
     * @return: com.kuaidao.common.entity.JSONResult<java.lang.Void>
     * @Date: 2020/07/08 18:25
     * @since: 1.0.0
     **/
    @PostMapping("/getSettlementAmount")
    JSONResult<BigDecimal> getSettlementAmount(@RequestBody IdEntityLong idEntityLong);

    /**
     * 对账、申请
     * @param req
     * @return
     */
    @PostMapping("/applyConfirm")
   JSONResult<Void> applyConfirm(@RequestBody ReconciliationConfirmReq req);

    @PostMapping("/validateBalance")
  JSONResult<String> validateBalance(@RequestBody ReconciliationConfirmReq req);

    @Component
    static class HystrixClientFallback implements ReconciliationConfirmFeignClient {

        private static Logger logger = LoggerFactory.getLogger(ReconciliationConfirmFeignClient.class);

        private JSONResult fallBackError(String name) {
            logger.error(name + "接口调用失败：无法获取目标服务");
            return new JSONResult().fail(SysErrorCodeEnum.ERR_REST_FAIL.getCode(), SysErrorCodeEnum.ERR_REST_FAIL.getMessage());
        }

        @Override
        public JSONResult<Void> reconciliationConfirm(ReconciliationConfirmReq req) {
            return fallBackError("对账、结算确认");
        }

        @Override
        public JSONResult<PageBean<ReconciliationConfirmDTO>> list(ReconciliationConfirmPageParam param) {
            return fallBackError("对账结算确认列表");
        }

        @Override
        public JSONResult<List<ReconciliationConfirmDTO>> listNoPage(ReconciliationConfirmPageParam param) {
            return fallBackError("对账结算确认列表");
        }

        @Override
        public JSONResult<BigDecimal> sumCommissionMoney(ReconciliationConfirmPageParam param) {
            return fallBackError("已结算佣金总计");
        }

        @Override
        public JSONResult<PageBean<ReconciliationConfirmDTO>> applyList(ReconciliationConfirmPageParam param) {
            return fallBackError("对账申请列表");
        }

        @Override
        public JSONResult<List<ReconciliationConfirmDTO>> applyListNoPage(ReconciliationConfirmPageParam param) {
            return fallBackError("对账申请列表");
        }

        @Override
        public JSONResult<Void> rejectApply(ReconciliationConfirmReq req) {
            // TODO Auto-generated method stub
            return fallBackError("对账驳回");
        }

        @Override
        public JSONResult<BigDecimal> getConfirmCommission(Long accountId) {
            return fallBackError("根据对账申请id获取已确认的对账佣金错误");
        }

        @Override
        public JSONResult<BigDecimal> getSettlementAmount(IdEntityLong idEntityLong) {
            return fallBackError("根据付款明细id获取提交对账确认结算金额初始化金额");
        }

        @Override
        public JSONResult<Void> applyConfirm(ReconciliationConfirmReq req) {
            return fallBackError("对账申请");
        }

        @Override
        public JSONResult<String> validateBalance(ReconciliationConfirmReq req) {
            return fallBackError("提交对账校验是否存在未提交对账的付款");
        }

    }

}
