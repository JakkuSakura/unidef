package unidef.utils
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.typesafe.scalalogging.Logger

import java.io.{
  BufferedReader,
  File,
  FileReader,
  FileWriter,
  IOException,
  OutputStream,
  PrintWriter,
  Reader,
  Writer
}
import java.nio.file.{
  CopyOption,
  FileAlreadyExistsException,
  FileSystem,
  Files,
  Path,
  StandardCopyOption,
  StandardOpenOption
}
import scala.collection.mutable.ArrayBuffer
import org.apache.commons.io.FileUtils

import scala.collection.mutable
import java.nio.file.attribute.FileAttribute

private class OpenedFile(val filePath: Path, var stream: OutputStream, var writer: PrintWriter) {
  def resetWriters(): Unit = {
    writer.close()
    stream = Files.newOutputStream(filePath)
    writer = new PrintWriter(new NioWriter(stream))
  }
}
private object OpenedFile {
  def apply(filePath: Path): OpenedFile = {
    val stream = Files.newOutputStream(filePath)
    val writer = new PrintWriter(new NioWriter(stream))
    new OpenedFile(filePath, stream, writer)
  }
}

private class NioWriter(outputStream: OutputStream) extends Writer {
  @throws[IOException]
  override def write(cbuf: Array[Char], off: Int, len: Int): Unit = {
    outputStream.write(new String(cbuf, off, len).getBytes)
  }

  override def close(): Unit = ()
  override def flush(): Unit = ()

}

class VirtualFileSystem {
  val logger: Logger = Logger[this.type]
  val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())
  val openList: mutable.HashMap[String, OpenedFile] = mutable.HashMap.empty
  def getWriterAt(filename: String): PrintWriter = {
    if (openList.contains(filename)) {
      openList(filename).writer
    } else
      newWriterAt(filename)
  }

  def newWriterAt(filename: String): PrintWriter = {
    if (openList.contains(filename)) {
      logger.warn(s"Attempting to create a new writer for an already opened file $filename")
      val cached = openList(filename)
      cached.resetWriters()
      cached.writer
    } else {
      logger.debug(s"Creating virtual file $filename")
      val filePath = fs.getPath(filename)
      if (filePath.getParent != null) {
        Files.createDirectories(filePath.getParent)
      }
      val stream = Files.newOutputStream(filePath)
      val openedFile = OpenedFile(filePath)
      openList += filename -> openedFile
      openedFile.writer
    }
  }

  def dumpAll(base: Path, remove: Boolean = false): Unit = {
    if (remove)
      FileUtils.deleteDirectory(base.toFile);
    flushFiles()
    openList.foreach { (name, f) =>
      val dest = base.resolve(name)
      if (dest.getParent != null)
        try {
          Files.createDirectories(dest.getParent)
        } catch {
          case e: FileAlreadyExistsException =>
          case e => throw e
        }
      Files.copy(f.filePath, dest, StandardCopyOption.REPLACE_EXISTING)

    }
  }
  def showAsString(writer: Writer): Unit = {
    flushFiles()
    openList.foreach { (name, f) =>
      writer.write(s"====${name}====\n")
      val content = Files.readAllBytes(f.filePath)
      writer.append(new String(content))

    }
    writer.flush()
  }
  def flushFiles(): Unit = {
    openList.values.foreach(f => f.writer.flush())
  }
  def closeFiles(): Unit = {
    openList.values.foreach(f => f.writer.close())
    openList.clear()
  }
}
