package springboot.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * 
 * @author admin
 * 多数据源配置
 */

@Configuration
@MapperScan(basePackages="springboot.dao",sqlSessionTemplateRef="firstSqlSessionTemplate")
public class DataSourceFirstCfg {

	@Value("${spring.datasource.first.url}")
	private String url;
	@Value("${spring.datasource.first.username}")
	private String username;
	@Value("${spring.datasource.first.password}")
	private String password;
	@Value("${spring.datasource.first.driverClassName}")
	private String driverClassName;
	/**本数据源扫描的mapper路径*/
	static final String MAPPER_LOCATION = "classpath:mapper/first/*.xml"; 
	
	/**创建数据源*/
	@Bean(name = "firstDS")
	@Primary
	public DataSource getFirstDataSource() {
		DataSource build =  DataSourceBuilder.create()
				.driverClassName(driverClassName)
				.url(url)
				.username(username)
				.password(password)
				.build();
		return build;
	}


	/**创建SessionFactory*/
	@Bean(name = "firstSqlSessionFactory")
	@Primary
	public SqlSessionFactory firstSqlSessionFactory(@Qualifier("firstDS") DataSource dataSource) throws Exception {
		SqlSessionFactoryBean  bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		//设置mapper配置文件
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MAPPER_LOCATION));
		return bean.getObject();
	}

	/**创建事务管理器*/
	@Bean("firstTransactionManger")
	@Primary
	public DataSourceTransactionManager firstTransactionManger(@Qualifier("firstDS") DataSource dataSource){
		return new DataSourceTransactionManager(dataSource);
	}

	/**创建SqlSessionTemplate*/
	@Bean(name = "firstSqlSessionTemplate")
	@Primary
	public SqlSessionTemplate firstSqlSessionTemplate(@Qualifier("firstSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
		return new SqlSessionTemplate(sqlSessionFactory);
	} 
}
