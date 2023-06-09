package com.kuaidao.manageweb.controller.visitrecord;

import com.kuaidao.aggregation.dto.visitrecord.*;
import com.kuaidao.businessconfig.constant.BusinessConfigConstant;
import com.kuaidao.businessconfig.dto.project.CompanyInfoDTO;
import com.kuaidao.businessconfig.dto.project.ProjectInfoDTO;
import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.IdListLongReq;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.util.CommonUtil;
import com.kuaidao.common.util.DateUtil;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.config.LogRecord.OperationType;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.project.CompanyInfoFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.manageweb.feign.visit.VisitRecordFeignClient;
import com.kuaidao.manageweb.feign.visitrecord.BusVisitRecordFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.sys.dto.dictionary.DictionaryItemQueryDTO;
import com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yangbiao
 * @Date: 2019/1/2 15:14
 * @Description: 到访记录
 */

@Controller
@RequestMapping("/busVisitRecord")
public class BusinessVisitRecordController {

    private static Logger logger = LoggerFactory.getLogger(BusinessVisitRecordController.class);

    @Autowired
    private ProjectInfoFeignClient projectInfoFeignClient;

    @Autowired
    private BusVisitRecordFeignClient visitRecordFeignClient;
    @Autowired
    private VisitRecordFeignClient visitRecordFeignClient1;

    @Autowired
    private CompanyInfoFeignClient companyInfoFeignClient;
    @Autowired
    private UserInfoFeignClient userInfoFeignClient;

    @Autowired
    DictionaryItemFeignClient dictionaryItemFeignClient;


    /**
    * @Description 根据项目ID获取项目对应的到访店铺类型集合
    * @param projectId
    * @Return com.kuaidao.common.entity.JSONResult<java.util.List<com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO>>
    * @Author xuyunfeng
    * @Date 19-12-27 下午4:05
    **/
    @PostMapping("/getShortTypeByProjectId")
    @ResponseBody
    public JSONResult<List<DictionaryItemRespDTO>> getShortTypeByProjectId(@RequestBody IdEntityLong projectId) {
        List<DictionaryItemRespDTO> shopList = new ArrayList<>();
        JSONResult<ProjectInfoDTO>  projectInfoDTOJSONResult = projectInfoFeignClient.get(projectId);
        if(JSONResult.SUCCESS.equals(projectInfoDTOJSONResult.getCode())){
            ProjectInfoDTO projectInfoDTO = projectInfoDTOJSONResult.getData();
            if(projectInfoDTO != null && StringUtils.isNotBlank(projectInfoDTO.getShopType())){
                String[] type1 = projectInfoDTO.getShopType().split(",");
                Map<Integer,String> typeMap = new HashMap<>();
                for(int i =0;i<type1.length;i++){
                    typeMap.put(Integer.parseInt(type1[i]),type1[i]);
                }
                DictionaryItemQueryDTO queryDTO = new DictionaryItemQueryDTO();
                queryDTO.setGroupCode("vistitStoreType");
                JSONResult<List<DictionaryItemRespDTO>> result = dictionaryItemFeignClient.queryDicItemsByGroupCode(queryDTO.getGroupCode());
                if (JSONResult.SUCCESS.equals(result.getCode())) {
                    List<DictionaryItemRespDTO> dictionaryItemRespDTOList = result.getData();
                    shopList = dictionaryItemRespDTOList.stream().filter(a->typeMap.containsKey(Integer.parseInt(a.getValue()))).collect(Collectors.toList());
                }
            }
        }
        return new JSONResult<List<DictionaryItemRespDTO>>().success(shopList);
    }

    @RequestMapping("/listPage")
    public String listPage(HttpServletRequest request, @RequestParam String clueId) {
        BusVisitRecordReqDTO recordReqDTO = new BusVisitRecordReqDTO();
        recordReqDTO.setClueId(Long.valueOf(clueId));
        recordReqDTO.setIsVisit(BusinessConfigConstant.YES);
        recordReqDTO.setIsHistory(BusinessConfigConstant.NO);
        JSONResult<List<BusVisitRecordRespDTO>> listJSONResult =
                visitRecordFeignClient.queryList(recordReqDTO);
        List<BusVisitRecordRespDTO> data = new ArrayList<>();
        String notSignReason = "";
        if (JSONResult.SUCCESS.equals(listJSONResult.getCode())) {
            data = listJSONResult.getData();
            if (data != null && data.size() > 0) {
                notSignReason = data.get(data.size() - 1).getNotSignReason();
            }
        }
        // 查询到访状态： 首次到访 2次到访 多次到访 （需要添加接口）

        request.setAttribute("tableData", data);
        request.setAttribute("notSignReason", notSignReason);
        return "bus_mycustomer/showVisitRecord";
    }


