package springboot.service;

import com.github.pagehelper.PageInfo;
 
import springboot.modal.vo.BohVo;
import springboot.modal.vo.BohVoExample; 

/**
 * @author tangyx
 * @date 2021.4.14
 */
public interface IBohService {
    /**
     * 保存用户数据
     *
     * @param BohVo 用户数据
     * @return 主键
     */

    Integer insertUser(BohVo BohVo);

    /**
     * 通过uid查找对象
     * @param uid
     * @return
     */
    BohVo queryUserById(Integer uid);
 
    /**
     *  
     *
     * @param cid
     * @param page
     * @param limit
     * @return CommentBo
     */
    PageInfo<BohVo> getComments(int page, int limit);

    /**
     *  
     *
     * @param BohVoExample
     * @param page
     * @param limit
     * @return CommentVo
     */
    PageInfo<BohVo> getCommentsWithPage(BohVoExample bohVoExample, int page, int limit);
    /**
     * 根据主键更新user对象
     * @param BohVo
     * @return
     */
    void updateByUid(BohVo BohVo);
    
    void deleteByid(Integer id);
}
