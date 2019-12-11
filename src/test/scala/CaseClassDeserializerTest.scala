import annotation.CaseClassDeserialize
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import deser.CaseClassDeserializer
import mapper.CaseClassObjectMapper
import org.scalatest.FunSuite

@CaseClassDeserialize()
case class CaseClass1(@JsonDeserialize(using = classOf[TestDeserializer]) intValue:Int)

class MyDeserializer extends CaseClassDeserializer[TestCaseClass]


@CaseClassDeserialize()
case class TestCaseClass(
                          intValue:Int,
                          stringValue:String,
                          optionValue:Option[_] = None,
                          seqValue:Seq[_] = Nil,
                          @JsonDeserialize(using = classOf[TestDeserializer2]) caseClassValue:CaseClass1  = null)

class CaseClassDeserializerTest extends FunSuite {

  val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
  mapper.registerModule(DefaultScalaModule)

  val json =
    """
      |{
      | "intValue": 3,
      | "stringValue": "test",
      | "caseClassValue": "dfdf"
      |}
    """.stripMargin

  test("json deserializer") {
    val obj = mapper.readValue(json,classOf[TestCaseClass])
    assert(obj.intValue == 3)
    assert(obj.stringValue == "test")
    assert(obj.optionValue.isEmpty)
    assert(obj.seqValue.isEmpty)
    assert(obj.caseClassValue.intValue == 333)
  }

  test("default") {
    val json =
      """
        |{
        | "intValue": 3
        |}
      """.stripMargin
    val obj = mapper.readValue(json,classOf[CaseClass1])
    assert(obj.intValue == 9999)
  }

}

