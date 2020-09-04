package com.kuaidao.manageweb.controller.language;


import com.alibaba.fastjson.JSON;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.custservice.dto.language.CommonLanguagePageReqDTO;
import com.kuaidao.custservice.dto.language.CommonLanguageReqDto;
import com.kuaidao.custservice.dto.language.CommonLanguageRespDto;
import com.kuaidao.manageweb.feign.language.CommonLanguageFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
* 接口层
* Created  on 2020-9-1 10:42:23
*/
@RestController
@RequestMapping("/commonLanguage")
public class CommonLanguageController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CommonLanguageFeignClient commonLanguageFeignClient;


    /**
     * 更新常用词
     * @param commonLanguageReq
     * @return
     */
    @PostMapping(value = "/save")
    public JSONResult<Boolean> save(CommonLanguageReqDto commonLanguageReq) {
        logger.info("manager-web CommonLanguageController saveOrUpdate:param{}", JSON.toJSONString(commonLanguageReq));
        if(commonLanguageReq.getType()==null){
            return new JSONResult<Boolean>().fail("-1","新增常用词类型不能为空");
        }
        if(commonLanguageReq.getComText()==null){
            return new JSONResult<Boolean>().fail("-1","常用词内容不能为空");
        }
        if(commonLanguageReq.getComText().length()>500){
            return new JSONResult<Boolean>().fail("-1","常用语内容不能超过500字");
        }
        commonLanguageReq.setCreateTime(new Date());
        return commonLanguageFeignClient.insert(commonLanguageReq);
    }

    /**
     * 更新常用词
     * @param commonLanguageReq
     * @return
     */
    @PostMapping(value = "/update")
    public JSONResult<Boolean> update(CommonLanguageReqDto commonLanguageReq) {
        if(commonLanguageReq.getId()==null){
            return new JSONResult<Boolean>().fail("-1","修改操作常用语不能为空");
        }
        if(commonLanguageReq.getComText()==null){
            return new JSONResult<Boolean>().fail("-1","常用词内容不能为空");
        }
        if(commonLanguageReq.getComText().length()>500){
            return new JSONResult<Boolean>().fail("-1","常用语内容不能超过500字");
        }
        commonLanguageReq.setWeight(null);

        logger.info("manager-web CommonLanguageController saveOrUpdate:param{}", JSON.toJSONString(commonLanguageReq));
        commonLanguageReq.setUpdateTime(new Date());
        return commonLanguageFeignClient.update(commonLanguageReq);
    }

    /**
     * 根据ID查处常用词
     * @return
     */
    @PostMapping(value = "/deleteById")
    public JSONResult<Boolean> deleteById(Long  id) {
        CommonLanguageReqDto commonLanguageReqDto = new CommonLanguageReqDto();
        commonLanguageReqDto.setId(id);
        return commonLanguageFeignClient.deleteById(commonLanguageReqDto);
    }



    /**
     * 更新顺序
     * @return
     */
    @PostMapping(value = "/updateOrder")
    public JSONResult<Boolean> updateOrder(CommonLanguageReqDto commonLanguageReqDto  ) {
        if(commonLanguageReqDto.getStartId()==null || commonLanguageReqDto.getEndId()==null){
            return new JSONResult<Boolean>().fail("-1","常用词更新ID不能为空");
        }

        if(commonLanguageReqDto.getStartId()== commonLanguageReqDto.getEndId()){
            return new JSONResult<Boolean>().fail("-1","常用词更新ID不能相同");
        }
        return commonLanguageFeignClient.updateOrder(commonLanguageReqDto);

    }

}