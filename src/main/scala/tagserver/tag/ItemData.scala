package tagserver.tag

import scala.io.Codec

trait ItemData {
  val storableData: Array[Byte]
}

case class TextData(text: String) extends ItemData {
  val storableData = Codec.toUTF8(text)
  override def toString = text
}

case class BinaryData(binaryData: Array[Byte]) extends ItemData {
  val storableData = binaryData
  override def toString = "[binary data " + binaryData.length + " bytes]"
}
