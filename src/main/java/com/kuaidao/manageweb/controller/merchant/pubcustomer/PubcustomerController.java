package com.kuaidao.manageweb.controller.merchant.pubcustomer;

import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.merchant.publiccustomer.PubcustomerFeignClient;
import com.kuaidao.manageweb.feign.merchant.user.MerchantUserInfoFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.merchant.dto.pubcusres.ClueQueryParamDTO;
import com.kuaidao.merchant.dto.pubcusres.ClueReceiveRecordsDTO;
import com.kuaidao.merchant.dto.pubcusres.PublicCustomerResourcesRespDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import com.kuaidao.sys.dto.user.UserInfoReq;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yangbiao
 * @Date: 2019/2/11 15:08
 * @Description: 公共客户资源-商家端
 */
@Controller
@RequestMapping("/merchant/pubcustomer")
public class PubcustomerController {

    private static Logger logger = LoggerFactory.getLogger(PubcustomerController.class);

    @Autowired
    private DictionaryItemFeignClient dictionaryItemFeignClient;

    @Autowired
    private PubcustomerFeignClient pubcustomerFeignClient;
    @Autowired
    private MerchantUserInfoFeignClient merchantUserInfoFeignClient;
    /**
     * 分页查询
     */
    @RequestMapping("/listPage")
    public String listPage(HttpServletRequest request) {
        return "merchant/pubcustomer/publicCustomer";
    }

  /**
   * 资源领取
   */
  @PostMapping("/receiveClue")
  @ResponseBody
  public JSONResult<ClueReceiveRecordsDTO> receiveClue(
      @RequestBody ClueReceiveRecordsDTO dto) {
    UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
    if(curLoginUser.getUserType() != null && curLoginUser.getUserType() ==2){
      dto.setSetBusiness(curLoginUser.getId());
    }else if(curLoginUser.getUserType() != null && curLoginUser.getUserType() ==3){
      UserInfoReq req = new UserInfoReq();
      req.setId(curLoginUser.getParentId());
      JSONResult<UserInfoReq> jsonResult = merchantUserInfoFeignClient.getMechantUserById(req);
      UserInfoReq userDto = jsonResult.getData();
      dto.setSetBusiness(userDto.getId());
    }
    dto.setReceiveUser(curLoginUser.getId());
    dto.setReceiveTime(new java.util.Date());
    return pubcustomerFeignClient.receiveClue(dto);
  }

  /**
   *  List数据查询
   */
  @PostMapping("/queryPage")
    @ResponseBody
    public JSONResult<PageBean<PublicCustomerResourcesRespDTO>> queryListPage(
            @RequestBody ClueQueryParamDTO dto) {
       UserInfoDTO user = CommUtil.getCurLoginUser();
       if(user.getUserType() != null && user.getUserType() ==2){
         dto.setBussinessAccount(user.getId());
       }else if(user.getUserType() != null && user.getUserType() ==3){
         UserInfoReq req = new UserInfoReq();
         req.setId(user.getParentId());
         JSONResult<UserInfoReq> jsonResult = merchantUserInfoFeignClient.getMechantUserById(req);
         UserInfoReq userDto = jsonResult.getData();
         dto.setBussinessAccount(userDto.getId());
       }
        return pubcustomerFeignClient.queryListPage(dto);
    }


}
