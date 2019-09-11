package com.kuaidao.manageweb.controller.merchant.clue;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.kuaidao.merchant.constant.MerchantConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.kuaidao.aggregation.dto.financing.ReconciliationConfirmDTO;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.common.util.DateUtil;
import com.kuaidao.common.util.ExcelUtil;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.merchant.clue.ClueManagementFeignClient;
import com.kuaidao.merchant.dto.clue.ClueAssignReqDto;
import com.kuaidao.merchant.dto.clue.ClueManagementDto;
import com.kuaidao.merchant.dto.clue.ClueManagementParamDto;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;

/**
 * 资源管理
 *
 * @author:fanjd
 * @date:2019/9/10
 * @since 1.0.0
 */
@Slf4j
@Controller
@RequestMapping("/clue/management")
public class ClueManagementController {
    @Autowired
    private ClueManagementFeignClient clueManagementFeignClient;

    /**
     * 资源管理页面初始化
     *
     * @param
     * @return
     */
    @PostMapping("/init")
    public String init(HttpServletRequest request) {

        return "merchant/resourceManagement/resourceManagement";
    }


    /**
     * 查询资源列表
     *
     * @param
     * @return
     */
    @ResponseBody
    @PostMapping("/queryPage")
    public JSONResult<PageBean<ClueManagementDto>> queryPage(@RequestBody ClueManagementParamDto pageParam) {
        UserInfoDTO userInfoDTO = getUser();
        // 用户类型
        pageParam.setUserType(userInfoDTO.getUserType());
        // 用户id
        pageParam.setUserId(userInfoDTO.getId());
        return clueManagementFeignClient.queryPage(pageParam);
    }

    /**
     * 资源分配
     *
     * @param
     * @return
     */
    @ResponseBody
    @PostMapping("/clueAssign")
    public JSONResult<String> clueAssign(@RequestBody ClueAssignReqDto reqDto) {

        return clueManagementFeignClient.clueAssign(reqDto);
    }

    /**
     * 资源管理-导出
     *
     * @param
     * @return
     */
    @PostMapping("/export")
    @RequiresPermissions("clue:management:export")
    @LogRecord(description = "导出", operationType = LogRecord.OperationType.EXPORT, menuName = MenuEnum.CLUE_MANAGEMENT)
    public void export(@RequestBody ClueManagementParamDto reqDto, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long start = System.currentTimeMillis();
        log.info("clueManagement start,param{}", reqDto);
        UserInfoDTO userInfoDTO = getUser();
        // 用户类型
        reqDto.setUserType(userInfoDTO.getUserType());
        // 用户id
        reqDto.setUserId(userInfoDTO.getId());
        JSONResult<List<ClueManagementDto>> listNoPage = clueManagementFeignClient.listNoPage(reqDto);
        List<List<Object>> dataList = new ArrayList<>();
        dataList.add(getHeadTitleList());
        if (JSONResult.SUCCESS.equals(listNoPage.getCode()) && listNoPage.getData() != null && listNoPage.getData().size() != 0) {
            List<ClueManagementDto> resultList = listNoPage.getData();
            int size = resultList.size();
            for (int i = 0; i < size; i++) {
                ClueManagementDto dto = resultList.get(i);
                List<Object> curList = new ArrayList<>();
                // 序号
                curList.add(i + 1);
                // 媒介
                curList.add(dto.getSourceName());
                // 资源项目
                curList.add(dto.getCategoryName());
                // 客户姓名
                curList.add(dto.getCusName());
                // 搜索词
                curList.add(dto.getSearchWord());
                // 留言时间
                curList.add(getTimeStr(dto.getMessageTime()));
                // 留言内容
                curList.add(dto.getMessagePoint());
                // 联系电话
                curList.add(dto.getPhone());
                // 资源区域
                curList.add(dto.getClueArea());
                // 获取时间
                curList.add(getTimeStr(dto.getReceiveTime()));
                // 价格(元)/条
                curList.add(dto.getCharge());
                // 是否分发
                String str = MerchantConstant.ASSIGN_SUB_ACCOUNT_YES.equals(dto.getAssignSubAccount()) ? "是" : "否";
                curList.add(str);
                dataList.add(curList);
            }
        } else {
            log.error("export rule_report res{{}}", listNoPage);
        }
        // 创建一个工作薄
        XSSFWorkbook workBook = new XSSFWorkbook();
        // 创建一个工作薄对象sheet
        XSSFSheet sheet = workBook.createSheet();
        // 设置宽度
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(5, 6000);
        sheet.setColumnWidth(6, 4000);
        sheet.setColumnWidth(7, 4000);
        sheet.setColumnWidth(22, 8000);
        XSSFWorkbook wbWorkbook = ExcelUtil.creat2007ExcelWorkbook(workBook, dataList);
        String name = "资源列表" + DateUtil.convert2String(new Date(), DateUtil.ymdhms2) + ".xlsx";
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(name.getBytes("UTF-8"), "ISO8859-1"));
        response.addHeader("fileName", URLEncoder.encode(name, "utf-8"));
        response.setContentType("application/octet-stream");
        ServletOutputStream outputStream = response.getOutputStream();
        wbWorkbook.write(outputStream);
        outputStream.close();
        Long end = System.currentTimeMillis();
        Long spend = end - start;
        log.info("clueManagement export success,spend:{} ", spend);
    }


    /**
     * 设置EXCEL表头
     * 
     * @return
     */
    private List<Object> getHeadTitleList() {
        List<Object> headTitleList = new ArrayList<>();
        headTitleList.add("序号");
        headTitleList.add("媒介");
        headTitleList.add("资源项目");
        headTitleList.add("客户姓名");
        headTitleList.add("搜索词");
        headTitleList.add("留言时间");
        headTitleList.add("留言内容");
        headTitleList.add("联系电话");
        headTitleList.add("资源区域");
        headTitleList.add("获取时间");
        headTitleList.add("价格(元)/条");
        headTitleList.add("是否分发");
        return headTitleList;
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
     * 转换时间
     * 
     * @author: Fanjd
     * @param date 日期
     * @return: java.lang.String
     * @Date: 2019/9/11 10:57
     * @since: 1.0.0
     **/
    private String getTimeStr(Date date) {
        if (date == null) {
            return "";
        }
        return DateUtil.convert2String(date, DateUtil.ymd);
    }
}
