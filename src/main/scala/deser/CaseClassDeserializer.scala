package deser

import java.util.Objects

import com.fasterxml.jackson.core.{JsonParser, JsonProcessingException}
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.{BeanDescription, DeserializationContext, ObjectMapper}
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition
import com.fasterxml.jackson.databind.node.ObjectNode
import mapper.CaseClassObjectMapper

import scala.reflect.runtime.universe._

class CaseClassDeserializer[T: Manifest]() extends StdDeserializer[T](manifest[T].runtimeClass) {

  private[this] val constructor = handledType.getConstructors.head

  private[this] val methods = handledType.getMethods

  private[this] val fieldsWithIndex = typeOf[T].members.filter(!_.isMethod).toArray.reverse.zipWithIndex

  private[this] val numberTypes = Seq(typeOf[Int], typeOf[Long], typeOf[Char], typeOf[Short], typeOf[Byte], typeOf[Float], typeOf[Double])

  private[this] def zeroValue(tpe: Type) = {
    tpe match {
      case t if t =:= typeOf[Boolean]              ⇒ Boolean.box(false)
      case t if t <:< typeOf[Option[_]]            ⇒ None
      case t if t <:< typeOf[collection.Map[_, _]] ⇒ collection.Map.empty
      case t if t <:< typeOf[Iterable[_]]          ⇒ Nil
      case t if numberTypes.contains(t)            ⇒ 0.asInstanceOf[AnyRef]
      case _                                       ⇒ null
    }
  }

  private[this] val valueType = TypeFactory.defaultInstance().constructType(handledType())

  @throws[JsonProcessingException]
  def deserialize(jp: JsonParser, ctxt: DeserializationContext): T = {

    val node = jp.getCodec.readTree[ObjectNode](jp)
    val mapper = jp.getCodec.asInstanceOf[ObjectMapper with CaseClassObjectMapper]
    val beanDesc = ctxt.getConfig.introspect[BeanDescription](valueType)


    val params = fieldsWithIndex.map {
      case (field, index) ⇒
        val fieldName = field.name.toString.trim
        if (node.hasNonNull(fieldName)) {
          val subJsonParser = mapper.treeAsTokens(node.get(fieldName))
          val beanPropertyDefinition: BeanPropertyDefinition = beanDesc.findProperties().get(index)
          val javaType = ctxt.getAnnotationIntrospector.refineDeserializationType(ctxt.getConfig, beanPropertyDefinition.getPrimaryMember,mapper.constructType(field.typeSignature))
          val deserializeClass = ctxt.getAnnotationIntrospector.findDeserializer(beanPropertyDefinition.getPrimaryMember)

          if(Objects.nonNull(deserializeClass)) {
            val deserializer = ctxt.deserializerInstance(beanPropertyDefinition.getField,deserializeClass)
            deserializer.deserialize(subJsonParser,ctxt)
          } else {
            mapper.readValue(subJsonParser, javaType).asInstanceOf[AnyRef]
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

