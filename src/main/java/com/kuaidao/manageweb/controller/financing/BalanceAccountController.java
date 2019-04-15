package com.kuaidao.manageweb.controller.financing;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;

import com.kuaidao.aggregation.constant.AggregationConstant;
import com.kuaidao.aggregation.dto.financing.ReconciliationConfirmDTO;
import com.kuaidao.aggregation.dto.financing.ReconciliationConfirmPageParam;
import com.kuaidao.aggregation.dto.financing.ReconciliationConfirmReq;
import com.kuaidao.aggregation.dto.financing.RefundAndImgRespDTO;
import com.kuaidao.aggregation.dto.financing.RefundImgRespDTO;
import com.kuaidao.aggregation.dto.financing.RefundInfoQueryDTO;
import com.kuaidao.aggregation.dto.financing.RefundQueryDTO;
import com.kuaidao.aggregation.dto.financing.RefundRespDTO;
import com.kuaidao.aggregation.dto.financing.RefundUpdateDTO;
import com.kuaidao.aggregation.dto.paydetail.PayDetailAccountDTO;
import com.kuaidao.aggregation.dto.project.ProjectInfoDTO;
import com.kuaidao.common.constant.DicCodeEnum;
import com.kuaidao.common.constant.OrgTypeConstant;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.constant.SysErrorCodeEnum;
import com.kuaidao.common.entity.IdEntityLong;
import com.kuaidao.common.entity.IdListLongReq;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.common.util.CommonUtil;
import com.kuaidao.manageweb.config.LogRecord;
import com.kuaidao.manageweb.config.LogRecord.OperationType;
import com.kuaidao.manageweb.constant.MenuEnum;
import com.kuaidao.manageweb.feign.area.SysRegionFeignClient;
import com.kuaidao.manageweb.feign.customfield.CustomFieldFeignClient;
import com.kuaidao.manageweb.feign.dictionary.DictionaryItemFeignClient;
import com.kuaidao.manageweb.feign.financing.BalanceAccountApplyClient;
import com.kuaidao.manageweb.feign.financing.ReconciliationConfirmFeignClient;
import com.kuaidao.manageweb.feign.financing.RefundFeignClient;
import com.kuaidao.manageweb.feign.organization.OrganizationFeignClient;
import com.kuaidao.manageweb.feign.project.ProjectInfoFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.manageweb.util.CommUtil;
import com.kuaidao.sys.dto.area.SysRegionDTO;
import com.kuaidao.sys.dto.customfield.CustomFieldQueryDTO;
import com.kuaidao.sys.dto.customfield.QueryFieldByRoleAndMenuReq;
import com.kuaidao.sys.dto.customfield.QueryFieldByUserAndMenuReq;
import com.kuaidao.sys.dto.customfield.UserFieldDTO;
import com.kuaidao.sys.dto.dictionary.DictionaryItemRespDTO;
import com.kuaidao.sys.dto.organization.OrganizationQueryDTO;
import com.kuaidao.sys.dto.organization.OrganizationRespDTO;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import com.kuaidao.sys.dto.user.UserOrgRoleReq;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 *  退返款
 * @author  Chen
 * @date 2019年4月10日 下午7:23:08   
 * @version V1.0
 */
@RequestMapping("/financing/balanceaccount")
@Controller
public class BalanceAccountController {

	 @Autowired
	    private ReconciliationConfirmFeignClient reconciliationConfirmFeignClient;
	    @Autowired
	    private OrganizationFeignClient organizationFeignClient;
	    @Autowired
	    private UserInfoFeignClient userInfoFeignClient;
	    @Autowired
	    private ProjectInfoFeignClient projectInfoFeignClient;
	    @Autowired
	    private DictionaryItemFeignClient dictionaryItemFeignClient;
	    @Autowired
	    private SysRegionFeignClient sysRegionFeignClient;
	    @Autowired
	    private BalanceAccountApplyClient balanceAccountApplyClient;
	    @Autowired
	    private CustomFieldFeignClient customFieldFeignClient;
    private Configuration configuration = null;

