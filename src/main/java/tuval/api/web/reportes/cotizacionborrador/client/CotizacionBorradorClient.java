package tuval.api.web.reportes.cotizacionborrador.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Set;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tuval.api.web.reportes.cotizacionborrador.dto.CotizacionBorradorReporteDto;
import tuval.api.web.reportes.cotizacionborrador.request.CotizacionBorradorReporteRequest;
import tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException;

@Service
public class CotizacionBorradorClient {
  private static final String PATH = "/api/cotizaciones/borrador-items";
  private final boolean enabled;
  private final URI endpoint;
  private final HttpClient client;
  private final ObjectMapper mapper;
  private final Duration timeout;

  @Autowired
  public CotizacionBorradorClient(ObjectMapper mapper,
      @Value("${reportes.cotizacion-borrador.enabled:false}") boolean enabled,
      @Value("${reportes.cotizacion-borrador.backend-origin:}") String origin, Environment environment) {
    this(mapper, enabled && entornoDePruebas(environment), origin, HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build());
  }

  private static boolean entornoDePruebas(Environment environment) {
    var profiles = Arrays.stream(environment.getActiveProfiles())
        .map(value -> value.toLowerCase(java.util.Locale.ROOT)).toList();
    return !profiles.isEmpty() && profiles.stream().allMatch(Set.of("local", "testing", "qa", "staging")::contains);
  }

  // Package-private injection point for tests; production always constructs the restricted client above.
  CotizacionBorradorClient(ObjectMapper mapper, boolean enabled, String origin, HttpClient client) {
    this(mapper, enabled, origin, client, Duration.ofSeconds(30));
  }

  CotizacionBorradorClient(ObjectMapper mapper, boolean enabled, String origin, HttpClient client, Duration timeout) {
    this.enabled = enabled;
    this.endpoint = destino(origin);
    this.client = client;
    this.mapper = mapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    this.timeout = timeout;
  }

  private static URI destino(String origin) {
    try {
      URI uri = URI.create(origin);
      if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
          || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
          || !(uri.getRawPath().isEmpty() || uri.getRawPath().equals("/"))
          || (uri.getPort() != -1 && (uri.getPort() < 1 || uri.getPort() > 65535))) return null;
      return uri.resolve(PATH);
    } catch (IllegalArgumentException exception) { return null; }
  }

  public Resultado crear(CotizacionBorradorReporteRequest request, String authorization) {
    if (!enabled || endpoint == null) throw new CotizacionBorradorException(503);
    CompletableFuture<HttpResponse<byte[]>> pending = null;
    try {
      // The caller's credential only. No cookies, impersonation headers, shared token or retry loop.
      var httpRequest = HttpRequest.newBuilder(endpoint).timeout(timeout)
          .header("Accept", "application/json").header("Content-Type", "application/json")
          .header("Authorization", authorization)
          .POST(HttpRequest.BodyPublishers.ofByteArray(mapper.writeValueAsBytes(request))).build();
      pending = client.sendAsync(httpRequest, info -> new RespuestaLimitada());
      // HttpRequest.timeout alone stops covering the response after headers arrive.
      HttpResponse<byte[]> response = pending.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
      int status = response.statusCode();
      if (Set.of(401, 403, 409, 422, 429).contains(status)) throw new CotizacionBorradorException(status);
      if (status != 200 && status != 201) throw new CotizacionBorradorException(502);
      if (!response.headers().firstValue("Content-Type").orElse("")
          .toLowerCase(java.util.Locale.ROOT).split(";", 2)[0].trim().equals("application/json")) {
        throw new CotizacionBorradorException(502);
      }
      var body = mapper.readTree(response.body());
      if (body == null || !body.path("success").isBoolean() || !body.path("success").booleanValue()
          || !body.path("data").isObject()) throw new CotizacionBorradorException(502);
      var data = mapper.treeToValue(body.path("data"), CotizacionBorradorReporteDto.class);
      if (data.id() == null || data.id() < 1 || data.numero() == null || data.numero().isBlank()
          || !"borrador_app".equals(data.estado()) || !request.requestId().equalsIgnoreCase(data.requestId())
          || !Boolean.valueOf(status == 200).equals(data.reutilizado())) throw new CotizacionBorradorException(502);
      return new Resultado(status, data);
    } catch (TimeoutException exception) {
      throw new CotizacionBorradorException(504);
    } catch (ExecutionException exception) {
      throw new CotizacionBorradorException(exception.getCause() instanceof HttpTimeoutException ? 504 : 502);
    } catch (IOException exception) {
      throw new CotizacionBorradorException(502);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new CotizacionBorradorException(502);
    } finally {
      if (pending != null && !pending.isDone()) pending.cancel(true);
    }
  }

  public record Resultado(int status, CotizacionBorradorReporteDto data) {}
}
