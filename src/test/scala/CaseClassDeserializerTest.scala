import com.fasterxml.jackson.databind.ObjectMapper
import mapper.CaseClassObjectMapper

case class TestCaseClass(intValue:Int,stringValue:Int,optionValue:Option[_] = None)

class CaseClassDeserializerTest extends SerializerTest {

  val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
  val json =
    """
      |{
      | "intValue": "3",
      | "stringValue": "test"
      |}
    """.stripMargin


}
