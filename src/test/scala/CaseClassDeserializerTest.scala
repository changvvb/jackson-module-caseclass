import CaseClassDeserializerTest.StaticInnerClass
import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.caseclass.mapper.CaseClassObjectMapper
import org.scalatest.FunSuite

case class CaseClassField(intValue: Int)

case class TestClassWithDefault(intValue: Int, seqValue: Seq[String] = Nil)

@CaseClassDeserialize()
case class CaseClass1(@JsonDeserialize(using = classOf[TestDeserializer]) intValue: Int)

@CaseClassDeserialize()
case class TestCaseClass(
  intValue: Int,
  stringValue: String,
  optionValue: Option[_] = None,
  seqValue: Seq[_] = Nil,
  @JsonDeserialize(using = classOf[TestDeserializer2]) caseClassValue: CaseClass1 = None.orNull)

@CaseClassDeserialize
case class Test(x: Int, y: String = "sss", z: Seq[Int])

class CaseClassDeserializerTest extends FunSuite {

  val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper

  val json =
    """
      |{
      | "intValue": 3,
      | "stringValue": "test",
      | "optionValue": "something",
      | "seqValue": [1, 2, 3]
      |}
    """.stripMargin

  test("json deserializer") {
    val obj = mapper.readValue(json, classOf[TestCaseClass])
    assert(obj.intValue == 3)
    assert(obj.stringValue == "test")
    assert(obj.optionValue.contains("something"))
    assert(obj.seqValue == Seq(1, 2, 3))
    assert(obj.caseClassValue.intValue == TestDeserializer2.value)
  }

  test("default") {
    val json = "{}"
    val obj = mapper.readValue(json, classOf[TestCaseClass])
    assert(obj.intValue == 0)
    assert(obj.stringValue == null)
    assert(obj.optionValue.isEmpty)
    assert(obj.seqValue.isEmpty)
  }

  test("other") {
    val json =
      """
        |{
        | "x": 89
        |}
      """.stripMargin
    val obj = mapper.readValue[Test](json)
    assert(obj.x == 89)
    assert(obj.y == "sss")
    assert(obj.z == null)
  }

  test("annotation") {
    val json =
      """
        |{
        | "intValue": 2
        |}
      """.stripMargin
    val obj = mapper.readValue[CaseClass1](json)
    assert(obj.intValue == TestDeserializer.value)
  }

  test("static inner class") {
    val json =
      """
        |{
        | "intValue": 2
        |}
      """.stripMargin
    val obj2 = mapper.readValue[StaticInnerClass](json)
    assert(obj2.seqValue == Nil)
  }

  test("all case class enabled") {
    val json =
      """
        |{
        | "intValue": 2
        |}
      """.stripMargin
    val mapper2 = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
    val obj = mapper2.readValue[TestClassWithDefault](json)
    assert(obj.seqValue == null)

    mapper2.setAllCaseClassEnabled(true)
    val obj2 = mapper2.readValue[TestClassWithDefault](json)
    assert(obj2.seqValue == Nil)
  }
}

object CaseClassDeserializerTest {
  @CaseClassDeserialize()
  case class StaticInnerClass(intValue: Int, seqValue: Seq[String] = Nil)
}
