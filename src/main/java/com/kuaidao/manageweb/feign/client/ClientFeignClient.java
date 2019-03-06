package com.kuaidao.manageweb.feign.client;

import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.kuaidao.aggregation.dto.client.AddOrUpdateQimoClientDTO;
import com.kuaidao.aggregation.dto.client.AddOrUpdateTrClientDTO;
import com.kuaidao.aggregation.dto.client.ClientLoginReCordDTO;
import com.kuaidao.aggregation.dto.client.ImportQimoClientDTO;
import com.kuaidao.aggregation.dto.client.ImportTrClientDTO;
import com.kuaidao.aggregation.dto.client.QimoClientQueryDTO;
import com.kuaidao.aggregation.dto.client.QimoClientRespDTO;
import com.kuaidao.aggregation.dto.client.QimoDataRespDTO;
import com.kuaidao.aggregation.dto.client.QueryQimoDTO;
import com.kuaidao.aggregation.dto.client.QueryTrClientDTO;
import com.kuaidao.aggregation.dto.client.TrClientDataRespDTO;
import com.kuaidao.aggregation.dto.client.TrClientQueryDTO;
import com.kuaidao.aggregation.dto.client.TrClientRespDTO;
import com.kuaidao.aggregation.dto.client.UploadTrClientDataDTO;
import com.kuaidao.aggregation.dto.client.UserCnoReqDTO;
import com.kuaidao.aggregation.dto.client.UserCnoRespDTO;
import com.kuaidao.callcenter.dto.QimoOutboundCallDTO;
import com.kuaidao.callcenter.dto.QimoOutboundCallRespDTO;
import com.kuaidao.callcenter.dto.TrAxbOutCallReqDTO;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.entity.IdEntity;
import com.kuaidao.common.entity.IdListReq;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;

/**
 *  坐席管理
 * 
 * @author Chen Chengxue
 * @date: 2019年1月3日 下午5:06:37
 * @version V1.0
 */
@FeignClient(name = "aggregation-service", path = "/aggregation/client/client", fallback = ClientFeignClient.HystrixClientFallback.class)
public interface ClientFeignClient {
    
    /**
     * 保存天润坐席
     * @param reqDTO
     * @return
     */
    @PostMapping("/saveTrClient")
    JSONResult<Boolean> saveTrClient( @RequestBody AddOrUpdateTrClientDTO reqDTO);

    /**
     *更新天润坐席
     */
    @PostMapping("/updateTrClient")
    JSONResult<Boolean> updateTrClient(@RequestBody AddOrUpdateTrClientDTO reqDTO);
    
    /**
     * 删除天润坐席
     * @param idListReq
     * @return
     */
    @PostMapping("/deleteTrClient")
    JSONResult<Boolean> deleteTrClient(@RequestBody IdListReq idListReq);
    
    /**
     * 根据Id 查询天润坐席
     * @param idEntity
     * @return
     */
    @PostMapping("/queryTrClientById")
    JSONResult<TrClientRespDTO> queryTrClientById(@RequestBody IdEntity idEntity);
    
    /**
     *  根据参数查询数据 精确匹配
     * @param queryDTO
     * @return
     */
    @PostMapping("/queryTrClientByParam")
    JSONResult<TrClientRespDTO> queryTrClientByParam(@RequestBody TrClientQueryDTO queryDTO);
    
    /**
     * 分页查询天润坐席
     * @param queryClientDTO
     * @return
     */
    @PostMapping("/listTrClientPage")
    JSONResult<PageBean<TrClientDataRespDTO>> listTrClientPage(@RequestBody QueryTrClientDTO queryClientDTO);
    
    /**
     * 保存七陌坐席
     * @param reqDTO
     * @return
     */
    @PostMapping("/saveQimoClient")
    JSONResult<Boolean> saveQimoClient(@RequestBody AddOrUpdateQimoClientDTO reqDTO);
    
    
    /**
     * 更新七陌坐席
     * @param reqDTO
     * @param result
     * @return
     */
    @PostMapping("/updateQimoClient")
    JSONResult<Boolean> updateQimoClient(@RequestBody AddOrUpdateQimoClientDTO reqDTO);
    
