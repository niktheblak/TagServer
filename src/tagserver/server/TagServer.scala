package tagserver.server

import tagserver.utils.{TagCollector, TagDatabase, TagSearcher}
import tagserver.Log
import tagserver.tag.Tag
import tagserver.utils.TagCollector.collectTags
import tagserver.server.commands._
import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.ListBuffer
import scala.concurrent.ops.spawn
import tagserver.utils.BaseDirDatabase

case class SetTagsRequest(tags: Seq[Tag]) extends TagServerRequest

// Hold the current tags in a separate actor so we can guarantee correct
// concurrent behavior when reloading tags
class TagListHolder extends Actor {
  var tags: Seq[Tag] = Nil
  
  def act() {
    loop {
      react {
        case SetTagsRequest(t) =>
          tags = t
        case GetTagsRequest =>
          reply(GetTagsReply(tags))
        case Quit =>
          exit()
      }
    }
  }
}

class TagServer extends Actor {
  val logger = Log.getLogger("tagserver.server.TagServer")

  def act() {
    logger.info("Starting tag server...")
    val baseDirs = (new ListBuffer[String] ++= loadBaseDirs())
    val listeners = new ListBuffer[Actor]
    val tagListHolder = new TagListHolder
    tagListHolder.start()
    tagListHolder ! SetTagsRequest(loadTagDatabase())

    loop {
      react {
        case AddBaseDirectory(dir) =>
          baseDirs += dir
          listeners foreach { _ ! BaseDirAdded(dir) }
        case RemoveBaseDirectory(dir) =>
          baseDirs -= dir
          listeners foreach { _ ! BaseDirRemoved(dir) }
        case msg @ GetTagsRequest => tagListHolder forward msg
        case GetBaseDirectoriesRequest =>
          reply(GetBaseDirectoriesReply(baseDirs))
        case ReloadTagsRequest =>
          // Potentially long-running operation
          actor {
            val tags = baseDirs flatMap { dir => collectTags(dir) }
            tagListHolder ! SetTagsRequest(tags)
            listeners foreach { _ ! TagsReloaded }
          }
        case ReloadTagsAsyncRequest =>
          // Potentially long-running operation
          actor {
            val tags = baseDirs flatMap { dir => collectTags(sender, dir) }
            tagListHolder ! SetTagsRequest(tags)
            reply(ReloadTagsAsyncReply)
            listeners foreach { _ ! TagsReloaded }
          }
        case SearchRequest(str) =>
          val response = tagListHolder !? GetTagsRequest
          response match {
            case GetTagsReply(tags) =>
              val found = TagSearcher.simpleSearch(str, tags)
              reply(SearchReply(found))
          }
        case AddListener(listener) => listeners += listener
        case RemoveListener(listener) => listeners -= listener
        case Ping => reply(Pong)
        case Quit =>
          val response = tagListHolder !? GetTagsRequest
          response match {
            case GetTagsReply(tags) =>
              saveTagDatabase(tags)
              saveBaseDirs(baseDirs)
          }
          tagListHolder ! Quit
          exit()
        case other => logger.warn(this + " has received unexpected message " + other)
      }
    }
  }
  
  def loadTagDatabase(): Seq[Tag] = {
    logger.info("Loading tag database...")
    val tags = TagDatabase.load()
    logger.info("Loaded " + tags.length + " tags from database")
    Console.println("Loaded " + tags.length + " tags from database")
    tags
  }

  def saveTagDatabase(tags: Seq[Tag]) {
    Console.println("Saving tags to database")
    logger.info("Saving " + tags.length + " tags to database...")
    TagDatabase.save(tags)
    logger.info("Tag database saved")
  }
  
  def loadBaseDirs(): Seq[String] = {
    logger.info("Loading base directories...")
    val baseDirs = BaseDirDatabase.load()
    logger.info("Loaded " + baseDirs.length + " base directories from database")
    Console.println("Loaded " + baseDirs.length + " base directories from database")
    baseDirs
  }
  
  def saveBaseDirs(baseDirs: Seq[String]) {
    Console.println("Saving base directories to database")
    logger.info("Saving " + baseDirs.length + " base directories to database...")
    BaseDirDatabase.save(baseDirs)
    logger.info("Base directories saved")
  }
}
