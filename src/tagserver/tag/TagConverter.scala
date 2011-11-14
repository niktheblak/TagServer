package tagserver.tag

import tagserver.Log
import org.jaudiotagger.tag.KeyNotFoundException
import org.jaudiotagger.tag.TagField
import org.jaudiotagger.tag.TagFieldKey
import scala.collection.mutable.HashMap

object TagConverter {
  val logger = Log.getLogger("tagserver.tag.TagConverter")

  def addCommonFields(map: HashMap[String, ItemData], tag: org.jaudiotagger.tag.Tag) {
    val album = tag.getFirstAlbum()
    val artist = tag.getFirstArtist()
    val comment = tag.getFirstComment()
    val genre = tag.getFirstGenre()
    val title = tag.getFirstTitle()
    val track = tag.getFirstTrack()
    val year = tag.getFirstYear()
    if (album.size > 0) {
      map.update(Keys.album, new TextData(album))
    }
    if (artist.size > 0) {
      map.update(Keys.artist, new TextData(artist))
    }
    if (comment.size > 0) {
      map.update(Keys.comment, new TextData(comment))
    }
    if (genre.size > 0) {
      map.update(Keys.genre, new TextData(genre))
    }
    if (title.size > 0) {
      map.update(Keys.title, new TextData(title))
    }
    if (track.size > 0) {
      map.update(Keys.track, new TextData(track))
    }
    if (year.size > 0) {
      map.update(Keys.date, new TextData(year))
    }

    try {
      val albumArtist = tag.getFirst(TagFieldKey.ALBUM_ARTIST)
      if (albumArtist.size > 0) {
        map.update(Keys.albumArtist, new TextData(albumArtist))
      }
    } catch {
      case e: KeyNotFoundException => logger.debug(e.toString)
    }
    try {
      val composer = tag.getFirst(TagFieldKey.COMPOSER)
      if (composer.size > 0) {
        map.update(Keys.composer, new TextData(composer))
      }
    } catch {
      case e: KeyNotFoundException => logger.debug(e.toString)
    }
    try {
      val conductor = tag.getFirst(TagFieldKey.CONDUCTOR)
      if (conductor.size > 0) {
        map.update(Keys.conductor, new TextData(conductor))
      }
    } catch {
      case e: KeyNotFoundException => logger.debug(e.toString)
    }
    try {
      val discNumber = tag.getFirst(TagFieldKey.DISC_NO)
      if (discNumber.size > 0) {
        map.update(Keys.discNumber, new TextData(discNumber))
      }
    } catch {
      case e: KeyNotFoundException => logger.debug(e.toString)
    }
  }

  def addBinaryFields(map: HashMap[String, ItemData], tag: org.jaudiotagger.tag.Tag) {
    val iter = tag.getFields
    while (iter.hasNext) {
      val field = iter.next
      if (field.isBinary) {
        map.update(field.getId, new BinaryData(field.getRawContent))
      }
    }
  }
  
  def convert(tag: org.jaudiotagger.tag.Tag): Tag = {
    val items = new HashMap[String, ItemData]
    addCommonFields(items, tag)
    new SerializableTag(items)
  }
}
