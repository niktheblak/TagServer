package tagserver.server

import scala.actors.Actor

import tagserver.tag.Tag

object commands {
  abstract class TagServerRequest
  case object GetTagsRequest extends TagServerRequest
  case class SearchRequest(str: String) extends TagServerRequest
  case class LoadTagsRequest(baseDirs: Seq[String]) extends TagServerRequest
  case class LoadTagsAsyncRequest(baseDirs: Seq[String]) extends TagServerRequest
  case object GetBaseDirectoriesRequest extends TagServerRequest
  case object ReloadTagsRequest extends TagServerRequest
  case object ReloadTagsAsyncRequest extends TagServerRequest
  case object Ping extends TagServerRequest
  case class AddBaseDirectory(dir: String) extends TagServerRequest
  case class RemoveBaseDirectory(dir: String) extends TagServerRequest
  case class AddListener(listener: Actor) extends TagServerRequest
  case class RemoveListener(listener: Actor) extends TagServerRequest
  case object Quit extends TagServerRequest

  abstract class TagServerReply
  case class GetTagsReply(tags: Seq[Tag]) extends TagServerReply
  case class LoadTagsReply(tags: Seq[Tag]) extends TagServerReply
  case object ReloadTagsAsyncReply extends TagServerReply
  case class Ack(r: TagServerRequest) extends TagServerReply
  case class TagDiscovered(tag: Tag) extends TagServerReply
  case class LoadTagsAsyncReply(tags: Seq[Tag]) extends TagServerReply
  case class SearchReply(tags: Seq[Tag]) extends TagServerReply
  case class GetBaseDirectoriesReply(dirs: Seq[String]) extends TagServerReply
  case object Pong extends TagServerReply

  abstract class ListenerNotification
  case object TagsReloaded extends ListenerNotification
  case class BaseDirAdded(dir: String) extends ListenerNotification
  case class BaseDirRemoved(dir: String) extends ListenerNotification
}