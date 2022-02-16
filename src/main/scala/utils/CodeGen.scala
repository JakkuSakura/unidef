package com.jeekrs.unidef
package utils

import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.exception.ResourceNotFoundException
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.ResourceCacheImpl
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import org.apache.velocity.{Template, VelocityContext}
import org.slf4j.helpers.NOPLogger
import org.apache.velocity.tools.{ToolContext, ToolManager}

import java.io.{StringWriter, Writer}
import scala.jdk.CollectionConverters._

object CodeGen {
  val VELOCITY: VelocityEngine = new VelocityEngine()
  VELOCITY.setProperty(
    RuntimeConstants.RUNTIME_LOG_INSTANCE,
    NOPLogger.NOP_LOGGER
  )

  VELOCITY.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string")

  VELOCITY.addProperty(
    RuntimeConstants.RESOURCE_MANAGER_CACHE_CLASS,
    classOf[ResourceCacheImpl].getName
  )
  VELOCITY.addProperty(
    "resource.loader.string.class",
    classOf[StringResourceLoader].getName
  )
  VELOCITY.addProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true)

  VELOCITY.init()

  val toolManager: ToolManager = new ToolManager()
  toolManager.configure("org/apache/velocity/tools/generic/tools.xml")
  toolManager.setVelocityEngine(VELOCITY)
  val toolContext: ToolContext = toolManager.createContext()
  val enhancedContext = new VelocityContext(toolContext)
  enhancedContext.put("text", TextTool)

  val REPO: StringResourceRepository = StringResourceLoader.getRepository()
  REPO.putStringResource(
    "macro",
    """
          |#macro(indent $code $i)$text.indent($code, $i)
          |#end
          |""".stripMargin
  )

  val MACRO_LIBRARIES: java.util.List[Template] = Seq(
    VELOCITY.getTemplate("macro")
  ).asJava

  def renderTo(templateSource: String,
               context: VelocityContext,
               writer: Writer): Unit = {

    if (context.getMacroLibraries == null)
      context.setMacroLibraries(MACRO_LIBRARIES)
    val id = System.identityHashCode(templateSource).toString
    val template = try {
      VELOCITY.getTemplate(id)
    } catch {
      case e: ResourceNotFoundException =>
        REPO.putStringResource(id, templateSource)
        VELOCITY.getTemplate(id)
    }
    template.merge(context, writer)

  }
  def render(templateSource: String, context: VelocityContext): String = {
    val writer = new StringWriter()
    renderTo(templateSource, context, writer)
    writer.toString
  }
  def createContext: VelocityContext = new VelocityContext(enhancedContext)
}

object TextTool {
  def indent(text: String, indent: Int): String = {
    val indentStr = " " * indent
    val spt = text.split("\n")
    if (spt.length == 1)
      spt(0)
    else {
      spt.head + "\n" + (1 until spt.length)
        .map(spt(_))
        .map(indentStr + _)
        .mkString("\n")
    }
  }
}
