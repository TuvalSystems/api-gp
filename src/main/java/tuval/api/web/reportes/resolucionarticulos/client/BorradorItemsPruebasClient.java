package tuval.api.web.reportes.resolucionarticulos.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException;
import tuval.api.web.reportes.resolucionarticulos.dto.ResolucionBorradorDto;

@Service
public class BorradorItemsPruebasClient {
  private static final int MAX_BYTES = 64 * 1024;
  private final boolean enabled;
  private final URI endpoint;
  private final ObjectMapper mapper;
  private final Duration timeout;
  private final boolean pinDevTarget;
  private static final String DEV_HOST = "tuvalapi.cosenco.ec";

  @Autowired
  public BorradorItemsPruebasClient(ObjectMapper mapper, Environment environment,
      @Value("${reportes.resolucion-borrador.enabled:false}") boolean enabled,
      @Value("${reportes.resolucion-borrador.backend-origin:}") String origin,
      @Value("${reportes.resolucion-borrador.allow-insecure-http:false}") boolean allowHttp,
      @Value("${reportes.resolucion-borrador.dev-test-target-verified:false}") boolean devVerified) {
    this(mapper, entornoDePruebas(environment, origin, devVerified) && enabled, origin, allowHttp,
        Duration.ofSeconds(30), contieneDev(environment));
  }

  // Tests use a loopback server. No system/global HTTP settings are modified.
  BorradorItemsPruebasClient(ObjectMapper mapper, boolean enabled, String origin, boolean allowHttp, Duration timeout) {
    this(mapper, enabled, origin, allowHttp, timeout, false);
  }

  private BorradorItemsPruebasClient(ObjectMapper mapper, boolean enabled, String origin, boolean allowHttp,
      Duration timeout, boolean pinDevTarget) {
    this.pinDevTarget = pinDevTarget;
    this.enabled = enabled;
    this.endpoint = destino(origin, allowHttp);
    this.mapper = mapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    this.timeout = timeout;
  }

  private static boolean contieneDev(Environment environment) {
    return Arrays.stream(environment.getActiveProfiles()).anyMatch("dev"::equalsIgnoreCase);
  }

  private static boolean entornoDePruebas(Environment environment, String origin, boolean devVerified) {
    var profiles = Arrays.stream(environment.getActiveProfiles()).map(p -> p.toLowerCase(Locale.ROOT)).toList();
    if (profiles.isEmpty() || !profiles.stream().allMatch(Set.of("local", "testing", "qa", "staging", "dev")::contains))
      return false;
    if (!contieneDev(environment)) return true;
    if (!devVerified) return false;
    try {
      var uri = URI.create(origin);
      return DEV_HOST.equalsIgnoreCase(uri.getHost()) && (uri.getPort() == -1
          || ("http".equalsIgnoreCase(uri.getScheme()) && uri.getPort() == 80)
          || ("https".equalsIgnoreCase(uri.getScheme()) && uri.getPort() == 443));
    } catch (IllegalArgumentException exception) { return false; }
  }

  // The DEV exception is tied to the verified test backend, not any URL labelled "dev".
  // Pin the socket destination as well as the hostname, retaining Host/SNI and normal TLS verification.
  private static DnsResolver destinoDev() {
    return new DnsResolver() {
      public InetAddress[] resolve(String host) throws UnknownHostException {
        if (!DEV_HOST.equalsIgnoreCase(host)) throw new UnknownHostException();
        return new InetAddress[] { InetAddress.getByAddress(DEV_HOST, new byte[] {(byte)172, 30, 1, 7}) };
      }
      public String resolveCanonicalHostname(String host) throws UnknownHostException {
        if (!DEV_HOST.equalsIgnoreCase(host)) throw new UnknownHostException();
        return DEV_HOST;
      }
    };
  }

