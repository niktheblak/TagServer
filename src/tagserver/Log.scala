package tagserver

import org.apache.log4j.Logger

class Log(logger: Logger) {
  def trace(msg: => Object) {
    if (logger.isTraceEnabled) {
      logger.trace(msg)
    }
  }
  def debug(msg: => Object) {
    if (logger.isDebugEnabled) {
      logger.debug(msg)
    }
  }
  def info(msg: => Object) {
    if (logger.isInfoEnabled) {
      logger.info(msg)
    }
  }
  def warn(msg: => Object) {
    logger.warn(msg)
  }
  def error(msg: => Object) {
    logger.error(msg)
  }
}

object Log {
  def getLogger(className: String) = {
    new Log(Logger.getLogger(className))
  }
}