import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class TestDeserializer extends StdDeserializer[Int](classOf[Int]) {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Int = {
    println("using TestDeserializer")
    TestDeserializer.defaultValue
  }
}

object TestDeserializer {
  val defaultValue = 999
}

class CaseClassFieldDeserializer extends StdDeserializer[CaseClassField](classOf[CaseClassField]) {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): CaseClassField = {
    println("using CaseClassFieldDeserializer")
    new CaseClassField(CaseClassFieldDeserializer.defaultValue)
  }
}

object CaseClassFieldDeserializer {
  val defaultValue = 333
}

