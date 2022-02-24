package com.zloyrobot.scilla.ide

import com.intellij.lang.parameterInfo.*
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.util.parentOfType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.zloyrobot.scilla.lang.*


class ScillaParameterInfoProvider : ParameterInfoHandler<ScillaAppExpression, Array<ScillaIdWithType?>> {
	override fun findElementForParameterInfo(context: CreateParameterInfoContext): ScillaAppExpression? {
		val application = context.file.findElementAt(context.offset)?.parentOfType<ScillaAppExpression>() ?: return null
		
		val value = application.function?.reference?.resolve() as? ScillaLetElement ?: return null
		var function: ScillaExpression? = value.initializer ?: return null
		val params = mutableListOf<ScillaIdWithType?>()
		while (function is ScillaFunExpression) {
			val param = function.parameterList?.parameters?.firstOrNull()
			var body = function.body
			params.add(param)
			
			while (body is ScillaLetExpression)
				body = body.body
			
			function = body 
		}
		
		context.itemsToShow = arrayOf(params.toTypedArray())
		return application
	}

	override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): ScillaAppExpression? {
		return context.file.findElementAt(context.offset)?.parentOfType()
	}

	override fun updateUI(params: Array<ScillaIdWithType?>?, context: ParameterInfoUIContext) {
		if (params == null) 
			return
		
		val current = params.getOrNull(context.currentParameterIndex)
		val builder = StringBuilder()
		var start = -1
		var stop = -1
		
		for (param in params) {
			if (current == param)
				start = builder.length
			
			if (param == null)
				builder.append("?")
			else {
				val type = param.typeAnnotation?.ownType?.presentation ?: "?"
				builder.append("(${param.name}: $type) ")
			}

			if (current == param)
				stop = builder.length
		}
		context.setupUIComponentPresentation(
			builder.toString(),				
			start,
			stop,
			!context.isUIComponentEnabled,
			false,
			false,
			context.defaultParameterColor.brighter())
	}
	
	override fun updateParameterInfo(parameterOwner: ScillaAppExpression, context: UpdateParameterInfoContext) {
		if (context.parameterOwner != parameterOwner)  
			context.removeHint()
		else {
			val offset = context.offset
			
			if (parameterOwner.arguments.isEmpty() || parameterOwner.arguments[0].startOffset > offset) {
				context.setCurrentParameter(-1)
				return
			}
			
			for ((index, arg) in parameterOwner.arguments.withIndex()) {
				if (arg.endOffset > offset) {
					context.setCurrentParameter(index)
					return
				}
			}
			context.setCurrentParameter(parameterOwner.arguments.size)
		}
	}

	override fun showParameterInfo(element: ScillaAppExpression, context: CreateParameterInfoContext) {
		context.showHint(element, element.startOffset, this)
	}
}
