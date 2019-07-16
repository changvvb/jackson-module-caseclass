package com.fasterxml.jackson.module.deser

import com.fasterxml.jackson.core.{JsonParser, JsonProcessingException}
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.{BeanDescription, BeanProperty, DeserializationContext, JavaType, JsonDeserializer, ObjectMapper}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.mapper.CaseClassObjectMapper

import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

class CaseClassDeserializer[T: Manifest]() extends StdDeserializer[T](manifest[T].runtimeClass) {

  private val constructor = handledType.getConstructors.head

  private val methods = handledType.getMethods

  private val fields = typeOf[T].members.filter(!_.isMethod).toArray.reverse

  private val tpe = typeOf[T]

  private val annotation: universe.Annotation = tpe.typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.head.last.annotations.head

  private val fieldsWithIndex = fields.zipWithIndex

  private val numberTypes = Seq(typeOf[Int], typeOf[Long], typeOf[Char], typeOf[Short], typeOf[Byte], typeOf[Float], typeOf[Double])

  private def zeroValue(tpe: Type) = {  
    tpe match {
      case t if numberTypes.contains(t)            ⇒ 0.asInstanceOf[AnyRef]
      case t if t =:= typeOf[Boolean]              ⇒ Boolean.box(false)
      case t if t <:< typeOf[Option[_]]            ⇒ None
      case t if t <:< typeOf[collection.Map[_, _]] ⇒ collection.Map.empty
      case t if t <:< typeOf[Iterable[_]]          ⇒ Nil
      case _                                       ⇒ null
    }
  }

  @throws[JsonProcessingException]
  def deserialize(jp: JsonParser, ctxt: DeserializationContext): T = {

    val node: ObjectNode = jp.getCodec.readTree(jp)
    val mapper = jp.getCodec.asInstanceOf[ObjectMapper with CaseClassObjectMapper]
    val beanDesc: BeanDescription = ctxt.getConfig.introspect(mapper.constructType(tpe.typeSymbol.typeSignature))

    beanDesc.getClassAnnotations

    val params = fieldsWithIndex.map {
      case (field, index) ⇒
        val fieldName = field.name.toString.trim
        if (node.hasNonNull(fieldName)) {
          val javaType = mapper.constructType(field.typeSignature)
          val subJsonParser = mapper.treeAsTokens(node.get(fieldName))
          val bd = beanDesc.findProperties().get(index)
          Option(bd.getPrimaryMember.getAnnotation(classOf[JsonDeserialize])).map(_.using()).fold(
            mapper.readValue(subJsonParser, javaType).asInstanceOf[AnyRef]
          ) { deserClass =>
            val deserializer = ctxt.deserializerInstance(bd.getField,deserClass)
            deserializer.deserialize(subJsonParser,ctxt)
          }

        } else {
          val methodName = "$lessinit$greater$default$" + (index + 1)
          methods.find(_.getName == methodName).fold(zeroValue(field.typeSignature))(_.invoke(null))
        }
    }

    constructor.newInstance(params: _*).asInstanceOf[T]
  }
}

object CaseClassDeserializer {

  def apply[AnyRef](clazz: Class[_]): CaseClassDeserializer[AnyRef] = {
      implicit val manifest = Manifest.classType[AnyRef](clazz)
      new CaseClassDeserializer[AnyRef]
  }
}

