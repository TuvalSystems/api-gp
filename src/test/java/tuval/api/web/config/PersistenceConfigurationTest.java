package tuval.api.web.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManagerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.sql.DataSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

class PersistenceConfigurationTest {

  @ParameterizedTest
  @ValueSource(strings = {"dynamics", "tuval"})
  void persistenceUnitsKeepTheirOwnPackagesAndConnections(String database) throws Exception {
    var configClass = database.equals("dynamics") ? DynamicsJpaConfig.class : TuvalJpaConfig.class;
    var repositories = configClass.getAnnotation(EnableJpaRepositories.class);
    assertThat(repositories.entityManagerFactoryRef()).isEqualTo(database + "EntityManagerFactory");
    assertThat(repositories.transactionManagerRef()).isEqualTo(database + "TransactionManager");
    assertThat(configClass.getMethod(database + "DataSource")
        .getAnnotation(ConfigurationProperties.class).prefix()).isEqualTo(database + ".datasource");
    try (var resource = getClass().getResourceAsStream("/architecture/" + database + "-repositories.txt")) {
      assertThat(resource).isNotNull();
      String[] expected = new String(resource.readAllBytes(), StandardCharsets.UTF_8).lines().toArray(String[]::new);
      assertThat(repositories.basePackages()).containsExactlyInAnyOrder(expected);
      assertThat(Arrays.stream(repositories.basePackages()))
          .allMatch(name -> name.contains(".dao." + database));
    }

    var source = mock(DataSource.class);
    var builder = mock(EntityManagerFactoryBuilder.class);
    var unit = mock(EntityManagerFactoryBuilder.Builder.class, RETURNS_SELF);
    var factoryBean = mock(LocalContainerEntityManagerFactoryBean.class);
    when(builder.dataSource(source)).thenReturn(unit);
    when(unit.build()).thenReturn(factoryBean);
    LocalContainerEntityManagerFactoryBean actual;
    var factory = mock(EntityManagerFactory.class);
    JpaTransactionManager manager;
    if (database.equals("dynamics")) {
      var config = new DynamicsJpaConfig();
      actual = config.dynamicsEntityManagerFactory(builder, source);
      manager = (JpaTransactionManager) config.dynamicsTransactionManager(factory);
    } else {
      var config = new TuvalJpaConfig();
      actual = config.tuvallEntityManagerFactoryBean(builder, source);
      manager = (JpaTransactionManager) config.tuvalTransactionManager(factory);
    }
    assertThat(actual).isSameAs(factoryBean);
    assertThat(manager.getEntityManagerFactory()).isSameAs(factory);
    verify(builder).dataSource(source);
    verify(unit).persistenceUnit(database + "PU");
    var packages = ArgumentCaptor.forClass(String[].class);
    verify(unit).packages(packages.capture());
    try (var resource = getClass().getResourceAsStream("/architecture/" + database + "-entities.txt")) {
      assertThat(resource).isNotNull();
      String[] expected = new String(resource.readAllBytes(), StandardCharsets.UTF_8).lines().toArray(String[]::new);
      assertThat(packages.getValue()).containsExactlyInAnyOrder(expected);
    }
    verifyNoInteractions(source);
  }
}
