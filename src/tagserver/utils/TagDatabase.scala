package tagserver.utils

import tagserver.Log
import tagserver.IO.using
import tagserver.tag.Tag
import tagserver.tag.SerializableTag

import java.io.Closeable
import java.io.IOException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

object TagDatabase {
  val logger = Log.getLogger("tagserver.utils.TagDatabase")
  val fileName = ".tagdata"
  
  def save(tags: Seq[Tag]) {
    using(new DataOutputStream(new FileOutputStream(fileName))) (output => {
      output.writeInt(tags.size)
      tags foreach { SerializableTag.write(output, _) }
    })
  }
  
  def load(): Seq[Tag] = {
    logger.debug("Loading tag database")
    val file = new File(fileName)
    if (!file.exists || file.length == 0) {
      logger.debug("Database file does not exist; returning")
      return Nil
    }
    
    using(new DataInputStream(new FileInputStream(file))) (input => {
      val tagCount = input.readInt()
      logger.debug("Loading " + tagCount + " tag from database")
      def readItems(tags: List[Tag], counter: Int): List[Tag] = {
        if (counter == tagCount) {
          logger.debug("Tag database loaded, returning " + counter + " tags")
          tags
        } else {
          val tag = SerializableTag.read(input)
          logger.trace("Tag " + counter + ": " + tag);
          readItems(tag :: tags, counter + 1)
        }
      }
      readItems(Nil, 0)
    })
  }
}
