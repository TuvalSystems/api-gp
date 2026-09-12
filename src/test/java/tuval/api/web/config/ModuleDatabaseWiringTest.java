package tuval.api.web.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.bind.annotation.RestController;
import tuval.api.web.transacciones.service.tuval.transactionService;
import tuval.api.web.ventas.sincronizacion.service.tuval.GeneralService;

class ModuleDatabaseWiringTest {

  @Configuration
  @EnableTransactionManagement(proxyTargetClass = true)
  static class Transactions {}

  @Test
  void modulesLoadWithBothDatabasesAndKeepAllComponents() throws Exception {
    DataSource dynamics = mock(DataSource.class);
    DataSource tuval = mock(DataSource.class);
    try (var context = createContext(dynamics, tuval)) {
      try (var baseline = getClass().getResourceAsStream("/architecture/components.txt")) {
        assertThat(baseline).isNotNull();
        Set<String> expected = new String(baseline.readAllBytes(), StandardCharsets.UTF_8)
            .lines().filter(line -> !line.isBlank()).collect(Collectors.toSet());
        Set<String> actual = Arrays.stream(context.getBeanDefinitionNames())
            .map(context::getType)
            .filter(type -> type != null)
            .map(org.springframework.util.ClassUtils::getUserClass)
            .filter(type -> type.isAnnotationPresent(RestController.class)
                || type.isAnnotationPresent(Service.class)
                || type.isAnnotationPresent(Repository.class))
            .map(Class::getName).collect(Collectors.toSet());
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
      }
      // Both named and positional JDBC operations must retain their own connection.
      assertThat(context.getBean("dynamicsJdbcTemplate", JdbcTemplate.class).getDataSource())
          .isSameAs(dynamics);
      assertThat(context.getBean("tuvalJdbcTemplate", JdbcTemplate.class).getDataSource())
          .isSameAs(tuval);
      assertThat(context.getBean("dynamicsNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class)
          .getJdbcTemplate().getDataSource()).isSameAs(dynamics);
      assertThat(context.getBean("tuvalNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class)
          .getJdbcTemplate().getDataSource()).isSameAs(tuval);
      // Check the injected connection of every DAO, including fields inherited by proxies.
      for (Object bean : context.getBeansWithAnnotation(Repository.class).values()) {
        Object target = org.springframework.test.util.AopTestUtils.getUltimateTargetObject(bean);
        DataSource expected = target.getClass().getPackageName().contains(".dao.dynamics")
            ? dynamics : tuval;
        org.springframework.util.ReflectionUtils.doWithFields(target.getClass(), field -> {
          org.springframework.util.ReflectionUtils.makeAccessible(field);
          Object value = field.get(target);
          if (value instanceof JdbcTemplate template) {
            assertThat(template.getDataSource()).as(target.getClass().getName()).isSameAs(expected);
          } else if (value instanceof NamedParameterJdbcTemplate template) {
            assertThat(template.getJdbcTemplate().getDataSource())
                .as(target.getClass().getName()).isSameAs(expected);
          }
        });
      }
      verifyNoInteractions(dynamics, tuval);
    }
  }

  @Test
  void tuvalOperationsRollbackWithTuvalEvenThoughDynamicsIsPrimary() {
    try (var context = createContext(mock(DataSource.class), mock(DataSource.class))) {
      var tuval = context.getBean("tuvalTransactionManager", PlatformTransactionManager.class);
      var dynamics = context.getBean("dynamicsTransactionManager", PlatformTransactionManager.class);
      var service = context.getBean(GeneralService.class);
      assertThatThrownBy(() -> service.processMassiveHeaderUpdate(null))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> service.processMassiveDetailUpdate(null))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> context.getBean(transactionService.class).storeReceivedL(null))
          .isInstanceOf(NullPointerException.class);
      verify(tuval, times(3)).getTransaction(any());
      verify(tuval, times(3)).rollback(any());
      verify(tuval, never()).commit(any());
      verifyNoInteractions(dynamics);
    }
  }

  @Test
  void quoteUpdatesUseDynamicsAndRollbackOnFailure() {
    try (var context = createContext(mock(DataSource.class), mock(DataSource.class))) {
      var tuval = context.getBean("tuvalTransactionManager", PlatformTransactionManager.class);
      var dynamics = context.getBean("dynamicsTransactionManager", PlatformTransactionManager.class);
      assertThatThrownBy(() -> context.getBean(GeneralService.class)
          .processMassiveQuoteHeaderUpdate(null)).isInstanceOf(IllegalArgumentException.class);
      verify(dynamics).getTransaction(any());
      verify(dynamics).rollback(any());
      verify(dynamics, never()).commit(any());
      verifyNoInteractions(tuval);
    }
  }

  private AnnotationConfigApplicationContext createContext(DataSource dynamics, DataSource tuval) {
    var context = new AnnotationConfigApplicationContext();
    var dynamicsConfig = new DynamicsJpaConfig();
    var tuvalConfig = new TuvalJpaConfig();
    context.registerBean("dynamicsJdbcTemplate", JdbcTemplate.class,
        () -> dynamicsConfig.dynamicsJdbcTemplate(dynamics), definition -> definition.setPrimary(true));
    context.registerBean("tuvalJdbcTemplate", JdbcTemplate.class,
        () -> tuvalConfig.tuvalJdbcTemplate(tuval));
    context.registerBean("dynamicsNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class,
        () -> dynamicsConfig.dynamicsNamedParameterJdbcTemplate(dynamics), definition -> definition.setPrimary(true));
    context.registerBean("tuvalNamedParameterJdbcTemplate", NamedParameterJdbcTemplate.class,
        () -> tuvalConfig.tuvalNamedParameterJdbcTemplate(tuval));
    context.registerBean("dynamicsTransactionManager", PlatformTransactionManager.class,
        this::transactionManager, definition -> definition.setPrimary(true));
    context.registerBean("tuvalTransactionManager", PlatformTransactionManager.class, this::transactionManager);
    context.registerBean(ObjectMapper.class, () -> new ObjectMapper().findAndRegisterModules());
    context.register(Transactions.class);
    var scanner = new ClassPathBeanDefinitionScanner(context, false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
    scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
    scanner.addIncludeFilter(new AnnotationTypeFilter(Repository.class));
    scanner.scan("tuval.api.web");
    context.refresh();
    return context;
  }

  private PlatformTransactionManager transactionManager() {
    var manager = mock(PlatformTransactionManager.class);
    when(manager.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
    return manager;
  }
}
