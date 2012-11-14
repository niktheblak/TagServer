package tagserver.tag

import java.io.DataInput
import java.io.DataOutput
import scala.collection.immutable.Map
import tagserver.Log
import scala.io.Codec

object DataType extends Enumeration {
  type DataType = Value
  val Text, Binary = Value

  def toByte(dataType: DataType): Byte = {
    dataType match {
      case Text => 0
      case Binary => 1
    }
  }

  def fromByte(dataType: Byte): DataType.Value = {
    dataType match {
      case 0 => Text
      case 1 => Binary
    }
  }
}

object SerializableTag {
  val logger = Log.getLogger("tagserver.tag.SerializableTag")

  private def readItem(input: DataInput): (String, ItemData) = {
    val dataTypeByte = input.readByte()
    val dataType = DataType.fromByte(dataTypeByte)
    logger.debug("Reading " + dataType + " item")
    val key = input.readUTF()
    logger.trace("Key: " + key)
    val dataLength = input.readInt()
    logger.trace("Data length: " + dataLength);
    val data = new Array[Byte](dataLength)
    input.readFully(data)

    val value = dataType match {
      case DataType.Text => TextData(Codec.fromUTF8(data).mkString)
      case DataType.Binary => BinaryData(data)
    }

    (key, value)
  }

  def read(input: DataInput): Tag = {
    logger.debug("Reading tag...")
    val itemCount = input.readInt()
    logger.debug("Stream contains " + itemCount + "items")
    def readItems(items: Map[String, ItemData], counter: Int): Map[String, ItemData] = {
      if (counter == itemCount) {
        items
      } else {
        val item = readItem(input)
        logger.trace("Read item: " + item)
        readItems(items + item, counter + 1)
      }
    }
    new SerializableTag(readItems(Map.empty, 0))
  }

  def write(output: DataOutput, tag: Tag) {
    output.writeInt(tag.items.size)
    tag.items.foreach(writeItem(output)_)
  }

  private def writeItem(output: DataOutput)(item: (String, ItemData)) {
    item match {
      case (key, value) =>
        value match {
          case TextData(_) => output.writeByte(DataType.toByte(DataType.Text))
          case BinaryData(_) => output.writeByte(DataType.toByte(DataType.Binary))
        }
        output.writeUTF(key)
        val data = value.storableData
        output.writeInt(data.length)
        output.write(data)
    }
  }
}

class SerializableTag(val items: scala.collection.Map[String, ItemData]) extends Tag {
  override def toString = {
    def build(builder: StringBuilder, item: (String, ItemData)): StringBuilder =
      item match {
        case (key, value) =>
          builder ++= key
          builder += '='
          builder ++= value.toString
          builder += '\n'
      }
    items.foldLeft(new StringBuilder)(build).toString
  }
}