    /**
     * 新增
     */
    @RequestMapping("/insert")
    @ResponseBody
    @LogRecord(description = "添加到访记录", operationType = OperationType.INSERT,
        menuName = MenuEnum.CUSTOMER_VISIT_RECORD)
    public JSONResult<Boolean> saveVisitRecord(
            @Valid @RequestBody BusVisitRecordInsertOrUpdateDTO dto, BindingResult result)
            throws Exception {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        UserInfoDTO user = CommUtil.getCurLoginUser();
        dto.setCreateUser(user.getId());
        dto.setId(null);
        if(user.getBusinessLine() != null){
            dto.setBusinessLine(user.getBusinessLine());
        }
        return visitRecordFeignClient.saveVisitRecord(dto);
    }

    /**
     * 更新
     */
    @RequestMapping("/update")
    @ResponseBody
    public JSONResult<Boolean> updateVisitRecord(
            @Valid @RequestBody BusVisitRecordInsertOrUpdateDTO dto, BindingResult result)
            throws Exception {
        if (result.hasErrors()) {
            return CommonUtil.validateParam(result);
        }
        UserInfoDTO user = CommUtil.getCurLoginUser();
        dto.setUpdateUser(user.getId());
        if(user.getBusinessLine() != null){
            dto.setBusinessLine(user.getBusinessLine());
        }
        return visitRecordFeignClient.updateVisitRecord(dto);
    }

    /**
     * 查询 不分页
     */
    @RequestMapping("/queryList")
    @ResponseBody
    public JSONResult<List<BusVisitRecordRespDTO>> queryList(@RequestBody BusVisitRecordReqDTO dto)
            throws Exception {
        dto.setIsHistory(BusinessConfigConstant.NO);
        return this.queryVisitList(dto);
    }

