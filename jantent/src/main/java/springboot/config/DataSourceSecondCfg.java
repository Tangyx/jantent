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

@Configuration
@MapperScan(basePackages="springboot.dao2",sqlSessionTemplateRef="secondSqlSessionTemplate")
public class DataSourceSecondCfg {

	@Value("${spring.datasource.second.url}")
	private String url;
	@Value("${spring.datasource.second.username}")
	private String username;
	@Value("${spring.datasource.second.password}")
	private String password;
	@Value("${spring.datasource.second.driverClassName}")
	private String driverClassName;
	/**本数据源扫描的mapper路径*/
	static final String MAPPER_LOCATION = "classpath:mapper/second/*.xml"; 
	
	/**创建数据源*/
	@Bean(name = "secondDS")
	public DataSource getSecondDataSource() {
		DataSource build =  DataSourceBuilder.create()
				.driverClassName(driverClassName)
				.url(url)
				.username(username)
				.password(password)
				.build();
		return build;
	}


	/**创建SessionFactory*/
	@Bean(name = "secondSqlSessionFactory")
	public SqlSessionFactory secondSqlSessionFactory(@Qualifier("secondDS") DataSource dataSource) throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		//设置mapper配置文件
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(MAPPER_LOCATION));
		return bean.getObject();
	}

	/**创建事务管理器*/
	@Bean("secondTransactionManger")
	public DataSourceTransactionManager secondTransactionManger(@Qualifier("secondDS") DataSource dataSource){
		return new DataSourceTransactionManager(dataSource);
	}

	/**创建SqlSessionTemplate*/
	@Bean(name = "secondSqlSessionTemplate")
	public SqlSessionTemplate secondSqlSessionTemplate(@Qualifier("secondSqlSessionFactory") SqlSessionFactory sqlSessionFactory){
		return new SqlSessionTemplate(sqlSessionFactory);
	} 
}
