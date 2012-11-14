package tagserver.tag

import scala.collection.Map

object Keys {
  val artist = "ARTIST"
  val album = "ALBUM"
  val title = "TITLE"
  val track = "TRACK"
  val date = "YEAR"
  val genre = "GENRE"
  val comment = "COMMENT"
  val albumArtist = "ALBUMARTIST"
  val composer = "COMPOSER"
  val conductor = "CONDUCTOR"
  val discNumber = "DISCNUMBER"
}

trait Tag {
  val items: Map[String, ItemData]

  def contains(key: String) = items.contains(key)

  def artist = items(Keys.artist)
  def album = items(Keys.album)
  def title = items(Keys.title)
  def track = items(Keys.track)
  def date = items(Keys.date)
  def genre = items(Keys.genre)
  def comment = items(Keys.comment)
  def albumArtist = items(Keys.albumArtist)
  def composer = items(Keys.composer)
  def discNumber = items(Keys.discNumber)
}
