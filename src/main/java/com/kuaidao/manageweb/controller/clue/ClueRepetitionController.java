package com.kuaidao.manageweb.controller.clue;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kuaidao.aggregation.dto.clue.ClueRepetitionDTO;
import com.kuaidao.common.constant.RoleCodeEnum;
import com.kuaidao.common.entity.IdEntity;
import com.kuaidao.common.entity.JSONResult;
import com.kuaidao.common.entity.PageBean;
import com.kuaidao.manageweb.feign.clue.ClueRepetitionFeignClient;
import com.kuaidao.manageweb.feign.user.UserInfoFeignClient;
import com.kuaidao.sys.dto.organization.OrganizationDTO;
import com.kuaidao.sys.dto.role.RoleInfoDTO;
import com.kuaidao.sys.dto.user.UserInfoDTO;
import com.kuaidao.sys.dto.user.UserOrgRoleReq;

/**
* 重单管理
* Created  on 2019-2-18 16:15:59
*/
@Controller
@RequestMapping("/clue/cluerepetition")
public class ClueRepetitionController {
	private static Logger logger = LoggerFactory.getLogger(ClueRepetitionController.class);
	
	@Autowired
	ClueRepetitionFeignClient clueRepetitionFeignClient;
	
	@Autowired
	private UserInfoFeignClient userInfoFeignClient;
	 /**
     *  重单列表页面
     * 
     * @return
     */
    @RequestMapping("/queryRepeatPage")
    public String queryRepeatPage(HttpServletRequest request) {
		return "clue/repetition/customerrePetitionList";
    }
    
    /**
     * 重单列表
     * 
     * @return
     */
    @RequestMapping("/queryRepeatList")
    @ResponseBody
    public JSONResult<PageBean<ClueRepetitionDTO>> queryRepeatList(HttpServletRequest request,@RequestBody ClueRepetitionDTO clueRepetitionDTO) {
    	UserInfoDTO user = getUser();
		List<RoleInfoDTO> roleList = user.getRoleList();
		List<Long> idList = new ArrayList<Long>();
		if (roleList != null && RoleCodeEnum.DXZJ.name().equals(roleList.get(0).getRoleCode())) {
			// 如果当前登录的为电销总监,查询所有下属电销员工
			List<UserInfoDTO> userList = getUserList(user.getOrgId(), RoleCodeEnum.DXCYGW.name());
			idList = userList.stream().map(c -> c.getId() ).collect(Collectors.toList());
		}else if (roleList != null && RoleCodeEnum.DXCYGW.name().equals(roleList.get(0).getRoleCode())) {
			idList.add(user.getId()) ;
		}
		clueRepetitionDTO.setIdList(idList);
    	JSONResult<PageBean<ClueRepetitionDTO>> list = clueRepetitionFeignClient.queryRepeatList(clueRepetitionDTO);
    	return list;
    }
    /**
	 * 根据机构和角色类型获取用户
	 * 
	 * @param orgDTO
	 * @return
	 */
	private List<UserInfoDTO> getUserList(Long orgId, String roleCode) {
		UserOrgRoleReq userOrgRoleReq = new UserOrgRoleReq();
		userOrgRoleReq.setOrgId(orgId);
		userOrgRoleReq.setRoleCode(roleCode);
		JSONResult<List<UserInfoDTO>> listByOrgAndRole = userInfoFeignClient.listByOrgAndRole(userOrgRoleReq);
		return listByOrgAndRole.getData();
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
     * 重单详情
     * 
     * @return
     */
    @RequestMapping("/queryRepeatById")
    @ResponseBody
    public JSONResult<ClueRepetitionDTO> queryRepeatById(HttpServletRequest request,@RequestBody ClueRepetitionDTO clueRepetitionDTO) {
    	return clueRepetitionFeignClient.queryRepeatById(clueRepetitionDTO);
    }
    
    /**
     * 撤销重单申请
     * 
     * @return
     */
    @RequestMapping("/delRepeatByIds")
    @ResponseBody
    public JSONResult<PageBean<ClueRepetitionDTO>> delRepeatByIds(HttpServletRequest request,@RequestBody ClueRepetitionDTO clueRepetitionDTO) {
    	JSONResult<PageBean<ClueRepetitionDTO>> list = clueRepetitionFeignClient.queryRepeatList(clueRepetitionDTO);
    	return list;
    }
    
    
    /**
     *  重单处理列表页面
     * 
     * @return
     */
    @RequestMapping("/dealPetitionListPage")
    public String dealPetitionListPage(HttpServletRequest request) {
		return "clue/repetition/dealPetitionList";
    } 
    
    /**
     * 重单处理列表
     * 
     * @return
     */
    @RequestMapping("/dealPetitionList")
    @ResponseBody
    public JSONResult<PageBean<ClueRepetitionDTO>> dealPetitionList(HttpServletRequest request,@RequestBody ClueRepetitionDTO clueRepetitionDTO) {
    	JSONResult<PageBean<ClueRepetitionDTO>> list = clueRepetitionFeignClient.dealPetitionList(clueRepetitionDTO);
    	return list;
    }
    
    /**
     * 处理重单
     * 
     * @return
     */
    @RequestMapping("/dealPetitionById")
    @ResponseBody
    public JSONResult<PageBean<ClueRepetitionDTO>> dealPetitionById(HttpServletRequest request,@RequestBody ClueRepetitionDTO clueRepetitionDTO) {
    	JSONResult<PageBean<ClueRepetitionDTO>> list = clueRepetitionFeignClient.dealPetitionById(clueRepetitionDTO);
    	return list;
    }
    
    /**
     * 重单审核
     * 
     * @return
     */
    @RequestMapping("/updatePetitionById")
    @ResponseBody
    public JSONResult updatePetitionById(HttpServletRequest request,@RequestBody ClueRepetitionDTO clueRepetitionDTO) {
    	JSONResult<PageBean<ClueRepetitionDTO>> list = clueRepetitionFeignClient.updatePetitionById(clueRepetitionDTO);
    	return list;
    }
 
}
