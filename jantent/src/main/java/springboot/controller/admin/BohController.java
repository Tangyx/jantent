package springboot.controller.admin;
  
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.tags.EditorAwareTag;

import com.github.pagehelper.PageInfo;

import io.netty.handler.codec.http.HttpRequest;
import springboot.controller.AbstractController;
import springboot.controller.helper.ExceptionHelper;
import springboot.exception.TipException;
import springboot.modal.bo.RestResponseBo;
import springboot.modal.vo.BohVo; 
import springboot.service.IBohService;

@Controller
@RequestMapping("/admin/boh")
public class BohController extends AbstractController{

	private static final Logger logger = LoggerFactory.getLogger(BohController.class);
	
	@Resource
	private IBohService service;
	
	@GetMapping(value = "")
    public String index(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "15") int limit, HttpServletRequest request) {
        //BohVoExample bohVoExample = new BohVoExample();
        PageInfo<BohVo> comments = service.getComments(page,limit);
        request.setAttribute("comments", comments);
        return "admin/boh_list";
    }

    @PostMapping(value = "delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam Integer id) {
        try { 
            service.deleteByid(id);
        } catch (Exception e) {
            String msg = "数据删除失败";
            return ExceptionHelper.handlerException(logger, msg, e);
        }
        return RestResponseBo.ok();
    }
    
    @GetMapping(value="add")
    public String add() {
		return "admin/boh_edit";
	}
    
    @GetMapping(value="/{id}")
    public String edit(HttpServletRequest request,@PathVariable Integer id) {
    	BohVo vo = service.queryUserById(id);
    	request.setAttribute("contents", vo);
    	return "admin/boh_edit";
    }
    
    
    @PostMapping("publish")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo publish(@RequestParam String name,@RequestParam int age,
    		@RequestParam String tel ,
    		@RequestParam String  mail ,
    		@RequestParam String  address , HttpServletRequest request ) {
    	
    	BohVo vo = new BohVo();
    	vo.setAddress(address);
    	vo.setAge(age);
    	vo.setCreatetime(new Date());
    	vo.setMail(mail);
    	vo.setName(name);
    	vo.setTel(tel);
    	try {
    		service.insertUser(vo);
    	}catch (Exception e) {
			String msg = "数据保存失败";
			return ExceptionHelper.handlerException(logger, msg, e);
		}
    	return RestResponseBo.ok();
    }

    @PostMapping("modify")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo modify(
    		@RequestParam Integer id,
    		@RequestParam String name,@RequestParam int age,
    		@RequestParam String tel ,
    		@RequestParam String  mail ,
    		@RequestParam String  address , HttpServletRequest request) {
    	
    	BohVo vo = new BohVo();
    	vo.setId(id);
    	vo.setAddress(address);
    	vo.setAge(age);
    	vo.setCreatetime(new Date());
    	vo.setMail(mail);
    	vo.setName(name);
    	vo.setTel(tel);
    	try {
			service.updateByUid(vo);
		} catch (Exception e) {
			String msg = "数据更新失败";
			return ExceptionHelper.handlerException(logger, msg, e);
		}
    	
    	return RestResponseBo.ok();
	}
}
