package com.jeekrs.unidef
package languages.common

import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.exception.ResourceNotFoundException
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import org.apache.velocity.{Template, VelocityContext}
import scala.jdk.CollectionConverters._
import java.io.{StringWriter, Writer}

object CodeGen {
  val VELOCITY: VelocityEngine = new VelocityEngine()

  VELOCITY.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string")
  VELOCITY.addProperty(
    "resource.loader.string.class",
    classOf[StringResourceLoader].getName
  )
  VELOCITY.addProperty(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true)
  VELOCITY.init()

  val REPO: StringResourceRepository = StringResourceLoader.getRepository()
  REPO.putStringResource("macro", """
                           |#macro(block $code)
                           |#foreach($line in $code.split("\n"))
                           |    $line
                           |#end
                           |#end
                           |""".stripMargin)

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

}