    public BalanceAccountController() {
        configuration = new Configuration();
        configuration.setDefaultEncoding("utf-8");
    }
    /**
     * 申请页面
     * @return
     */
    @RequestMapping("/balanceAccountPage")
    @RequiresPermissions("financing:balanceaccountManager:view")
    public String balanceAccountPage(HttpServletRequest request) {
    	 UserInfoDTO user = getUser();
         // 查询所有商务大区
         List<OrganizationRespDTO> busAreaList = getOrgList(null, OrgTypeConstant.SWDQ);
         request.setAttribute("busAreaList", busAreaList);
         // 查询所有商务组
         List<OrganizationRespDTO> busGroupList = getOrgList(null, OrgTypeConstant.SWZ);
         request.setAttribute("busGroupList", busGroupList);
         // 查询所有电销事业部
         List<OrganizationRespDTO> teleDeptList = getOrgList(null, OrgTypeConstant.DZSYB);
         request.setAttribute("teleDeptList", teleDeptList);
         // 查询所有电销组
         List<OrganizationRespDTO> teleGroupList = getOrgList(null, OrgTypeConstant.DXZ);
         request.setAttribute("teleGroupList", teleGroupList);
         // 查询所有商务经理
         List<UserInfoDTO> busSaleList = getUserList(null, RoleCodeEnum.SWJL.name(), null);
         request.setAttribute("busSaleList", busSaleList);
         // 查询所有电销创业顾问
         List<UserInfoDTO> teleSaleList = getUserList(null, RoleCodeEnum.DXCYGW.name(), null);
         request.setAttribute("teleSaleList", teleSaleList);

         // 查询所有项目
         JSONResult<List<ProjectInfoDTO>> allProject = projectInfoFeignClient.allProject();
         request.setAttribute("projectList", allProject.getData());
         // 查询所有省
         JSONResult<List<SysRegionDTO>> getproviceList = sysRegionFeignClient.getproviceList();
         request.setAttribute("provinceList", getproviceList.getData());
         // 根据角色查询页面字段
         QueryFieldByRoleAndMenuReq queryFieldByRoleAndMenuReq = new QueryFieldByRoleAndMenuReq();
         queryFieldByRoleAndMenuReq.setMenuCode("financing:balanceaccountManager");
         queryFieldByRoleAndMenuReq.setId(user.getRoleList().get(0).getId());
         JSONResult<List<CustomFieldQueryDTO>> queryFieldByRoleAndMenu =
                 customFieldFeignClient.queryFieldByRoleAndMenu(queryFieldByRoleAndMenuReq);
         request.setAttribute("fieldList", queryFieldByRoleAndMenu.getData());

         // 根据用户查询页面字段
         QueryFieldByUserAndMenuReq queryFieldByUserAndMenuReq = new QueryFieldByUserAndMenuReq();
         queryFieldByUserAndMenuReq.setId(user.getId());
         queryFieldByUserAndMenuReq.setRoleId(user.getRoleList().get(0).getId());
         queryFieldByUserAndMenuReq.setMenuCode("financing:balanceaccountManager");
         JSONResult<List<UserFieldDTO>> queryFieldByUserAndMenu =
                 customFieldFeignClient.queryFieldByUserAndMenu(queryFieldByUserAndMenuReq);
         request.setAttribute("userFieldList", queryFieldByUserAndMenu.getData());
      // 查询签约店型集合
         request.setAttribute("vistitStoreTypeList",
                 getDictionaryByCode(DicCodeEnum.VISITSTORETYPE.getCode()));
         return "financing/balanceAccountPage";
    }
    
    /***
     * 对账结算申请列表
     * 
     * @return
     */
    @PostMapping("/applyList")
    @ResponseBody
    @RequiresPermissions("financing:balanceaccountManager:view")
    public JSONResult<PageBean<ReconciliationConfirmDTO>> appayList(
            @RequestBody ReconciliationConfirmPageParam pageParam, HttpServletRequest request) {
        UserInfoDTO user = getUser();
        // 插入当前用户、角色信息
        pageParam.setUserId(user.getId());
        List<RoleInfoDTO> roleList = user.getRoleList();
        if (roleList != null) {

            pageParam.setRoleCode(roleList.get(0).getRoleCode());
        }

        JSONResult<PageBean<ReconciliationConfirmDTO>> list =
                reconciliationConfirmFeignClient.applyList(pageParam);
        return list;
    }
    
