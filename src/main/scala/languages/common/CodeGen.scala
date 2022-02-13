package com.jeekrs.unidef
package languages.common

import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.exception.ResourceNotFoundException
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import org.apache.velocity.{Template, VelocityContext}

import java.io.StringWriter

object CodeGen {
  val VELOCITY: VelocityEngine = new VelocityEngine()

  VELOCITY.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string")
  VELOCITY.addProperty(
    "resource.loader.string.class",
    classOf[StringResourceLoader].getName
  )
  VELOCITY.addProperty("resource.loader.string.modificationCheckInterval", "1")
  VELOCITY.addProperty("runtime.references.strict", true)
  VELOCITY.init()

  val REPO: StringResourceRepository = StringResourceLoader.getRepository()
  REPO.putStringResource("macro", """
                           |#macro(block $code)
                           |#foreach($line in $code.split("\n"))
                           |    $line
                           |#end
                           |#end
                           |""".stripMargin)

  val MACRO_LIBRARIES: java.util.List[Template] = new java.util.ArrayList()
  MACRO_LIBRARIES.add(VELOCITY.getTemplate("macro"))

  def render(templateSource: String, context: VelocityContext): String = {

    if (context.getMacroLibraries == null)
      context.setMacroLibraries(MACRO_LIBRARIES)
    val id = System.identityHashCode(templateSource).toString
    try {
      VELOCITY.getTemplate(id)
    } catch {
      case e: ResourceNotFoundException =>
        REPO.putStringResource(id, templateSource)
        VELOCITY.getTemplate(id)
    }

    val template = VELOCITY.getTemplate(id)
    val w = new StringWriter()
    template.merge(context, w)

    w.toString
  }

}
