package tagserver

import tagserver.server.TagServer
import org.apache.log4j.{ BasicConfigurator, Logger, Level }
import java.util.logging.LogManager

object CLI extends App {
  try {
    BasicConfigurator.configure()
    Logger.getRootLogger().setLevel(Level.WARN)
    val logger = java.util.logging.Logger.getLogger("org.jaudiotagger")
    logger.setLevel(java.util.logging.Level.SEVERE)
    val server = new TagServer
    val client = new TagClient(server)
    server.start
    client.start
  } catch {
    case e: Exception =>
      Console.println(e)
      System.exit(1)
  }
}
