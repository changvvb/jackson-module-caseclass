package com.fasterxml.jackson.module.caseclass.mapper

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ DeserializationContext, JavaType, JsonDeserializer, ObjectMapper }
import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.fasterxml.jackson.module.caseclass.deser.CaseClassDeserializer
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.collection.mutable
import scala.reflect.runtime.universe._

trait CaseClassObjectMapper extends ObjectMapper {
  self: ObjectMapper ⇒

  private[this] var _allCaseClassEnabled = false

  def setAllCaseClassEnabled(enabled: Boolean): Unit = _allCaseClassEnabled = enabled

  this.registerModule(DefaultScalaModule)

  def constructType(tpe: Type): JavaType = {
    def checkArgumentLength(argumentsLength: Int, shouldBeLength: Int) = {
      if (argumentsLength != shouldBeLength) {
        throw new IllegalArgumentException(s"Need exactly $shouldBeLength type parameter for types (${tpe.typeSymbol.fullName})")
      }
    }

    lazy val clazz = runtimeMirror(getClass.getClassLoader).runtimeClass(tpe.typeSymbol.asClass)
    lazy val typeArguments = tpe.typeArgs.map(constructType).toArray

    tpe match {
      case t: Type if t == typeOf[Any] ⇒
        getTypeFactory.constructType(classOf[Any])
      case _: Type if clazz.isArray ⇒
        checkArgumentLength(typeArguments.length, 1)
        getTypeFactory.constructArrayType(typeArguments(0))
      case t: Type if t <:< typeOf[collection.Map[_, _]] ⇒
        checkArgumentLength(typeArguments.length, 2)
        getTypeFactory.constructMapLikeType(clazz, typeArguments(0), typeArguments(1))
      case t: Type if t <:< typeOf[collection.Iterable[_]] ⇒
        checkArgumentLength(typeArguments.length, 1)
        getTypeFactory.constructCollectionLikeType(clazz, typeArguments(0))
      case t: Type if t <:< typeOf[Option[_]] ⇒
        checkArgumentLength(typeArguments.length, 1)
        getTypeFactory.constructReferenceType(clazz, typeArguments(0))
      case _: Type ⇒
        getTypeFactory.constructParametricType(clazz, typeArguments: _*)
    }
  }

  def registerCaseClassDeserializer[T: Manifest](): ObjectMapper = {
    val module = new SimpleModule
    module.addDeserializer(manifest[T].runtimeClass.asInstanceOf[Class[T]], new CaseClassDeserializer[T])
    this.registerModule(module)
  }

  override def _findRootDeserializer(ctxt: DeserializationContext, valueType: JavaType): JsonDeserializer[AnyRef] = {
    _caseClassDeserializers.get(valueType) match {
      case Some(deser) => deser
      case None if useCaseClassDeserializer(valueType) =>
        val deser = CaseClassDeserializer.apply(valueType.getRawClass.asInstanceOf[Class[AnyRef]])
        _caseClassDeserializers.put(valueType, deser)
        deser
      case _ => super._findRootDeserializer(ctxt, valueType)
    }
  }

  private[this] def useCaseClassDeserializer(valueType: JavaType) = {
    if (_allCaseClassEnabled) {
      runtimeMirror(getClass.getClassLoader).classSymbol(valueType.getRawClass).isCaseClass
    } else {
      valueType.getRawClass.getAnnotation(classOf[CaseClassDeserialize]) != null
    }
  }

  private[this] val _caseClassDeserializers = mutable.Map[JavaType, JsonDeserializer[AnyRef]]()

}
