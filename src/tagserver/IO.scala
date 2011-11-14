package tagserver
import java.io.Closeable

object IO {
  def using[S <% Closeable, T](closeable: S)(f: S => T): T = {
    try {
      f(closeable)
    } finally {
      closeable.close()
    }
  }
}