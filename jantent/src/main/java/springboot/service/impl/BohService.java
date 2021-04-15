package springboot.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import springboot.dao.BohVoMapper;
import springboot.dao.CommentVoMapper;
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
import springboot.util.DateKit;
import springboot.util.MyUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyx
 * @date 2021.4.14
 */
@Service
public class BohService implements IBohService {
    @Resource
    private BohVoMapper bohDao;

    

    /**
     * 检查评论输入数据
     *
     * @param comments
     * @throws TipException
     */
    private void checkComment(BohVo comments) throws TipException {
        if (null == comments) {
            throw new TipException("数据对象为空");
        }
        if (StringUtils.isBlank(comments.getName())) {
            comments.setName("热心网友");
        }
        if (StringUtils.isNotBlank(comments.getMail()) && !MyUtils.isEmail(comments.getMail())) {
            throw new TipException("请输入正确的邮箱格式");
        }
    }
    
	@Override
	public Integer insertUser(BohVo BohVo) {
		checkComment(BohVo);
		bohDao.insert(BohVo);
		return 1;
	}

	@Override
	public BohVo queryUserById(Integer uid) {
		BohVo selectByPrimaryKey = bohDao.selectByPrimaryKey(uid);
		return selectByPrimaryKey;
	}

	@Override
	public PageInfo<BohVo> getCommentsWithPage(BohVoExample bohVoExample, int page, int limit) {
		PageHelper.startPage(page, limit);
		List<BohVo> selectByExample = bohDao.selectByExample(bohVoExample);
		return new PageInfo<>(selectByExample);
	}

	@Override
	public void updateByUid(BohVo BohVo) {
		bohDao.updateByPrimaryKey(BohVo);
	}

	@Override
	public PageInfo<BohVo> getComments(int page, int limit) {
		PageHelper.startPage(page, limit);
		List<BohVo> selectAll = bohDao.selectAll();
		return new PageInfo<>(selectAll);
	}

	@Override
	public void deleteByid(Integer id) {
		bohDao.deleteByPrimaryKey(id);
		
	}
}