    /**
     * 删除七陌坐席 ，根据ID list 
     * @param idListReq
     * @return
     */
    @PostMapping("/deleteQimoClient")
    JSONResult<Boolean> deleteQimoClient(@RequestBody IdListReq idListReq);
    
    
    /**
     * 根据Id 查询七陌坐席
     * @param idEntity
     * @return
     */
    @PostMapping("/queryQimoClientById")
    JSONResult<QimoClientRespDTO> queryQimoClientById(@RequestBody IdEntity idEntity);
    
    /**
     *   根据参数查询数据 精确匹配
     * @param queryDTO
     * @return
     */
    @PostMapping("/queryQimoClientByParam")
    JSONResult<QimoClientRespDTO> queryQimoClientByParam(@RequestBody QimoClientQueryDTO queryDTO);
    

    /**
     * 分页查询天润坐席
     * @param queryClientDTO
     * @return
     */
    @PostMapping("/listQimoClientPage")
    JSONResult<PageBean<QimoDataRespDTO>> listQimoClientPage( @RequestBody QueryQimoDTO queryClientDTO);
    
    @PostMapping("/uploadTrClientData")
    JSONResult<List<ImportTrClientDTO>> uploadTrClientData( @RequestBody UploadTrClientDataDTO reqClientDataDTO);
    
    /**
     * 更新天润回呼
     * @param reqDTO
     * @return
     */
    @PostMapping("/updateCallbackPhone")
    JSONResult<Boolean> updateCallbackPhone(@RequestBody AddOrUpdateTrClientDTO reqDTO);
    
    /**
     * 上传七陌坐席数据
     * @param reqClientDataDTO
     * @return
     */
    @PostMapping("/uploadQimoClientData")
    JSONResult<List<ImportQimoClientDTO>> uploadQimoClientData(
            UploadTrClientDataDTO<ImportQimoClientDTO> reqClientDataDTO);
    
    /**
     * 根据坐席号和组织机构Id精确查询数据
     * @param userCnoDTO
     * @return
     */
    @PostMapping("/queryUserCnoByCnoAndOrgId")
    JSONResult<UserCnoRespDTO> queryUserCnoByCnoAndOrgId(@RequestBody UserCnoReqDTO userCnoDTO);

    /**
     * 保存用户和坐席关系
     * @return
     */
    @PostMapping("/saveUserCno")
    public JSONResult<Boolean> saveUserCno(@Valid @RequestBody UserCnoReqDTO userCnoDTO);
    
    /**
     * 更新用户和坐席关系
     * @return
     */
    @PostMapping("/updateUserCnoByCnoAndOrgId")
    public JSONResult<Boolean> updateUserCnoByCnoAndOrgId(@RequestBody UserCnoReqDTO userCnoDTO);
    
    
    /**
     *  根据坐席账号查询可用的 七陌坐席
     * @param loginClint 登录坐席
     * @return
     */
    @PostMapping("/queryQimoClientByLoginClient")
    JSONResult<QimoClientRespDTO> queryQimoClientByLoginClient(@RequestBody String loginClint);
    
    /**
     * 七陌坐席外呼
     * @param callDTO
     * @return
     */
    @PostMapping("/qimoOutboundCall")
    JSONResult<QimoOutboundCallRespDTO> qimoOutboundCall(@RequestBody QimoOutboundCallDTO callDTO);
    
    /**
     * 坐席登录记录
     * @param clientLoginRecord
     * @return
     */
    @PostMapping("/clientLoginRecord")
    JSONResult<Boolean> clientLoginRecord(@RequestBody ClientLoginReCordDTO clientLoginRecord);
    

    /**
     * 天润 外呼
     * @param trAxbOutCallReqDTO
     * @return
     */
    @PostMapping("/axbOutCall")
    JSONResult<String> axbOutCall(TrAxbOutCallReqDTO trAxbOutCallReqDTO);
    
	@Component
	static class HystrixClientFallback implements ClientFeignClient {

		private static Logger logger = LoggerFactory.getLogger(ClientFeignClient.class);

		private JSONResult fallBackError(String name) {
			logger.error(name + "接口调用失败：无法获取目标服务");
			return new JSONResult().fail(SysErrorCodeEnum.ERR_REST_FAIL.getCode(),
					SysErrorCodeEnum.ERR_REST_FAIL.getMessage());
		}