    /**
     * 查询到访记录，且按照商务经理进行分组
     */
    @RequestMapping("/visitListGroupByBusSale")
    @ResponseBody
    public JSONResult<List<Map<String,Object>>> visitListGroupByBusSale(@RequestBody BusVisitRecordReqDTO dto)
        throws Exception {
        JSONResult<List<Map<String,Object>>> result = new JSONResult<>();
        JSONResult<List<BusVisitRecordRespDTO>> listJSONResult = this.queryVisitList(dto);
        if(!CommonUtil.resultCheck(listJSONResult)){
            return result.fail("-1","没有数据");
        }
        // 数据转换过程如下
        List<BusVisitRecordRespDTO> dataList = listJSONResult.getData();
        if(CollectionUtils.isEmpty(dataList)){
            return result.fail("-1","没有数据");
        }

        // 使用stream进行分组，而后分组后转换成map
        Map<Long, List<BusVisitRecordRespDTO>> collectMap = dataList.stream()
            .collect(Collectors.groupingBy(BusVisitRecordRespDTO::getBusinessManagerId));

        // 按照时间进行排序
        for(Map.Entry<Long,List<BusVisitRecordRespDTO>> map:collectMap.entrySet()){
            List<BusVisitRecordRespDTO> collect = map.getValue().stream()
                .sorted(new Comparator<BusVisitRecordRespDTO>() {
                    @Override
                    public int compare(BusVisitRecordRespDTO o1, BusVisitRecordRespDTO o2) {
                        if (o1.getVistitTime().getTime() > o2.getVistitTime().getTime()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }).collect(Collectors.toList());
            map.setValue(collect);
        }
        // 封装成指定类型结果集
        List<Map<String,Object>> resList = new ArrayList<>();
        for(Map.Entry<Long,List<BusVisitRecordRespDTO>> map:collectMap.entrySet()){
            List<BusVisitRecordRespDTO> values = map.getValue();
            if(CollectionUtils.isNotEmpty(values)){
                Map<String,Object> map1 = new HashMap<>();
                map1.put("name",values.get(0).getBusinessManagerName());
                map1.put("data",values);
                resList.add(map1);
            }
        }
        return result.success(resList);
    }


    /**
     * 查询到访记录
     */
    private JSONResult<List<BusVisitRecordRespDTO>> queryVisitList(BusVisitRecordReqDTO dto)
        throws Exception {
        dto.setIsVisit(BusinessConfigConstant.YES);
        return visitRecordFeignClient.queryList(dto);
    }

    /**
     *  用于查看历史记录:
     */
    @RequestMapping("/queryHisList")
    @ResponseBody
    public List<List<BusVisitRecordRespDTO>> queryHisList(@RequestBody BusVisitRecordReqDTO dto) throws Exception {
        dto.setIsVisit(BusinessConfigConstant.YES);
        dto.setIsHistory(BusinessConfigConstant.YES);
        JSONResult<List<BusVisitRecordRespDTO>> listJSONResult = visitRecordFeignClient.queryList(dto);
        List<List<BusVisitRecordRespDTO>> list = new ArrayList<>();
        if(JSONResult.SUCCESS.equals(listJSONResult.getCode())){
            List<BusVisitRecordRespDTO> data = listJSONResult.getData();
            String createUser = "";
            for(int i = 0 ; i < data.size() ; i ++){
                BusVisitRecordRespDTO visitRecordRespDTO = data.get(i);
                if(!createUser.equals(visitRecordRespDTO.getCreateUser())){
                    ArrayList<BusVisitRecordRespDTO> busVisitRecordRespDTOS = new ArrayList<>();
                    busVisitRecordRespDTOS.add(visitRecordRespDTO);
                    list.add(busVisitRecordRespDTOS);
                }else{
                    List<BusVisitRecordRespDTO> visitRecordRespDTOS = list.get(list.size() - 1);
                    visitRecordRespDTOS.add(visitRecordRespDTO);
                }
                createUser = visitRecordRespDTO.getCreateUser();
            }
        }
        // 查询为到访记录：
        VisitNoRecordReqDTO visitNoRecordReqDTO = new VisitNoRecordReqDTO();
        visitNoRecordReqDTO.setClueId(dto.getClueId());
        visitNoRecordReqDTO.setIsHistory(BusinessConfigConstant.YES);
        visitNoRecordReqDTO.setIsVisit(BusinessConfigConstant.NO);
        JSONResult<List<VisitNoRecordRespDTO>> listJSONResult1 = visitRecordFeignClient1.listNoVisitRecordNoPage(visitNoRecordReqDTO);
        if(JSONResult.SUCCESS.equals(listJSONResult1.getCode())){
            List<VisitNoRecordRespDTO> data = listJSONResult1.getData();
            List<BusVisitRecordRespDTO> noVisitList = new ArrayList<BusVisitRecordRespDTO>();
            for(VisitNoRecordRespDTO noRecord : data){
                BusVisitRecordRespDTO busVisitRecordRespDTO = new BusVisitRecordRespDTO();
                busVisitRecordRespDTO.setCreateTime(noRecord.getCreateTime());
                busVisitRecordRespDTO.setCreateUser(noRecord.getCreateUser());
                busVisitRecordRespDTO.setCreateUserName(noRecord.getCreateUserName());
                busVisitRecordRespDTO.setIsVisit(BusinessConfigConstant.NO);
                busVisitRecordRespDTO.setCustomerName(noRecord.getCusName());
                busVisitRecordRespDTO.setProjectName(noRecord.getTasteProjectName());
                busVisitRecordRespDTO.setSignProvince(noRecord.getProvince());
                busVisitRecordRespDTO.setSignCity(noRecord.getCity());
                busVisitRecordRespDTO.setSignDistrict(noRecord.getDistrict());
                busVisitRecordRespDTO.setNotVisitReason(noRecord.getNotVisitReason());
                busVisitRecordRespDTO.setStatus(noRecord.getStatus());
                busVisitRecordRespDTO.setRebutTime(noRecord.getRebutTime());
                busVisitRecordRespDTO.setRebutReason(noRecord.getRebutReason());
                noVisitList.add(busVisitRecordRespDTO);
            }
            list.add(noVisitList);
        }


        return list;
    }


    /**
     * 查询明细
     */
    @RequestMapping("/one")
    @ResponseBody
    public JSONResult<BusVisitRecordRespDTO> queryOne(@RequestBody IdEntityLong idEntityLong) throws Exception {
        BusVisitRecordRespDTO  busVisitRecordRespDTO = new BusVisitRecordRespDTO();
        //获取到访记录
        JSONResult<BusVisitRecordRespDTO> jsonResult = visitRecordFeignClient.queryOne(idEntityLong);
        if (JSONResult.SUCCESS.equals(jsonResult.getCode()) && jsonResult.getData() != null) {
            busVisitRecordRespDTO =    jsonResult.getData();
            IdEntityLong projectId = new IdEntityLong();
            projectId.setId(busVisitRecordRespDTO.getProjectId());
            JSONResult<List<DictionaryItemRespDTO>> vistitStoreJson = getShortTypeByProjectId(projectId);
            busVisitRecordRespDTO.setVistitStoreTypeArr(vistitStoreJson.getData());
            if(!checkShopType(busVisitRecordRespDTO.getVistitStoreType(),vistitStoreJson.getData())){
                busVisitRecordRespDTO.setVistitStoreType(null);
            }
            if (null  !=busVisitRecordRespDTO.getAuditPerson()) {
                IdEntityLong idEntityLongUser = new IdEntityLong();
                idEntityLongUser.setId(busVisitRecordRespDTO.getAuditPerson());
                JSONResult<UserInfoDTO> userInfoReslust = userInfoFeignClient.get(idEntityLongUser);
                if (JSONResult.SUCCESS.equals(userInfoReslust.getCode()) && userInfoReslust.getData() != null) {
                    //转换用户名
                    busVisitRecordRespDTO.setAuditPersonName(userInfoReslust.getData().getName());
                }
            }

        }
        return new JSONResult<BusVisitRecordRespDTO>().success(busVisitRecordRespDTO);
    }

    /**
     * 跳转到 到访记录明细页面
     */
    @RequestMapping("/visitRecordPage")
    public String visitRecordPage(HttpServletRequest request, @RequestParam String clueId,
            @RequestParam String visitStatus, @RequestParam String signAuditStatus,
        @RequestParam(required = false) Long busGroupId)
            throws Exception {
        request.setAttribute("clueId", clueId);
        request.setAttribute("busGroupId", busGroupId);
        request.setAttribute("visitStatus", visitStatus);
        request.setAttribute("signAuditStatus", signAuditStatus);
        // 项目
        JSONResult<List<ProjectInfoDTO>> proJson = projectInfoFeignClient.allProject();
        if (JSONResult.SUCCESS.equals(proJson.getCode())) {
            List<ProjectInfoDTO> alist = proJson.getData().parallelStream().filter(a->BusinessConfigConstant.NO.equals(a.getIsNotSign())).collect(Collectors.toList());
            request.setAttribute("proSelect", alist);
        }

        JSONResult<List<CompanyInfoDTO>> listJSONResult = companyInfoFeignClient.allCompany();
        if (JSONResult.SUCCESS.equals(listJSONResult.getCode())) {
            request.setAttribute("companySelect", proJson.getData());
        }
        return "bus_mycustomer/showVisitRecord";
    }

    /**
     * 到访记录，新增时候回显信息
     */
    @RequestMapping("/echo")
    @ResponseBody
    public JSONResult<BusVisitRecordRespDTO> echo(@RequestBody IdEntityLong idEntityLong)
            throws Exception {

        BusVisitRecordRespDTO recordRespDTO = new BusVisitRecordRespDTO();
        // 查询需要进行回显的信息，并进行映射
        /**
         * 考察公司： 分公司 到访时间： 预约时间 客户姓名： 资源-客户姓名 考察项目： 品尝项目 签约省份： 投资意向信息- 省份 签约城市： 投资意向信息- 城市 签约区县：
         * 投资意向信息- 区县 来访城市： 派车单-城市 到访人数： 派车单-客户人数
         */

        // 同步组内最先到访记录
//        JSONResult<BusVisitRecordRespDTO> maxNewOne =
//                visitRecordFeignClient.findMaxNewOne(idEntityLong);
//        BusVisitRecordRespDTO newData = maxNewOne.data();
        BusVisitRecordRespDTO newData = vrecordInGroup(idEntityLong.getId());
        if (newData != null) {
            newData.setRebutReason(null);
            newData.setRebutTime(null);
            newData.setNotSignReason(null);
//            newData.setVisitPeopleNum(null);
            IdEntityLong projectId = new IdEntityLong();
            projectId.setId(newData.getProjectId());
            JSONResult<List<DictionaryItemRespDTO>> vistitStoreJson = getShortTypeByProjectId(projectId);
            newData.setVistitStoreTypeArr(vistitStoreJson.getData());
            if(!checkShopType(newData.getVistitStoreType(),vistitStoreJson.getData())){
                newData.setVistitStoreType(null);
            }
            return new JSONResult<BusVisitRecordRespDTO>().success(newData);
        }

        // 同步最新邀约来访
        JSONResult<Map> echoAppoinment = visitRecordFeignClient.echoAppoinment(idEntityLong);
        Map appData = echoAppoinment.data();
        if (appData != null) {
            Object arrivalTime = appData.get("arrivalTime");
            Date arrDate = null;
            if (arrivalTime == null) {
                arrDate = new Date();
            } else {
                if(arrivalTime instanceof String){
                    arrDate = DateUtil.convert2Date((String) arrivalTime, DateUtil.ymdhms);
                }else{
                    arrDate = new Date((Long) arrivalTime);
                }

            }
            recordRespDTO.setVistitTime(arrDate);
            recordRespDTO.setCustomerName((String) appData.get("cusName"));
            String tasteProjectId = (String) appData.get("tasteProjectId");
            if(tasteProjectId!=null){
                String[] split = tasteProjectId.split(",");
                if (split.length > 0 && !"".equals(split[0])) {
                    recordRespDTO.setProjectId(Long.valueOf(split[0]));
                }
            }
            recordRespDTO.setSignProvince((String) appData.get("signProvince"));
            recordRespDTO.setSignCity((String) appData.get("signCity"));
            recordRespDTO.setSignDistrict((String) appData.get("signDistrict"));
            recordRespDTO.setVisitCity((String) appData.get("city"));
            recordRespDTO.setVisitPeopleNum((Integer) appData.get("cusNum"));
            recordRespDTO.setVisitType(1);
            recordRespDTO.setIsSign(1);
        }
        IdEntityLong projectId = new IdEntityLong();
        projectId.setId(recordRespDTO.getProjectId());
        JSONResult<List<DictionaryItemRespDTO>> vistitStoreJson = getShortTypeByProjectId(projectId);
        recordRespDTO.setVistitStoreTypeArr(vistitStoreJson.getData());
        if(!checkShopType(recordRespDTO.getVistitStoreType(),vistitStoreJson.getData())){
            recordRespDTO.setVistitStoreType(null);
        }
        recordRespDTO.setRebutReason(null);
        recordRespDTO.setRebutTime(null);
        recordRespDTO.setNotSignReason(null);
        return new JSONResult<BusVisitRecordRespDTO>().success(recordRespDTO);
    }



    private BusVisitRecordRespDTO vrecordInGroup(Long clueId){
        UserInfoDTO curLoginUser = CommUtil.getCurLoginUser();
        BusVisitRecordReqDTO recordReqDTO = new BusVisitRecordReqDTO();
        recordReqDTO.setClueId(clueId);
        recordReqDTO.setIsVisit(BusinessConfigConstant.YES);
        recordReqDTO.setBusGroupId(curLoginUser.getOrgId());
        JSONResult<List<BusVisitRecordRespDTO>> result = visitRecordFeignClient.queryList(recordReqDTO);
        List<BusVisitRecordRespDTO> data = result.data();
        if (CollectionUtils.isEmpty(data)) {
            return null;
        }else{
            return data.get(0);
        }
    }



    private Boolean checkShopType(String type,List<DictionaryItemRespDTO> itemList){
        Boolean flag = false;
        if(!StringUtils.isNotBlank(type)){
            flag = false;
        }else if(itemList != null && itemList.size() >0){
            String shopType = itemList.stream().map(a->a.getValue()).collect(Collectors.joining(","));
            if(shopType.contains(type)){
                flag = true;
            }else {
                flag = false;
            }
        }else {
            flag = false;
        }
        return flag;
    }
    /**
     * 未到访原因查看
     */
    @PostMapping("/notVisitReason")
    @ResponseBody
    public JSONResult<VisitNoRecordRespDTO> notVisitReason(@RequestBody IdEntityLong idEntityLong) {
        return visitRecordFeignClient.findMaxNewNotVisitOne(idEntityLong);
    }

    /**
     * 未到访原因查看
     */
    @PostMapping("/notVisitOne")
    @ResponseBody
    public JSONResult<VisitNoRecordRespDTO> notVisitOne(@RequestBody IdEntityLong idEntityLong) {
        return visitRecordFeignClient.notVisitOne(idEntityLong);
    }
}
