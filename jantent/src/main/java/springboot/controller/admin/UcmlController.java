package springboot.controller.admin;
  
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import springboot.controller.AbstractController;
import springboot.controller.helper.ExceptionHelper;
import springboot.exception.TipException;
import springboot.modal.bo.RestResponseBo;
import springboot.service.IUcmlService;
import springboot.util.UcmlPageInfoUtils;

@Controller
@RequestMapping("/admin/ucml")
public class UcmlController extends AbstractController{

	private static final Logger logger = LoggerFactory.getLogger(UcmlController.class);
	
	@Resource
	private IUcmlService service;
	
	@GetMapping(value = "/user")
    public String index(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "15") int limit, HttpServletRequest request) {
		int start = 1;
		int end = limit;
		if(page > 1) start = (page-1)*limit+1; end = page*limit; 
		String sql = "select UCML_UserOID,USR_LOGIN,PassWord,EmployeeName,row from( " + 
				"select UCML_UserOID,USR_LOGIN,PassWord,EmployeeName,ROW_NUMBER() over(order by USR_LOGIN ) as row from UCML_User " + 
				") t  ";
		//使用 pagehelper 设置分页的配置，两参数，一个是起始页页数。一个是一页分多少条数据
		//查出数据（这里要注意的是， pagehelper 下面紧跟着的查询出来的数据结果才有分页的效果）
		PageHelper.startPage(page, limit);
		List<Map<String, Object>> selectList = service.selectList(sql);
		PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(selectList);
		//实现了分页功能  2021.4.15
		//原sql查询所有 后sql增加分页功能 并对pageinfo的值做新的计算 
		sql += "where row between '"+start+"' and '"+end+"'";
		List<Map<String, Object>> selectList2 = service.selectList(sql);
		
		//给pageinfo增加相应的属性信息  对自由的sql操作
		pageInfo = UcmlPageInfoUtils.initUcmlpageInfo(selectList, pageInfo, page, limit, selectList2);
		
		request.setAttribute("comments", pageInfo);
		System.out.println(pageInfo);
        return "admin/ucml_list";
    }

    @PostMapping(value = "delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam Integer id) {
        try { 
            //service.deleteByid(id);
        } catch (Exception e) {
            String msg = "数据删除失败";
            return ExceptionHelper.handlerException(logger, msg, e);
        }
        return RestResponseBo.ok();
    }
    
}
