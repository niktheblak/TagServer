package tagserver.utils

import tagserver.tag.Tag

object TagSearcher {
  def simpleSearch(str: String, tags: Seq[Tag]): Seq[Tag] =
    tags.filter(t => contains(str, t))

  def globToRegex(glob: String): String = {
    def escape(c: Char): Array[Char] = {
      c match {
        case '*' => Array('.', '*')
        case '?' => Array('.')
        case '.' => Array('\\', '.')
        case '(' => Array('\\', '(')
        case ')' => Array('\\', ')')
        case '{' => Array('\\', '{')
        case '}' => Array('\\', '}')
        case ch => Array(c)
      }
    }
    glob.foldLeft(new StringBuilder)((buf, c) => buf.appendAll(escape(c))).toString
  }

  def contains(str: String, tag: Tag): Boolean = {
    val canonized = globToRegex(str).toUpperCase + ".*"
    val title = tag.title.toString.toUpperCase
    val artist = tag.artist.toString.toUpperCase
    val album = tag.album.toString.toUpperCase
    title.matches(canonized) || artist.matches(canonized) || album.matches(canonized)
  }
}