        @Override
        public JSONResult<Boolean> saveTrClient(AddOrUpdateTrClientDTO reqDTO) {
            return fallBackError("保存天润坐席");
        }

        @Override
        public JSONResult<Boolean> updateTrClient(AddOrUpdateTrClientDTO reqDTO) {
            return fallBackError("更新天润坐席");
        }

        @Override
        public JSONResult<Boolean> deleteTrClient(IdListReq idListReq) {
            return fallBackError("删除天润坐席");

        }

        @Override
        public JSONResult<TrClientRespDTO> queryTrClientById(IdEntity idEntity) {
            return fallBackError("根据Id查询天润坐席");
        }

        @Override
        public JSONResult<TrClientRespDTO> queryTrClientByParam(TrClientQueryDTO queryDTO) {
            return fallBackError("根据参数查询天润坐席"); 
        }

        @Override
        public JSONResult<PageBean<TrClientDataRespDTO>> listTrClientPage(
                QueryTrClientDTO queryClientDTO) {
            return fallBackError("分页查询天润坐席");
        }

        @Override
        public JSONResult<Boolean> saveQimoClient(AddOrUpdateQimoClientDTO reqDTO) {
            return fallBackError("保存七陌坐席");
        }

        @Override
        public JSONResult<Boolean> updateQimoClient(AddOrUpdateQimoClientDTO reqDTO) {
            return fallBackError("更新七陌坐席");
        }

        @Override
        public JSONResult<Boolean> deleteQimoClient(IdListReq idListReq) {
            return fallBackError("删除七陌坐席");
        }

        @Override
        public JSONResult<QimoClientRespDTO> queryQimoClientById(IdEntity idEntity) {
            return fallBackError("根据ID查询七陌坐席");
        }

        @Override
        public JSONResult<QimoClientRespDTO> queryQimoClientByParam(QimoClientQueryDTO queryDTO) {
            return fallBackError("根据参数查询七陌坐席");
        }

        @Override
        public JSONResult<PageBean<QimoDataRespDTO>> listQimoClientPage(
                QueryQimoDTO queryClientDTO) {
            return fallBackError("分页查询七陌坐席");
        }

        @Override
        public JSONResult<List<ImportTrClientDTO>> uploadTrClientData(
                UploadTrClientDataDTO reqClientDataDTO) {
            return fallBackError("上传天润坐席数据");
        }

        @Override
        public JSONResult<Boolean> updateCallbackPhone(AddOrUpdateTrClientDTO reqDTO) {
            return fallBackError("更新回呼手机号");
        }

        @Override
        public JSONResult<List<ImportQimoClientDTO>> uploadQimoClientData(
                UploadTrClientDataDTO<ImportQimoClientDTO> reqClientDataDTO) {
            return fallBackError("上传七陌坐席数据");
        }

        @Override
        public JSONResult<UserCnoRespDTO> queryUserCnoByCnoAndOrgId(UserCnoReqDTO userCnoDTO) {
            return fallBackError("查询用户和坐席关系");
        }

        @Override
        public JSONResult<Boolean> saveUserCno(UserCnoReqDTO userCnoDTO) {
            return fallBackError("保存用户和坐席关系");
        }

        @Override
        public JSONResult<Boolean> updateUserCnoByCnoAndOrgId(UserCnoReqDTO userCnoDTO) {
            return fallBackError("更新用户和坐席关系");
        }

        @Override
        public JSONResult<QimoClientRespDTO> queryQimoClientByLoginClient(String loginClint) {
            return fallBackError("根据坐席号查询可用的七陌坐席");
        }

        @Override
        public JSONResult<QimoOutboundCallRespDTO> qimoOutboundCall(QimoOutboundCallDTO callDTO) {
            return fallBackError("七陌坐席外呼");
        }

        @Override
        public JSONResult<Boolean> clientLoginRecord(ClientLoginReCordDTO clientLoginRecord) {
            return fallBackError("坐席登录记录");
        }

        @Override
        public JSONResult<String> axbOutCall(TrAxbOutCallReqDTO trAxbOutCallReqDTO) {
            return fallBackError("天润外呼");
        }

	}
   

}
