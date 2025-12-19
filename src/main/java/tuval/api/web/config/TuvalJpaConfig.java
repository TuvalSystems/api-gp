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
@EnableJpaRepositories(basePackages = "tuval.api.web.models.dao.tuval", entityManagerFactoryRef = "tuvalEntityManagerFactory", transactionManagerRef = "tuvalTransactionManager")
public class TuvalJpaConfig {

    @Bean(name = "tuvalDataSource")
    @ConfigurationProperties(prefix = "tuval.datasource")
    public DataSource tuvalDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "tuvalJdbcTemplate")
    public JdbcTemplate tuvalJdbcTemplate(@Qualifier("tuvalDataSource") DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.setFetchSize(100); // Configuración opcional para mejor rendimiento
        return template;
    }

    @Bean(name = "tuvalEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tuvallEntityManagerFactoryBean(EntityManagerFactoryBuilder builder,
            @Qualifier("tuvalDataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        // properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        properties.put("hibernate.show_sql", "true");
        return builder.dataSource(dataSource).properties(properties).packages("tuval.api.web.models.entity.tuval")
        .persistenceUnit("tuvalPU").build();

    }

    @Bean(name = "tuvalTransactionManager")
    public PlatformTransactionManager tuvalTransactionManager(
            @Qualifier("tuvalEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "tuvalEntityManager")
    public EntityManager tuvalEntityManager(
            @Qualifier("tuvalEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

}
