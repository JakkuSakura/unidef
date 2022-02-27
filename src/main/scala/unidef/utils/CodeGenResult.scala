package unidef.utils
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs

import java.io.{File, FileWriter, Writer}
import java.nio.file.FileSystem
import scala.collection.mutable.ArrayBuffer
import org.apache.commons.io.FileUtils

case class OpenedFile(file: File, writer: FileWriter)
class CodeGenResult {
  val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())
  val openList: ArrayBuffer[OpenedFile] = ArrayBuffer.empty
  def newWriteAt(filename: String): Writer = {
    val file = fs.getPath(filename).toFile
    file.createNewFile()
    val writer = new FileWriter(file)
    openList += OpenedFile(file, writer)
    writer
  }

  def dumpAll(base: File, remove: Boolean = false): Unit = {
    if (remove)
      FileUtils.deleteDirectory(base);

    openList.foreach(f => {
      val dest = new File(base, f.file.getPath)
      FileUtils.copyFile(f.file, dest)
    })
  }

  def closeFiles(): Unit = {
    openList.foreach(f => f)
    openList.clear()
  }

}
