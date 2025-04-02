package tuval.api.web.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "tuval.api.web.models.dao.dynamics", 
entityManagerFactoryRef = "dynamicsEntityManagerFactory", 
transactionManagerRef = "dynamicsTransactionManager")
public class DynamicsJpaConfig {

    @Primary
    @Bean(name = "dynamicsDataSource")
    @ConfigurationProperties(prefix = "dynamics.datasource")
    public DataSource dynamicsDataSource() {
        return DataSourceBuilder.create().build();
    }


    //para las conexiones sp de dynamics
    @Primary
    @Bean(name = "dynamicsJdbcTemplate")
    public JdbcTemplate dynamicsJdbcTemplate(@Qualifier("dynamicsDataSource") DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setFetchSize(100); // Configuración opcional para mejor rendimiento
        return template;
    }

    @Primary
    @Bean(name = "dynamicsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dynamicsEntityManagerFactory(EntityManagerFactoryBuilder builder,
            @Qualifier("dynamicsDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        properties.put("hibernate.show_sql", "true");
        return builder.dataSource(dataSource).properties(properties)
        .packages("tuval.api.web.models.entity.dynamics")
        .persistenceUnit("dynamicsPU").build();
    }

    @Primary
    @Bean(name = "dynamicsTransactionManager")
    public PlatformTransactionManager dynamicsTransactionManager(
            @Qualifier("dynamicsEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    //para las conexinexiones en las entidades de dynamics
    @Primary
    @Bean(name = "dynamicsEntityManager")
    public EntityManager dynamicsEntityManager(
            @Qualifier("dynamicsEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

}
