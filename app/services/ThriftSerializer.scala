package services

import com.twitter.scrooge.ThriftStruct
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TMemoryBuffer

object ThriftSerializer {

  val ThriftBufferInitialSize = 128

  def serializeToBytes(struct: ThriftStruct, includeCompressionFlag: Boolean): Array[Byte] = {
    val buffer = new TMemoryBuffer(ThriftBufferInitialSize)
    val protocol = new TCompactProtocol(buffer)
    struct.write(protocol)

    includeCompressionFlag match {
      case true => compressionByte +: buffer.getArray
      case false => buffer.getArray
    }
  }

  val compressionByte: Byte = {
    0x00.toByte
  }

}
