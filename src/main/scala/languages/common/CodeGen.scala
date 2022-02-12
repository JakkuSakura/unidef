package com.jeekrs.unidef
package languages.common

import java.io.StringWriter
import org.apache.velocity.{Template, VelocityContext}
import org.apache.velocity.app.{Velocity, VelocityEngine}
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.{StringResourceRepository, StringResourceRepositoryImpl}
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeSingleton
import org.apache.velocity.runtime.resource.loader.StringResourceLoader
import org.apache.velocity.runtime.resource.util.StringResourceRepository
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl

object CodeGen:
    val VELOCITY: VelocityEngine = VelocityEngine()

    VELOCITY.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string")
    VELOCITY.addProperty("resource.loader.string.class", classOf[StringResourceLoader].getName)
    VELOCITY.addProperty("resource.loader.string.modificationCheckInterval", "1")
    VELOCITY.addProperty("runtime.references.strict", true)
    VELOCITY.init()

    val REPO: StringResourceRepository = StringResourceLoader.getRepository()
    REPO.putStringResource("macro",
                           """
                             |#macro(block $code)
                             |#foreach($line in $code.split("\n"))
                             |    $line
                             |#end
                             |#end
                             |""".stripMargin
                           )

    val MACRO_LIBRARIES: java.util.List[Template] = java.util.ArrayList()
    MACRO_LIBRARIES.add(VELOCITY.getTemplate("macro"))


    def render(templateSource: String, context: VelocityContext): String =
        if context.getMacroLibraries == null then
            context.setMacroLibraries(MACRO_LIBRARIES)
        val id = System.identityHashCode(templateSource).toString
        if VELOCITY.getTemplate(id) == null then
            REPO.putStringResource(id, templateSource)

        val template = VELOCITY.getTemplate(id)
        val w = StringWriter()
        template.merge(context, w)

        w.toString
