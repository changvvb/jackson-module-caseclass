import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.caseclass.mapper.CaseClassObjectMapper
import org.scalatest.FunSuite

case class CaseClassField(@JsonDeserialize(using = classOf[TestDeserializer]) intValue:Int)

@CaseClassDeserialize()
case class TestCaseClass(
                          intValue:Int,
                          stringValue:String,
                          optionValue:Option[_] = None,
                          seqValue:Seq[_] = Nil,
                          @JsonDeserialize(using = classOf[CaseClassFieldDeserializer]) caseClassValue:CaseClassField  = null)

class CaseClassDeserializerTest extends FunSuite {

  val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
  mapper.registerModule(DefaultScalaModule)

  val json =
    """
      |{
      | "intValue": 3,
      | "stringValue": "test",
      | "optionValue": "something",
      | "seqValue": [1, 2, 3],
      | "caseClassValue": "anything"
      |}
    """.stripMargin

  test("json deserializer") {
    val obj = mapper.readValue(json,classOf[TestCaseClass])
    assert(obj.intValue == 3)
    assert(obj.stringValue == "test")
    assert(obj.optionValue.contains("something"))
    assert(obj.seqValue == Seq(1,2,3))
    assert(obj.caseClassValue.intValue == 333)
  }

  test("default") {
    val json = "{}"
    val obj = mapper.readValue(json,classOf[TestCaseClass])
    assert(obj.intValue == 0)
    assert(obj.stringValue == null)
    assert(obj.optionValue.isEmpty)
    assert(obj.seqValue.isEmpty)
    assert(obj.caseClassValue == null)
  }
}

