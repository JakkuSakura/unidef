package unidef.utils
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs

import java.io.{File, FileWriter, Writer}
import java.nio.file.FileSystem
import scala.collection.mutable.ArrayBuffer
import org.apache.commons.io.FileUtils

class CodeGenResult {
  val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())
  val openList: ArrayBuffer[File] = ArrayBuffer.empty
  def newWriteAt(file: String): Writer = {
    val handle = fs.getPath(file).toFile
    handle.createNewFile()
    openList += handle
    new FileWriter(handle)
  }

  def dumpAll(base: File, remove: Boolean = false): Unit = {
    if (remove)
      FileUtils.deleteDirectory(base);

    openList.foreach(f => {
      val dest = new File(base, f.getPath)
      FileUtils.copyFile(f, dest)
    })
  }

  def closeFiles(): Unit = {
    openList.foreach(f => f.close())
    openList.clear()
  }

}
