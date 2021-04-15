package springboot.dao;
 
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import springboot.modal.vo.BohVo;
import springboot.modal.vo.BohVoExample; 

@Component
public interface BohVoMapper { 

	long countByExample(BohVoExample example);

    int deleteByExample(BohVoExample example);

    int deleteByPrimaryKey(Integer uid);

    int insert(BohVo record);

    int insertSelective(BohVo record);

    List<BohVo> selectByExample(BohVoExample example);
    
    List<BohVo> selectAll();

    BohVo selectByPrimaryKey(Integer uid);

    int updateByExampleSelective(@Param("record") BohVo record, @Param("example") BohVoExample example);

    int updateByExample(@Param("record") BohVo record, @Param("example") BohVoExample example);

    int updateByPrimaryKeySelective(BohVo record);

    int updateByPrimaryKey(BohVo record);
}