package com.fasterxml.jackson.module.caseclass.deser

import java.util.Objects

import com.fasterxml.jackson.core.{ JsonParser, JsonProcessingException }
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.{ BeanDescription, DeserializationContext, JsonNode, ObjectMapper }
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition
import com.fasterxml.jackson.databind.node.{ NullNode, ObjectNode }
import com.fasterxml.jackson.module.caseclass.mapper.CaseClassObjectMapper

import scala.reflect.runtime.universe._

class CaseClassDeserializer[T: Manifest]() extends StdDeserializer[T](manifest[T].runtimeClass) {

  private[this] val constructor = getRuntimeMirror.reflectClass(getScalaType.typeSymbol.asClass)
    .reflectConstructor(getScalaType.members.find(_.isConstructor).get.asMethod)

  private[this] val defaultValueMethodMirrors = {
    val companionInstance = getRuntimeMirror.reflectModule(getScalaType.typeSymbol.companion.asModule).instance
    val defaultValueMethods = getScalaType.companion.members.filter(_.isMethod)
      .filter(_.name.toString.startsWith(CaseClassDeserializer.defaultMethodPrefix)).map(_.asMethod)

    defaultValueMethods.map { method =>
      method.name.toString.replace(CaseClassDeserializer.defaultMethodPrefix, "").toInt ->
        getRuntimeMirror.reflect(companionInstance).reflectMethod(method)
    }.toMap
  }

  private[this] val fieldsWithIndex = typeOf[T].members.filter(!_.isMethod).toArray.reverse.zipWithIndex

  private[this] def zeroValue(tpe: Type) = {
    tpe match {
      case t: Type if t =:= typeOf[Boolean] ⇒ Boolean.box(false)
      case t: Type if t <:< typeOf[Option[_]] ⇒ None
      //      case t: Type if t <:< typeOf[collection.Map[_, _]] ⇒ collection.Map.empty
      //      case t: Type if t <:< typeOf[Iterable[_]] ⇒ Nil
      case t: Type if CaseClassDeserializer.numberTypes.contains(t) ⇒ 0.asInstanceOf[AnyRef]
      case _: Type ⇒ None.orNull
    }
  }

  private[this] val valueType = TypeFactory.defaultInstance().constructType(handledType())

  private[this] def getRuntimeMirror = runtimeMirror(getClass.getClassLoader)

  private[this] def getScalaType = typeOf[T]

  @throws[JsonProcessingException]
  def deserialize(jp: JsonParser, ctxt: DeserializationContext): T = {

    val node = jp.getCodec.readTree[ObjectNode](jp)
    val mapper = jp.getCodec.asInstanceOf[ObjectMapper with CaseClassObjectMapper]
    val beanDesc = ctxt.getConfig.introspect[BeanDescription](valueType)

    val params = fieldsWithIndex.map {
      case (field, index) ⇒
        val fieldName = field.name.toString.trim
        val beanPropertyDefinition: BeanPropertyDefinition = beanDesc.findProperties().get(index)
        val subNodeOpt = Option(node.get(fieldName))
        val originJavaType = mapper.constructType(field.typeSignature)
        val javaType = ctxt.getAnnotationIntrospector.refineDeserializationType(ctxt.getConfig, beanPropertyDefinition.getPrimaryMember, originJavaType)
        val deserializeClass = ctxt.getAnnotationIntrospector.findDeserializer(beanPropertyDefinition.getPrimaryMember)

        if (Objects.nonNull(deserializeClass)) {
          val deserializer = ctxt.deserializerInstance(beanPropertyDefinition.getField, deserializeClass)
          val subNode = subNodeOpt getOrElse NullNode.getInstance()
          deserializer.deserialize(mapper.treeAsTokens(subNode), ctxt)
        } else {
          subNodeOpt.fold {
            defaultValueMethodMirrors.get(index + 1).fold(zeroValue(field.typeSignature))(_.apply(None.orNull).asInstanceOf[Object])
          } { subNode =>
            mapper.readValue(mapper.treeAsTokens(subNode), javaType).asInstanceOf[AnyRef]
          }
        }
    }

    if (constructor.symbol.paramLists.head.size != params.length) {
      throw MismatchedInputException.from(jp, handledType(), s"can only instantiate non-static inner class by using ${getClass.getName}")
    } else {
      constructor.apply(params: _*).asInstanceOf[T]
    }
  }
}

object CaseClassDeserializer {

  def apply[T](clazz: Class[T]): CaseClassDeserializer[T] = {
    implicit val manifest = Manifest.classType[T](clazz)
    new CaseClassDeserializer[T]
  }

  private val defaultMethodPrefix = "$lessinit$greater$default$"

  private val numberTypes = Seq(typeOf[Int], typeOf[Long], typeOf[Char], typeOf[Short], typeOf[Byte], typeOf[Float], typeOf[Double])
}