    /***
     * 驳回
     * 
     * @return
     */
    @PostMapping("/rejectApply")
    @ResponseBody
 //   @RequiresPermissions("financing:balanceaccountManager:reconciliation")
    @LogRecord(description = "驳回", operationType = OperationType.UPDATE,
            menuName = MenuEnum.REFUNDREBATEAPPLY_MANAGER)
    public JSONResult<Void> rejectApply(@RequestBody ReconciliationConfirmReq req,
            HttpServletRequest request) {
        req.setStatus(AggregationConstant.RECONCILIATION_STATUS.STATUS_1);
        JSONResult<Void> reconciliationConfirm =
                reconciliationConfirmFeignClient.reconciliationConfirm(req);
        return reconciliationConfirm;
    }
    /***
     * 结算确认
     * 
     * @return
     */
    @PostMapping("/settlementConfirm")
    @ResponseBody
  //  @RequiresPermissions("financing:reconciliationConfirmManager:settlement")
    @LogRecord(description = "结算确认", operationType = OperationType.UPDATE,
            menuName = MenuEnum.RECONCILIATIONCONFIRM_MANAGER)
    public JSONResult<Void> settlementConfirm(@RequestBody ReconciliationConfirmReq req,
            HttpServletRequest request) {
        req.setStatus(AggregationConstant.RECONCILIATION_STATUS.STATUS_2);
        JSONResult<Void> reconciliationConfirm =
                reconciliationConfirmFeignClient.reconciliationConfirm(req);
        return reconciliationConfirm;
    }
    /**
     * 获取当前登录账号
     * 
     * @param orgDTO
     * @return
     */
    private UserInfoDTO getUser() {
        Object attribute = SecurityUtils.getSubject().getSession().getAttribute("user");
        UserInfoDTO user = (UserInfoDTO) attribute;
        return user;
    }
    /**
     * 根据机构和角色类型获取用户
     * 
     * @param orgDTO
     * @return
     */
    private List<UserInfoDTO> getUserList(Long orgId, String roleCode, List<Integer> statusList) {
        UserOrgRoleReq userOrgRoleReq = new UserOrgRoleReq();
        userOrgRoleReq.setOrgId(orgId);
        userOrgRoleReq.setRoleCode(roleCode);
        userOrgRoleReq.setStatusList(statusList);
        JSONResult<List<UserInfoDTO>> listByOrgAndRole =
                userInfoFeignClient.listByOrgAndRole(userOrgRoleReq);
        return listByOrgAndRole.getData();
    }
    /**
     * 获取所有组织组
     * 
     * @param orgDTO
     * @return
     */
    private List<OrganizationRespDTO> getOrgList(Long parentId, Integer type) {
        OrganizationQueryDTO queryDTO = new OrganizationQueryDTO();
        queryDTO.setParentId(parentId);
        queryDTO.setOrgType(type);
        // 查询所有组织
        JSONResult<List<OrganizationRespDTO>> queryOrgByParam =
                organizationFeignClient.queryOrgByParam(queryDTO);
        List<OrganizationRespDTO> data = queryOrgByParam.getData();
        return data;
    }
    /**
     * 查询字典表
     * 
     * @param code
     * @return
     */
    private List<DictionaryItemRespDTO> getDictionaryByCode(String code) {
        JSONResult<List<DictionaryItemRespDTO>> queryDicItemsByGroupCode =
                dictionaryItemFeignClient.queryDicItemsByGroupCode(code);
        if (queryDicItemsByGroupCode != null
                && JSONResult.SUCCESS.equals(queryDicItemsByGroupCode.getCode())) {
            return queryDicItemsByGroupCode.getData();
        }
        return null;
    }
    /***
     * 下载模板
     * @param queryDTO
     * @return
     */
    @PostMapping("/downBalanceAccount")
    @ResponseBody
    public void downBalanceAccount(@RequestBody PayDetailAccountDTO queryDTO) {
    	JSONResult<PayDetailAccountDTO> jsonResult = balanceAccountApplyClient.getPayDetailById(queryDTO);
    	Map dataMap= new HashMap<>();
    	if (JSONResult.SUCCESS.equals(jsonResult.getCode()) && jsonResult.getData() !=null) {
    		List<DictionaryItemRespDTO> dictionaryItemRespDTOs = getDictionaryByCode(DicCodeEnum.VISITSTORETYPE.getCode());
    		
    		PayDetailAccountDTO accountDTO = jsonResult.getData();
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		dataMap.put("signShopType", "");
    		//去字典表查询签约店型
    		if(dictionaryItemRespDTOs !=null && dictionaryItemRespDTOs.size()>0) {
    			for (DictionaryItemRespDTO dictionaryItemRespDTO : dictionaryItemRespDTOs) {
					if(dictionaryItemRespDTO.getValue().equals(accountDTO.getSignShopType())) {
						dataMap.put("signShopType", dictionaryItemRespDTO.getName());
					}
				}
    		}
    		String payMode = "";
    		if(accountDTO.getPayMode() ==1) {
    			payMode = "现金";
    		}else if(accountDTO.getPayMode() ==2) {
    			payMode = "POS";
    		}else if(accountDTO.getPayMode() ==3) {
    			payMode = "转账";
    		}
    		String payType = "";
    		if(accountDTO.getPayType() ==1) {
    			payType = "全款";
    		}else if(accountDTO.getPayType() ==2) {
    			payType = "定金";
    		}else if(accountDTO.getPayType() ==3) {
    			payType = "追加定金";
    		}else if(accountDTO.getPayType() ==4) {
    			payType = "尾款";
    		}
    		String createTime = sdf.format(accountDTO.getPayTime());
    		dataMap.put("year", createTime.substring(0, 3));
            dataMap.put("month", createTime.substring(5, 6));
            dataMap.put("day", createTime.substring(8, 9));
            dataMap.put("statementNo", accountDTO.getStatementNo());
            dataMap.put("cueName", accountDTO.getCusName());
            dataMap.put("phone", accountDTO.getPhone());
            dataMap.put("idCard", accountDTO.getIdCard());
            dataMap.put("projectName", accountDTO.getProjectName());
            dataMap.put("area", accountDTO.getSignProvince()+accountDTO.getSignCity()+accountDTO.getSignDictrict());
            dataMap.put("companyName", accountDTO.getCompanyName());
            dataMap.put("payMode", payMode);
            dataMap.put("payType", payType);
            dataMap.put("amountReceived", accountDTO.getAmountReceived()==null?"":(accountDTO.getAmountReceived()+""));
            dataMap.put("businessManager",accountDTO.getBusinessManagerName());
            dataMap.put("busAreaName", accountDTO.getBusAreaName());
            dataMap.put("teleDeptName", accountDTO.getTeleDeptName());
            dataMap.put("teleGorupName", accountDTO.getTeleGorupName());
            dataMap.put("teleSaleName", accountDTO.getTeleSaleName());
            dataMap.put("signAmountReceivable", accountDTO.getSignAmountReceivable()==null?"":(accountDTO.getSignAmountReceivable()+"") );
            if(accountDTO.getPayType() ==1 || accountDTO.getPayType()==2) {
            	dataMap.put("firstToll", accountDTO.getFirstToll()==null?"":(accountDTO.getFirstToll()+""));
            	dataMap.put("preferentialAmount", accountDTO.getPreferentialAmount()==null?"":(accountDTO.getPreferentialAmount()+"") );
            }else {
            	dataMap.put("firstToll", "");
            	dataMap.put("preferentialAmount", "");
            }	
            dataMap.put("settlementMoney", accountDTO.getSettlementMoney());
            
            dataMap.put("amountPerformance", accountDTO.getAmountPerformance());
            dataMap.put("ID", "111111111111111111");
    	}
    	down(dataMap);
    }
    /**
     * 注意dataMap里存放的数据Key值要与模板中的参数相对应 
     * @param dataMap
     * 
     */
    @SuppressWarnings("unchecked")
    private void getData(Map dataMap) {
        //       dataMap.put("image", getImageStr());
    	dataMap.put("year", "2018");
        dataMap.put("month", "04");
        dataMap.put("day", "13");
        dataMap.put("statementNo", "fad32586fs");
        dataMap.put("cueName", "王五");
        dataMap.put("phone", "17300000000");
        dataMap.put("ID", "111111111111111111");
        dataMap.put("projectName", "麻辣烫");
        dataMap.put("area", "北京");
        dataMap.put("companyName", "快道科技有限公司");
        dataMap.put("signShopType", "哈哈");
        dataMap.put("payMode", "全款");
        dataMap.put("payType", "POS");
        dataMap.put("amountReceived", "60000.00");
        dataMap.put("businessManager","张三");
    }
    
