package com.kuaidao.manageweb.feign.clue;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.kuaidao.aggregation.dto.clue.TelCreatePhoneAuditDTO;
import com.kuaidao.aggregation.dto.clue.TelCreatePhoneAuditReqDTO;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.entity.JSONResult;

/**
 * 新建手机号神而活
 * @author: fanjd
 * @date: 2020年7月16日
 * @version V1.0
 */
@FeignClient(name = "aggregation-service", path = "/aggregation/telCreatePhoneAudit",
        fallback = TelCreatePhoneAuditFeignClient.HystrixClientFallback.class)
public interface TelCreatePhoneAuditFeignClient {

    /**
     * 新建手机号审核
     * @param
     * @return
     */
    @PostMapping("/insert")
    JSONResult<String> insert(@RequestBody TelCreatePhoneAuditReqDTO reqDTO);
    /**
     * 新建手机号审核-无效原因审核
     * @param
     * @return
     */
    @PostMapping("/update")
    JSONResult<String> update(@RequestBody TelCreatePhoneAuditReqDTO reqDTO);
    /**
     * 根据资源id查询最新的审核不通过记录
     */
    @PostMapping("/findNewestByClueId")
    JSONResult<TelCreatePhoneAuditDTO> findNewestByClueId(@RequestBody TelCreatePhoneAuditReqDTO reqDTO);

    @PostMapping("/findListByClueId")
    JSONResult<List<TelCreatePhoneAuditDTO>> findListByClueId(@RequestBody TelCreatePhoneAuditReqDTO reqDTO);

    @Component
    static class HystrixClientFallback implements TelCreatePhoneAuditFeignClient {

        private static Logger logger = LoggerFactory.getLogger(TelCreatePhoneAuditFeignClient.class);

        private JSONResult fallBackError(String name) {
            logger.error(name + "接口调用失败：无法获取目标服务");
            return new JSONResult().fail(SysErrorCodeEnum.ERR_REST_FAIL.getCode(), SysErrorCodeEnum.ERR_REST_FAIL.getMessage());
        }

        @Override
        public JSONResult<String> insert(@RequestBody TelCreatePhoneAuditReqDTO reqDTO) {
            return fallBackError("新建手机号审核插入");
        }
        @Override
        public JSONResult<String> update(@RequestBody TelCreatePhoneAuditReqDTO reqDTO) {
            return fallBackError("新建手机号审核无效原因修改");
        }
        @Override
        public JSONResult<TelCreatePhoneAuditDTO> findNewestByClueId(TelCreatePhoneAuditReqDTO reqDTO) {
            return fallBackError("根据资源id查询最新的审核不通过记录");
        }

        @Override
        public JSONResult<List<TelCreatePhoneAuditDTO>> findListByClueId(TelCreatePhoneAuditReqDTO reqDTO) {
            return fallBackError("根据资源id查询所有的审核不通过记录");
        }
    }

}
