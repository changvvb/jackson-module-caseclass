import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import deser.CaseClassDeserializer
import mapper.CaseClassObjectMapper
import org.scalatest.FunSuite


class MyDeserializer extends CaseClassDeserializer[TestCaseClass]
@JsonDeserialize(using = classOf[MyDeserializer])
case class TestCaseClass(intValue:Int,stringValue:String,optionValue:Option[_] = None,seqValue:Seq[_] = Nil)

class CaseClassDeserializerTest extends FunSuite {

  val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
  mapper.registerModule(DefaultScalaModule)

  val json =
    """
      |{
      | "intValue": 3,
      | "stringValue": "test"
      |}
    """.stripMargin

  test("json deserializer") {
    val obj = mapper.readValue(json,classOf[TestCaseClass])
    assert(obj.intValue == 3)
    assert(obj.stringValue == "test")
    assert(obj.optionValue.isEmpty)
    assert(obj.seqValue.isEmpty)
  }

}
