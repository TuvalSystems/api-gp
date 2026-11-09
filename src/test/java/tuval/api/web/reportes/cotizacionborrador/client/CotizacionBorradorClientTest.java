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
}