    public void down(Map dataMap) {
       // 设置模本装置方法和路径,FreeMarker支持多种模板装载方法。可以重servlet，classpath，数据库装载，  
       // 这里我们的模板是放在com.ftl包下面  
       configuration.setClassForTemplateLoading(this.getClass(),"/excel-templates");
       Template t = null;
       // 输出文档路径及名称
       File outFile = new File("D:/test.doc");
       Writer out = null;

       try {
           // test.ftl为要装载的模板 
           t = configuration.getTemplate("04133.ftl");
           t.setEncoding("utf-8");

           out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));
           t.process(dataMap, out);
           out.close();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
    public static void wordToHtml(String filePath, String outPutFilePath, String newFileName)
            throws Exception {
 
        String substring = filePath.substring(filePath.lastIndexOf(".") + 1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        outPutFilePath = getRealPath(outPutFilePath);
        filePath = getRealPath(filePath);
 
        //防止错误输入
        if(!outPutFilePath.endsWith("/") && !outPutFilePath.endsWith("\\")) {
            outPutFilePath = outPutFilePath + "/";
        }
 
        /*
         * word2007和word2003的构建方式不同，
         * 前者的构建方式是xml，后者的构建方式是dom树。
         * 文件的后缀也不同，前者后缀为.docx，后者后缀为.doc
         * 相应的，apache.poi提供了不同的实现类。
         */
        if ("docx".equals(substring)) {
 
 
            InputStream inputStream = new FileInputStream(new File(filePath));
            XWPFDocument document = new XWPFDocument(inputStream);
 
            //step 2 : prepare XHTML options
            final String imageUrl = "";
 
            XHTMLOptions options = XHTMLOptions.create();
            options.setExtractor(new FileImageExtractor(new File(outPutFilePath + imageUrl)));
            options.setIgnoreStylesIfUnused(false);
            options.setFragment(true);
            //    			@Override 重写的方法，加上这个报错，你看看是啥问题
            options.URIResolver(uri -> imageUrl + uri);
 
            //step 3 : convert XWPFDocument to XHTML
            XHTMLConverter.getInstance().convert(document, out, options);
        } else {
            HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(filePath));
            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                    DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .newDocument());
            wordToHtmlConverter.setPicturesManager((content, pictureType, suggestedName, widthInches, heightInches) -> suggestedName);
            wordToHtmlConverter.processDocument(wordDocument);
            //save pictures
            List pics = wordDocument.getPicturesTable().getAllPictures();
            if (pics != null) {
                for (int i = 0; i < pics.size(); i++) {
                    Picture pic = (Picture) pics.get(i);
                    System.out.println();
                    try {
                        pic.writeImageContent(new FileOutputStream(outPutFilePath
                                + pic.suggestFullFileName()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            Document htmlDocument = wordToHtmlConverter.getDocument();
            DOMSource domSource = new DOMSource(htmlDocument);
            StreamResult streamResult = new StreamResult(out);
 
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
        }
 
        out.close();
        writeFile(new String(out.toByteArray()), outPutFilePath + newFileName);
    }
    public static String getRealPath(String dirPath) {
        //利用资源加载器获取资源URL
        String path = Class.class.getResource("/").getPath();
        return path + dirPath;

    }
    private static void writeFile(String content, String path) {
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(content);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