  private CloseableHttpClient transporte() {
    var builder = HttpClients.custom().disableAutomaticRetries().disableRedirectHandling()
        .disableCookieManagement().disableAuthCaching().disableContentCompression()
        .setDefaultRequestConfig(RequestConfig.custom().setAuthenticationEnabled(false)
            .setConnectTimeout(Timeout.ofSeconds(5)).setConnectionRequestTimeout(Timeout.ofSeconds(5))
            .setResponseTimeout(Timeout.ofMilliseconds(timeout.toMillis())).build());
    if (pinDevTarget) builder.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
        .setDnsResolver(destinoDev()).build());
    return builder.build();
  }
  private static URI destino(String origin, boolean allowHttp) {
    try {
      var uri = URI.create(origin);
      if (!("https".equalsIgnoreCase(uri.getScheme()) || (allowHttp && "http".equalsIgnoreCase(uri.getScheme())))
          || uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
          || !(uri.getRawPath().isEmpty() || uri.getRawPath().equals("/"))
          || (uri.getPort() != -1 && (uri.getPort() < 1 || uri.getPort() > 65535))) return null;
      return uri.resolve("/cotizaciones/borrador-items");
    } catch (IllegalArgumentException exception) { return null; }
  }

  public void verificarConfiguracion() {
    if (!enabled || endpoint == null) throw new CotizacionBorradorException(503);
  }

  public ResolucionBorradorDto crear(Peticion payload) {
    verificarConfiguracion();
    var request = new HttpGet(endpoint);
    var expired = new AtomicBoolean();
    // Per-operation timer and connection: cancellation includes slow bodies, and no pooled stale retry is possible.
    try (var timer = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon().factory());
         var http = transporte()) {
      request.setHeader("Accept", "application/json");
      request.setHeader("Cache-Control", "no-store");
      request.setEntity(new ByteArrayEntity(mapper.writeValueAsBytes(payload), ContentType.APPLICATION_JSON));
      var cancellation = timer.schedule(() -> { expired.set(true); request.cancel(); }, timeout.toMillis(), TimeUnit.MILLISECONDS);
      try {
        return http.execute(request, response -> {
          int status = response.getCode();
          if (Set.of(401, 403, 422, 429).contains(status)) { request.cancel(); throw new CotizacionBorradorException(status); }
          // This real endpoint creates with 201; 200 is not a verified idempotent replay.
          if (status != 201 || response.getEntity() == null) { request.cancel(); throw new CotizacionBorradorException(502); }
          var type = response.getFirstHeader("Content-Type");
          if (type == null || !"application/json".equalsIgnoreCase(type.getValue().split(";", 2)[0].trim()))
            { request.cancel(); throw new CotizacionBorradorException(502); }
          byte[] bytes;
          try (var stream = response.getEntity().getContent()) {
            bytes = stream.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) { request.cancel(); throw new CotizacionBorradorException(502); }
          }
          var body = mapper.readTree(bytes);
          if (body == null || !body.path("success").isBoolean() || !body.path("success").booleanValue()
              || !body.path("data").isObject()) throw new CotizacionBorradorException(502);
          var result = mapper.treeToValue(body.path("data"), ResolucionBorradorDto.class);
          verificarRespuesta(payload, result);
          if (expired.get()) throw new CotizacionBorradorException(504);
          return result;
        });
      } finally { cancellation.cancel(false); }
    } catch (IOException exception) {
      throw new CotizacionBorradorException(expired.get() || exception instanceof java.net.SocketTimeoutException ? 504 : 502);
    }
  }

  private static void verificarRespuesta(Peticion payload, ResolucionBorradorDto result) {
    if (result == null || result.id() == null || result.id() < 1 || result.numero() == null || result.numero().isBlank()
        || !"borrador_app".equals(result.estado()) || result.usuario() == null || !Long.valueOf(6).equals(result.usuario().id())
        || result.puntoVenta() == null || !payload.pvId().equals(result.puntoVenta().id())
        || result.cliente() == null || !payload.clienteRuc().equals(result.cliente().ruc())
        || result.incluidos() == null || result.incluidos().size() != payload.items().size())
      throw new CotizacionBorradorException(502);
    for (int i = 0; i < payload.items().size(); i++) {
      var expected = payload.items().get(i); var actual = result.incluidos().get(i);
      if (actual == null || !Integer.valueOf(i).equals(actual.indice()) || !expected.codigo().equals(actual.codigo())
          || actual.cantidad() == null || expected.cantidad().compareTo(actual.cantidad()) != 0
          || actual.unidad() == null || actual.unidad().isBlank()) throw new CotizacionBorradorException(502);
    }
  }

  public record Peticion(@JsonProperty("cliente_ruc") String clienteRuc,
      @JsonProperty("pv_id") Long pvId, List<Item> items) {}
  public record Item(String codigo, BigDecimal cantidad) {}
}