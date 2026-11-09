package tuval.api.web.reportes.cotizacionborrador.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/** Bound upstream buffering without returning a stream after the request timeout. */
final class RespuestaLimitada implements HttpResponse.BodySubscriber<byte[]> {
  static final int MAX_BYTES = 64 * 1024;
  private final CompletableFuture<byte[]> result = new CompletableFuture<>();
  private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
  private Flow.Subscription subscription;

  @Override public CompletionStage<byte[]> getBody() { return result; }
  @Override public void onSubscribe(Flow.Subscription value) {
    subscription = value;
    value.request(1);
  }
  @Override public void onNext(List<ByteBuffer> buffers) {
    for (ByteBuffer buffer : buffers) {
      if (buffer.remaining() > MAX_BYTES - bytes.size()) {
        subscription.cancel();
        result.completeExceptionally(new IOException("Respuesta demasiado grande"));
        return;
      }
      byte[] chunk = new byte[buffer.remaining()];
      buffer.get(chunk);
      bytes.writeBytes(chunk);
    }
    subscription.request(1);
  }
  @Override public void onError(Throwable error) { result.completeExceptionally(error); }
  @Override public void onComplete() { result.complete(bytes.toByteArray()); }
}
