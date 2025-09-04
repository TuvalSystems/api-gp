package tuval.api.web.models.Utils;

public class CoerceHelperUtils {

  public static int getInt(Object o, int dflt) {
    if (o == null) return dflt;
    if (o instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(o.toString().trim());
    } catch (Exception e) {
      return dflt;
    }
  }

  public static int toInt(Object o, int dflt) {
    if (o == null) return dflt;
    if (o instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(o.toString().trim());
    } catch (Exception ignored) {
      return dflt;
    }
  }

  public static String stringOrEmpty(Object v) {
    return v == null ? "" : v.toString().trim();
  }
}
