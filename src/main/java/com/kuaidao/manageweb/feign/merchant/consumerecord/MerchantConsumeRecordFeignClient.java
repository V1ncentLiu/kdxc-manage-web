package com.kuaidao.manageweb.feign.merchant.consumerecord;

import com.kuaidao.account.dto.consume.*;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 消费记录
 * 
 * @author: zxy
 * @date: 2019年3月14日
 * @version V1.0
 */
@FeignClient(name = "account-service", path = "/account/consumeRecord",
        fallbackFactory = MerchantConsumeRecordFeignClient.HystrixClientFallback.class)
public interface MerchantConsumeRecordFeignClient {
    /**
     * 新增消费记录
     * 
     * @return
     */
    @PostMapping("/create")
    public JSONResult<Long> create(@RequestBody MerchantConsumeRecordReq merchantConsumeRecordReq);



    /**
     * 根据id查询消费记录信息
     * 
     * @return
     */
    @PostMapping("/get")
    public JSONResult<MerchantConsumeRecordDTO> get(@RequestBody IdEntityLong id);


    /**
     * 查询消费明细
     * 
     * @return
     */
    @PostMapping("/list")
    public JSONResult<PageBean<MerchantConsumeRecordDTO>> list(
            @RequestBody MerchantConsumeRecordPageParam pageParam);

    /**
     * 查询消费记录（商家端）
     * 
     * @return
     */
    @PostMapping("/countListMerchant")
    public JSONResult<PageBean<CountConsumeRecordDTO>> countListMerchant(
            @RequestBody MerchantConsumeRecordPageParam pageParam);

    /**
     * 查询消费记录（管理端）
     * 
     * @return
     */
    @PostMapping("/countList")
    public JSONResult<PageBean<CountConsumeRecordDTO>> countList(
            @RequestBody MerchantConsumeRecordPageParam pageParam);

    /**
     * 查询今日、昨日消费统计（管理端）
     * 
     * @return
     */
    @PostMapping("/countNum")
    public JSONResult<ConsumeRecordNumDTO> countNum(
            @RequestBody MerchantConsumeRecordPageParam pageParam);

    /**
     * 导出分公司消费记录
     */
    @PostMapping("/exportCompanyConsumeRecord")
    public JSONResult<List<CompanyConsumeRecordDTO>> exportCompanyConsumeRecord(@RequestBody CompanyConsumeRecordReq req);

    @PostMapping("/listDeatilExport")
    public JSONResult<List<MerchantConsumeRecordDTO>> listDeatilExport(@RequestBody MerchantConsumeRecordPageParam pageParam) ;

    @Component
    static class HystrixClientFallback
            implements FallbackFactory<MerchantConsumeRecordFeignClient> {

        private static Logger logger = LoggerFactory.getLogger(HystrixClientFallback.class);

        @Override
        public MerchantConsumeRecordFeignClient create(Throwable cause) {
            return new MerchantConsumeRecordFeignClient() {
                @SuppressWarnings("rawtypes")
                private JSONResult fallBackError(String name) {
                    logger.error("接口调用失败");
                    logger.error("接口名{}", name);
                    logger.error("失败原因{}", cause);
                    return new JSONResult().fail(SysErrorCodeEnum.ERR_REST_FAIL.getCode(),
                            SysErrorCodeEnum.ERR_REST_FAIL.getMessage());
                }

                @Override
                public JSONResult<MerchantConsumeRecordDTO> get(IdEntityLong id) {
                    return fallBackError("根据id查询消费记录信息");
                }

                @Override
                public JSONResult<Long> create(MerchantConsumeRecordReq merchantConsumeRecordReq) {
                    return fallBackError("新增消费记录");
                }

                @Override
                public JSONResult<PageBean<MerchantConsumeRecordDTO>> list(
                        MerchantConsumeRecordPageParam pageParam) {
                    return fallBackError("查询消费明细");
                }

                @Override
                public JSONResult<PageBean<CountConsumeRecordDTO>> countListMerchant(
                        MerchantConsumeRecordPageParam pageParam) {
                    return fallBackError("查询查询消费记录（商家端）");
                }

                @Override
                public JSONResult<PageBean<CountConsumeRecordDTO>> countList(
                        MerchantConsumeRecordPageParam pageParam) {
                    return fallBackError("查询消费记录（管理端）");
                }

                @Override
                public JSONResult<ConsumeRecordNumDTO> countNum(
                        MerchantConsumeRecordPageParam pageParam) {
                    return fallBackError("查询今日、昨日消费统计（管理端）");
                }

                @Override
                public JSONResult<List<CompanyConsumeRecordDTO>> exportCompanyConsumeRecord(CompanyConsumeRecordReq req) {
                    return fallBackError("导出分公司消费记录");
                }

                @Override
                public JSONResult<List<MerchantConsumeRecordDTO>> listDeatilExport(MerchantConsumeRecordPageParam pageParam) {
                    return fallBackError("导出明细");
                }

            };
        }


    }

}
