package tagserver.utils

import tagserver.Log
import tagserver.IO.using
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.File

object BaseDirDatabase {
  val logger = Log.getLogger("tagserver.utils.BaseDirDatabase")
  val fileName = ".basedirs"
    
  def save(baseDirs: Seq[String]) {
    using(new DataOutputStream(new FileOutputStream(fileName))) { output =>
      output.writeInt(baseDirs.length)
      baseDirs foreach output.writeUTF
    }
  }
  
  def load(): Seq[String] = {
    val file = new File(fileName)
    if (!file.exists || file.length == 0) {
      logger.debug("Base directories file does not exist; returning")
      return Nil
    }
    using(new DataInputStream(new FileInputStream(fileName))) { input =>
      val baseDirCount = input.readInt()
      logger.debug("Loading " + baseDirCount + " directories from database")
      def readBaseDirs(baseDirs: List[String], counter: Int): List[String] = {
        if (counter == baseDirCount) {
          logger.debug("Base directories loaded, returning " + counter + " directories")
          baseDirs
        } else {
          val baseDir = input.readUTF()
          logger.trace("Directory " + counter + ": " + baseDir);
          readBaseDirs(baseDir :: baseDirs, counter + 1)
        }
      }
      readBaseDirs(Nil, 0)
    }
  }
}