package springboot.dao2;
 
import org.springframework.stereotype.Component; 

import java.util.List;
import java.util.Map;

/**
 * ucml自定义sql查询（多表关联  查询比较自由）
 * @author admin
 *
 */

@Component
public interface UcmlMapper {
	Integer insert(String statement);

    Integer delete(String statement);

    Integer update(String statement);

    List<Map<String, Object>> selectList(String statement);

    String selectOne(String statement);
}