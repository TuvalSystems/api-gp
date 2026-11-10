package tuval.api.web.reportes.cotizacionborrador.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;
import tuval.api.web.reportes.cotizacionborrador.request.CotizacionBorradorReporteRequest;
import tuval.api.web.reportes.cotizacionborrador.service.tuvalbackend.CotizacionBorradorException;

class CotizacionBorradorClientTest {
  private static final String ORIGIN = "https://qa.example.invalid";
  private static final String ID = "5af09cd3-c181-461f-8637-c468f02d9638";
  private final ObjectMapper mapper = new ObjectMapper();
  private final HttpClient http = mock(HttpClient.class);
  private final CotizacionBorradorReporteRequest request = new CotizacionBorradorReporteRequest(
      ID, "CLIENTE-PRUEBA", 7L, null, null, List.of(new CotizacionBorradorReporteRequest.Item("ITEM", null, null)));

  @Test void delegaSoloAlDestinoFijoConBearerDelActorYConserva201Y200() throws Exception {
    for (int status : List.of(201, 200)) {
      var reply = response(status, success(status), "application/json");
      when(http.sendAsync(any(), any(HttpResponse.BodyHandler.class))).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(reply));
      var result = new CotizacionBorradorClient(mapper, true, ORIGIN, http).crear(request, "Bearer fixture-only");
      assertThat(result.status()).isEqualTo(status);
      assertThat(result.data().reutilizado()).isEqualTo(status == 200);
      var captor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
      verify(http).sendAsync(captor.capture(), any(HttpResponse.BodyHandler.class));
      var sent = captor.getValue();
      assertThat(sent.uri().toString()).isEqualTo(ORIGIN + "/api/cotizaciones/borrador-items");
      assertThat(sent.method()).isEqualTo("POST");
      assertThat(sent.timeout().orElseThrow()).isEqualTo(java.time.Duration.ofSeconds(30));
      assertThat(sent.headers().map().keySet()).containsExactlyInAnyOrder("Authorization", "Accept", "Content-Type");
      assertThat(sent.headers().firstValue("Authorization")).contains("Bearer fixture-only");
      var subscriber = HttpResponse.BodySubscribers.ofByteArray();
      sent.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<ByteBuffer>() {
        public void onSubscribe(Flow.Subscription value) { subscriber.onSubscribe(value); }
        public void onNext(ByteBuffer buffer) { subscriber.onNext(List.of(buffer)); }
        public void onError(Throwable error) { subscriber.onError(error); }
        public void onComplete() { subscriber.onComplete(); }
      });
      var body = mapper.readTree(subscriber.getBody().toCompletableFuture().join());
      assertThat(body.has("contextoValido")).isFalse();
      assertThat(body.path("items").get(0).has("cantidad")).isFalse();
      assertThat(body.path("request_id").asText()).isEqualTo(ID);
      assertThat(mapper.writeValueAsString(result.data())).doesNotContain("password", "debug", "precio", "costo", "secret");
      reset(http);
    }
  }

  @ParameterizedTest @ValueSource(strings = {"", "http://qa.example.invalid", "https://user:pass@qa.example.invalid",
      "https://qa.example.invalid/path", "https://qa.example.invalid?url=other", "https://qa.example.invalid#fragment",
      "https://qa.example.invalid:0", "https://qa.example.invalid:65536", "not-a-url"})
  void configuracionInseguraNoHaceHttp(String origin) {
    expectStatus(503, () -> new CotizacionBorradorClient(mapper, true, origin, http).crear(request, "Bearer fixture-only"));
    verifyNoInteractions(http);
  }

  @Test void deshabilitadoPorDefectoYFueraDePruebas() {
    expectStatus(503, () -> new CotizacionBorradorClient(mapper, false, ORIGIN, http).crear(request, "Bearer fixture-only"));
    for (String[] profiles : List.of(new String[]{}, new String[]{"prod"}, new String[]{"production"},
        new String[]{"qa", "prod"}, new String[]{"qa", "production"}, new String[]{"unknown"})) {
      var env = new MockEnvironment(); env.setActiveProfiles(profiles);
      expectStatus(503, () -> new CotizacionBorradorClient(mapper, true, ORIGIN, env).crear(request, "Bearer fixture-only"));
    }
    verifyNoInteractions(http);
  }

  @Test void transporteProduccionNoSigueRedireccionesNiUsaCookies() throws Exception {
    var env = new MockEnvironment(); env.setActiveProfiles("qa");
    var client = new CotizacionBorradorClient(mapper, true, ORIGIN, env);
    var field = CotizacionBorradorClient.class.getDeclaredField("client"); field.setAccessible(true);
    var transport = (HttpClient) field.get(client);
    assertThat(transport.followRedirects()).isEqualTo(HttpClient.Redirect.NEVER);
    assertThat(transport.cookieHandler()).isEmpty();
    assertThat(transport.authenticator()).isEmpty();
    assertThat(transport.connectTimeout()).contains(java.time.Duration.ofSeconds(5));
  }

  @ParameterizedTest @ValueSource(ints = {301,302,307,308,400,404,500,503,204})
  void falloORedireccionNoSeReintentaNiExponeBody(int status) throws Exception {
    var reply = response(status, "secret", "text/html");
      when(http.sendAsync(any(), any(HttpResponse.BodyHandler.class))).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(reply));
    expectStatus(502, () -> new CotizacionBorradorClient(mapper, true, ORIGIN, http).crear(request, "Bearer fixture-only"));
    verify(http, times(1)).sendAsync(any(), any(HttpResponse.BodyHandler.class));
  }

  @ParameterizedTest @ValueSource(ints = {401,403,409,422,429})
  void preservaDenegacionDeLaravelSinSuContenido(int status) throws Exception {
    var reply = response(status, "secret", "application/json");
      when(http.sendAsync(any(), any(HttpResponse.BodyHandler.class))).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(reply));
    expectStatus(status, () -> new CotizacionBorradorClient(mapper, true, ORIGIN, http).crear(request, "Bearer fixture-only"));
  }

  @ParameterizedTest @ValueSource(strings = {"null", "{}", "not json", "{\"success\":false}",
      "{\"success\":true,\"data\":{\"id\":1,\"numero\":\"COT\",\"estado\":\"aprobado\"}}"})
  void respuestaInvalidaNoSeDeclaraExitosa(String body) throws Exception {
    var reply = response(201, body, "application/json");
      when(http.sendAsync(any(), any(HttpResponse.BodyHandler.class))).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(reply));
    expectStatus(502, () -> new CotizacionBorradorClient(mapper, true, ORIGIN, http).crear(request, "Bearer fixture-only"));
  }

  @Test void timeoutErrorDeRedEInterrupcionNoReintentan() throws Exception {
    for (Exception error : List.of(new HttpTimeoutException("secret"), new IOException("secret"))) {
      when(http.sendAsync(any(), any(HttpResponse.BodyHandler.class))).thenReturn(java.util.concurrent.CompletableFuture.failedFuture(error));
      expectStatus(error instanceof HttpTimeoutException ? 504 : 502,
          () -> new CotizacionBorradorClient(mapper, true, ORIGIN, http).crear(request, "Bearer fixture-only"));
      verify(http).sendAsync(any(), any(HttpResponse.BodyHandler.class));
      assertThat(Thread.interrupted()).isFalse();
      reset(http);
    }
  }

  @Test void deadlineTotalCancelaBodyAtascadoDespuesDeHeaders() {
    var pending = new java.util.concurrent.CompletableFuture<HttpResponse<byte[]>>();
    when(http.sendAsync(any(), any(HttpResponse.BodyHandler.class))).thenReturn(pending);
    expectStatus(504, () -> new CotizacionBorradorClient(mapper, true, ORIGIN, http,
        java.time.Duration.ofMillis(50)).crear(request, "Bearer fixture-only"));
    assertThat(pending).isCancelled();
    verify(http).sendAsync(any(), any(HttpResponse.BodyHandler.class));
  }

  @Test void interrupcionCancelaSolicitudYConservaBandera() {
    var pending = new java.util.concurrent.CompletableFuture<HttpResponse<byte[]>>();
    when(http.sendAsync(any(), any(HttpResponse.BodyHandler.class))).thenReturn(pending);
    Thread.currentThread().interrupt();
    try {
      expectStatus(502, () -> new CotizacionBorradorClient(mapper, true, ORIGIN, http).crear(request, "Bearer fixture-only"));
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
      assertThat(pending).isCancelled();
    } finally { Thread.interrupted(); }
  }

  @Test void limitaRespuestaAntesDeAcumularMasDe64KiB() {
    var body = new RespuestaLimitada(); var subscription = mock(Flow.Subscription.class);
    body.onSubscribe(subscription);
    body.onNext(List.of(ByteBuffer.wrap(new byte[RespuestaLimitada.MAX_BYTES])));
    body.onNext(List.of(ByteBuffer.wrap(new byte[1])));
    verify(subscription).cancel();
    assertThat(body.getBody().toCompletableFuture()).isCompletedExceptionally();
    var small = new RespuestaLimitada(); small.onSubscribe(mock(Flow.Subscription.class));
    small.onNext(List.of(ByteBuffer.wrap(new byte[]{1,2}))); small.onComplete();
    assertThat(small.getBody().toCompletableFuture().join()).containsExactly(1,2);
  }

  private HttpResponse<byte[]> response(int status, String body, String type) {
    HttpResponse<byte[]> result = mock(HttpResponse.class);
    when(result.statusCode()).thenReturn(status);
    when(result.headers()).thenReturn(HttpHeaders.of(Map.of("Content-Type", List.of(type)), (a,b) -> true));
    when(result.body()).thenReturn(body.getBytes(StandardCharsets.UTF_8));
    return result;
  }
  private String success(int status) {
    return "{\"success\":true,\"debug\":\"secret\",\"data\":{\"id\":42,\"numero\":\"COT42\",\"estado\":\"borrador_app\","
        + "\"request_id\":\"" + ID + "\",\"reutilizado\":" + (status == 200)
        + ",\"password\":\"secret\",\"usuario\":{\"id\":1,\"codigo\":\"TEST\",\"password\":\"secret\"},"
        + "\"incluidos\":[{\"codigo\":\"ITEM\",\"costo\":1,\"precio\":2}]}}";
  }
  private void expectStatus(int status, org.assertj.core.api.ThrowableAssert.ThrowingCallable operation) {
    assertThatThrownBy(operation).isInstanceOfSatisfying(CotizacionBorradorException.class,
        error -> { assertThat(error.status()).isEqualTo(status); assertThat(error.getMessage()).isNull(); });
  }

  @org.junit.jupiter.api.Nested
  class TransporteBorradorPruebas {
    private final tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.Peticion payload =
        new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.Peticion("CLIENTE-PRUEBA", 7L,
            List.of(new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.Item("ITEM", new java.math.BigDecimal("2.50"))));
    private String valid() {
      return "{\"success\":true,\"debug\":\"secret\",\"data\":{\"id\":42,\"numero\":\"COT42\",\"estado\":\"borrador_app\","
          + "\"usuario\":{\"id\":6,\"codigo\":\"TEST\",\"password\":\"secret\"},\"punto_venta\":{\"id\":7},"
          + "\"cliente\":{\"id\":8,\"ruc\":\"CLIENTE-PRUEBA\"},\"incluidos\":[{\"indice\":0,\"codigo\":\"ITEM\",\"cantidad\":2.50,\"unidad\":\"UND\",\"precio\":99}]}}";
    }
    private tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient client(String origin, long millis) throws Exception {
      var ctor = tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.class.getDeclaredConstructor(
          ObjectMapper.class, boolean.class, String.class, boolean.class, java.time.Duration.class);
      ctor.setAccessible(true);
      return ctor.newInstance(mapper, true, origin, true, java.time.Duration.ofMillis(millis));
    }
    private class Local implements AutoCloseable {
      final com.sun.net.httpserver.HttpServer server;
      final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();
      final java.util.concurrent.atomic.AtomicReference<String> method = new java.util.concurrent.atomic.AtomicReference<>();
      final java.util.concurrent.atomic.AtomicReference<String> path = new java.util.concurrent.atomic.AtomicReference<>();
      final java.util.concurrent.atomic.AtomicReference<byte[]> body = new java.util.concurrent.atomic.AtomicReference<>();
      final java.util.concurrent.atomic.AtomicReference<com.sun.net.httpserver.Headers> headers = new java.util.concurrent.atomic.AtomicReference<>();
      final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();
      Local(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(executor);
        server.createContext("/", exchange -> {
          count.incrementAndGet(); method.set(exchange.getRequestMethod()); path.set(exchange.getRequestURI().toString());
          headers.set(exchange.getRequestHeaders()); body.set(exchange.getRequestBody().readAllBytes());
          try { handler.handle(exchange); } finally { exchange.close(); }
        });
        server.start();
      }
      String origin() { return "http://127.0.0.1:" + server.getAddress().getPort(); }
      public void close() { server.stop(0); executor.shutdownNow(); }
    }
    private void reply(com.sun.net.httpserver.HttpExchange exchange, int status, String body, String type) throws IOException {
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", type);
      exchange.sendResponseHeaders(status, bytes.length);
      exchange.getResponseBody().write(bytes);
    }
    @Test void getConJsonExactoSinCredencialesYRespuestaAllowlist() throws Exception {
      try (var server = new Local(e -> reply(e, 201, valid(), "application/json; charset=utf-8"))) {
        var env = new MockEnvironment(); env.setActiveProfiles("qa");
        var client = new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(mapper, env, true, server.origin(), true, false);
        var result = client.crear(payload);
        assertThat(server.count.get()).isEqualTo(1);
        assertThat(server.method.get()).isEqualTo("GET");
        assertThat(server.path.get()).isEqualTo("/cotizaciones/borrador-items");
        assertThat(mapper.readTree(server.body.get())).isEqualTo(mapper.readTree("{\"cliente_ruc\":\"CLIENTE-PRUEBA\",\"pv_id\":7,\"items\":[{\"codigo\":\"ITEM\",\"cantidad\":2.50}]}"));
        assertThat(server.headers.get().getFirst("Authorization")).isNull();
        assertThat(server.headers.get().getFirst("Cookie")).isNull();
        assertThat(server.headers.get().getFirst("Content-Type")).startsWith("application/json");
        assertThat(mapper.writeValueAsString(result)).doesNotContain("secret", "debug", "precio", "password");
        assertThat(result.id()).isEqualTo(42L);
      }
    }
    @Test void desconexionDespuesDelBodyNoReenviaGet() throws Exception {
      try (var server = new Local(e -> e.close())) {
        expectStatus(502, () -> client(server.origin(), 2000).crear(payload));
        assertThat(server.body.get()).isNotEmpty();
        assertThat(server.count.get()).isEqualTo(1);
      }
    }
    @Test void redireccion307NoSeSigue() throws Exception {
      try (var server = new Local(e -> { e.getResponseHeaders().set("Location", "/otra-creacion"); reply(e, 307, "secret", "text/plain"); })) {
        expectStatus(502, () -> client(server.origin(), 2000).crear(payload));
        assertThat(server.count.get()).isEqualTo(1);
      }
    }
    @Test void deadlineIncluyeBodyQueNoTermina() throws Exception {
      try (var server = new Local(e -> {
        e.getResponseHeaders().set("Content-Type", "application/json"); e.sendResponseHeaders(201, 0);
        e.getResponseBody().write('{'); e.getResponseBody().flush();
        try { Thread.sleep(5000); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
      })) {
        long start = System.nanoTime();
        expectStatus(504, () -> client(server.origin(), 200).crear(payload));
        assertThat(java.time.Duration.ofNanos(System.nanoTime()-start)).isLessThan(java.time.Duration.ofSeconds(3));
        assertThat(server.count.get()).isEqualTo(1);
      }
    }
    @Test void respuestaMayor64KiBNoSeAcepta() throws Exception {
      try (var server = new Local(e -> reply(e, 201, valid() + " ".repeat(65536), "application/json"))) {
        expectStatus(502, () -> client(server.origin(), 2000).crear(payload));
        assertThat(server.count.get()).isEqualTo(1);
      }
    }
    @Test void excesoDe64KiBAbortaSinEsperarRestoDelBody() throws Exception {
      try (var server = new Local(e -> {
        e.getResponseHeaders().set("Content-Type", "application/json"); e.sendResponseHeaders(201, 0);
        e.getResponseBody().write(new byte[65537]); e.getResponseBody().flush();
        try { Thread.sleep(5000); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
      })) {
        long start = System.nanoTime();
        expectStatus(502, () -> client(server.origin(), 1500).crear(payload));
        assertThat(java.time.Duration.ofNanos(System.nanoTime()-start)).isLessThan(java.time.Duration.ofSeconds(1));
        assertThat(server.count.get()).isEqualTo(1);
      }
    }
    @Test void contratoRespuestaInvalidoNoEsExito() throws Exception {
      for (String invalid : List.of("null", "{}", "not-json", valid().replace("\"success\":true", "\"success\":false"),
          valid().replace("\"id\":6", "\"id\":9"), valid().replace("\"id\":7", "\"id\":9"),
          valid().replace("CLIENTE-PRUEBA", "OTRO"), valid().replace("2.50", "3.00"),
          valid().replace("\"indice\":0", "\"indice\":1"), valid().replace("borrador_app", "aprobado"))) {
        try (var server = new Local(e -> reply(e, 201, invalid, "application/json"))) {
          expectStatus(502, () -> client(server.origin(), 2000).crear(payload));
          assertThat(server.count.get()).isEqualTo(1);
        }
      }
    }
    @Test void statusYContentTypeSeValidanSinReintentar() throws Exception {
      for (int status : List.of(200, 400, 401, 403, 409, 422, 429, 500, 503)) {
        try (var server = new Local(e -> reply(e, status, valid(), "application/json"))) {
          expectStatus(Set.of(401,403,422,429).contains(status) ? status : 502, () -> client(server.origin(), 2000).crear(payload));
          assertThat(server.count.get()).isEqualTo(1);
        }
      }
      try (var server = new Local(e -> reply(e, 201, valid(), "text/html"))) {
        expectStatus(502, () -> client(server.origin(), 2000).crear(payload));
      }
    }
    @Test void guardDePerfilesEnabledYOrigenBloqueaAntesDeHttp() throws Exception {
      try (var server = new Local(e -> reply(e, 201, valid(), "application/json"))) {
        for (String[] profiles : List.of(new String[]{}, new String[]{"prod"}, new String[]{"qa", "prod"}, new String[]{"unknown"})) {
          var env = new MockEnvironment(); env.setActiveProfiles(profiles);
          expectStatus(503, () -> new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(mapper, env, true, server.origin(), true, false).crear(payload));
        }
        var env = new MockEnvironment(); env.setActiveProfiles("qa");
        expectStatus(503, () -> new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(mapper, env, false, server.origin(), true, false).crear(payload));
        expectStatus(503, () -> new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(mapper, env, true, server.origin(), false, false).crear(payload));
        for (String origin : List.of("", "not-url", server.origin()+"/path", server.origin()+"?x=1", server.origin()+"#x", "http://user@127.0.0.1", "http://127.0.0.1:0", "http://127.0.0.1:65536")) {
          expectStatus(503, () -> new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(mapper, env, true, origin, true, false).crear(payload));
        }
        assertThat(server.count.get()).isZero();
      }
    }
    @Test void devExigeOptInDestinoVerificadoYSinMezclaProduccion() throws Exception {
      var type = tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient.class;
      var env = new MockEnvironment(); env.setActiveProfiles("dev");
      String origin = "http://tuvalapi.cosenco.ec";
      expectStatus(503, () -> new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(
          mapper, env, true, origin, true, false).verificarConfiguracion());
      expectStatus(503, () -> new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(
          mapper, env, true, origin, false, true).verificarConfiguracion());
      for (String allowed : List.of(origin, origin+":80", "https://tuvalapi.cosenco.ec", "https://tuvalapi.cosenco.ec:443")) {
        var client = new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(
            mapper, env, true, allowed, true, true);
        client.verificarConfiguracion();
        var pinned = type.getDeclaredField("pinDevTarget"); pinned.setAccessible(true);
        assertThat(pinned.getBoolean(client)).isTrue();
      }
      for (String denied : List.of("http://172.32.1.6", "http://172.30.1.7", "https://prod.example.invalid", origin+":8080",
          origin+"/path", origin+"?x=1", origin+"#fragment", "http://user@tuvalapi.cosenco.ec")) {
        expectStatus(503, () -> new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(
            mapper, env, true, denied, true, true).verificarConfiguracion());
      }
      for (String[] profiles : List.of(new String[]{"dev","prod"}, new String[]{"dev","production"}, new String[]{"dev","unknown"})) {
        env.setActiveProfiles(profiles);
        expectStatus(503, () -> new tuval.api.web.reportes.resolucionarticulos.client.BorradorItemsPruebasClient(
            mapper, env, true, origin, true, true).verificarConfiguracion());
      }
      // No socket is opened: inspect the resolver that the DEV transport installs.
      var method = type.getDeclaredMethod("destinoDev"); method.setAccessible(true);
      var dns = (org.apache.hc.client5.http.DnsResolver) method.invoke(null);
      assertThat(dns.resolve("tuvalapi.cosenco.ec")).hasSize(1);
      assertThat(dns.resolve("tuvalapi.cosenco.ec")[0].getHostAddress()).isEqualTo("172.30.1.7");
      assertThatThrownBy(() -> dns.resolve("172.32.1.6")).isInstanceOf(java.net.UnknownHostException.class);
      assertThatThrownBy(() -> dns.resolve("otro.example.invalid")).isInstanceOf(java.net.UnknownHostException.class);
    }
  }
}
