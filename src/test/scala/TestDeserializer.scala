import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class TestDeserializer extends StdDeserializer[Int](classOf[Int]) {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Int = {
    println("using TestDeserializer")
    9999
  }
}

class TestDeserializer2 extends StdDeserializer[CaseClass1](classOf[CaseClass1]) {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): CaseClass1 = {
    println("using TestDeserializer")
    new CaseClass1(333)
  }
}

