package tagserver.utils

import tagserver.Log
import tagserver.tag.{ Tag, TagConverter }
import tagserver.server.commands._
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.TagFieldKey
import java.io.{ File, FileFilter }
import scala.collection.immutable.{ Map, ListMap }
import scala.actors.Actor
import scala.actors.OutputChannel

class TagFilenameFilter extends FileFilter {
  def accept(dir: File): Boolean = {
    val name = dir.getName()
    !(name.equals(".") || name.equals("..")) &&
      (dir.isDirectory || !(TagCollector.supportedFileTypes filter { name.endsWith(_) }).isEmpty)
  }
}

object TagCollector {
  val supportedFileTypes = List("mp3", "mp4", "m4a", "ogg", "flac")
  val filenameFilter = new TagFilenameFilter()
  val logger = Log.getLogger("tagserver.TagCollector")

  def collectTags(baseDir: String): List[Tag] = {
    val files = new File(baseDir).listFiles(filenameFilter)
    scanFileSystemForTags(None)(files.toList, Nil)
  }

  def collectTags(output: OutputChannel[Any], baseDir: String): List[Tag] = {
    val files = new File(baseDir).listFiles(filenameFilter)
    scanFileSystemForTags(Some(output))(files.toList, Nil)
  }

  private def scanFileSystemForTags(output: Option[OutputChannel[Any]])(files: List[File], tags: List[Tag]): List[Tag] = {
    files match {
      case Nil => tags
      case (file :: rest) =>
        if (file.isDirectory) {
          logger.debug("Recursing into directory " + file.toString)
          val newFiles = file.listFiles(filenameFilter)
          scanFileSystemForTags(output)(rest ++ newFiles, tags)
        } else {
          logger.info("Reading tags from file " + file.toString)
          try {
            val f = AudioFileIO.read(file)
            val jtag = f.getTag()
            if (jtag != null) {
              logger.trace("Read tag: " + jtag.toString)
              val tag = TagConverter.convert(jtag)
              output match {
                case Some(actor) => actor ! TagDiscovered(tag)
                case None => ;
              }
              scanFileSystemForTags(output)(rest, tag :: tags)
            } else {
              logger.debug("File " + file.toString + " does not contain tags")
              scanFileSystemForTags(output)(rest, tags)
            }
          } catch {
            case e: Exception =>
              logger.error("Unable to read file " + file.toString + ": " + e.getStackTraceString)
              scanFileSystemForTags(output)(rest, tags)
          }
        }
    }
  }
}
