package springboot.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;
 
import springboot.modal.vo.BohVo;
import springboot.modal.vo.BohVoExample; 

/**
 * @author tangyx
 * @date 2021.4.15
 */
public interface IUcmlService {
	
	Integer insert(String statement);

    Integer delete(String statement);

    Integer update(String statement);

    List<Map<String, Object>> selectList(String statement);

    String selectOne(String statement);
}
