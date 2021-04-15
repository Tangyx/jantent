package springboot.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import springboot.dao.BohVoMapper;
import springboot.dao.CommentVoMapper;
import springboot.dao2.UcmlMapper;
import springboot.exception.TipException;
import springboot.modal.bo.CommentBo;
import springboot.modal.vo.BohVo;
import springboot.modal.vo.BohVoExample;
import springboot.modal.vo.CommentVo;
import springboot.modal.vo.CommentVoExample;
import springboot.modal.vo.ContentVo;
import springboot.service.IBohService;
import springboot.service.ICommentService;
import springboot.service.IContentService;
import springboot.service.IUcmlService;
import springboot.util.DateKit;
import springboot.util.MyUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tangyx
 * @date 2021.4.14
 */
@Service
public class UcmlService implements IUcmlService {
    @Resource
    private UcmlMapper ucml;

    /**
     * copy原有的分页信息，除数据
     *
     * @param ordinal
     * @param <T>
     * @return
     */
    private <T> PageInfo<T> copyPageInfo(PageInfo ordinal) {
        PageInfo<T> returnBo = new PageInfo<T>();
        returnBo.setPageSize(ordinal.getPageSize());
        returnBo.setPageNum(ordinal.getPageNum());
        returnBo.setEndRow(ordinal.getEndRow());
        returnBo.setTotal(ordinal.getTotal());
        returnBo.setHasNextPage(ordinal.isHasNextPage());
        returnBo.setHasPreviousPage(ordinal.isHasPreviousPage());
        returnBo.setIsFirstPage(ordinal.isIsFirstPage());
        returnBo.setIsLastPage(ordinal.isIsLastPage());
        returnBo.setNavigateFirstPage(ordinal.getNavigateFirstPage());
        returnBo.setNavigateLastPage(ordinal.getNavigateLastPage());
        returnBo.setNavigatepageNums(ordinal.getNavigatepageNums());
        returnBo.setSize(ordinal.getSize());
        returnBo.setPrePage(ordinal.getPrePage());
        returnBo.setNextPage(ordinal.getNextPage());
        return returnBo;
    }

	@Override
	public Integer insert(String statement) { 
		Integer insert = ucml.insert(statement);
		return insert;
	}

	@Override
	public Integer delete(String statement) {
		Integer delete = ucml.delete(statement);
		return delete;
	}

	@Override
	public Integer update(String statement) {
		Integer update = ucml.update(statement);
		return update;
	}

	@Override
	public List<Map<String, Object>> selectList(String statement) {
		List<Map<String, Object>> selectList = ucml.selectList(statement);
		return selectList;
	}

	@Override
	public String selectOne(String statement) {
		String selectOne = ucml.selectOne(statement);
		return selectOne;
	}
    
	
}
