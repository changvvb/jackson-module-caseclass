# jackson-module-caseclass

### Usage

#### 1. use @CaseClassDeserialize

```scala
import com.fasterxml.jackson.module.annotation.CaseClassDeserialize
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.mapper.CaseClassObjectMapper

@CaseClassDeserialize()
case class TestCaseClass(
  intValue:Int,
  stringValue:String,
  seqValue:Seq[_],
  optionValue:Option[_] = None)  
  
val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
mapper.registerModule(DefaultScalaModule)

val json =
  """
    |{
    | "intValue": 3,
    | "stringValue": "some strings"
    |}
  """.stripMargin


mapper.readValue[TestCaseClass](json)
```

#### 2. use @JsonDeserialize
```scala
import com.fasterxml.jackson.module.deser.CaseClassDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.mapper.CaseClassObjectMapper

class MyDeserializer extends CaseClassDeserializer[TestCaseClass]

@JsonDeserialize(using = classOf[MyDeserializer])
case class TestCaseClass(
  intValue:Int,
  stringValue:String,
  seqValue:Seq[_],
  optionValue:Option[_] = None)
  
val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
mapper.registerModule(DefaultScalaModule)  
  
val json =
  """
    |{
    | "intValue": 3,
    | "stringValue": "some strings"
    |}
  """.stripMargin


mapper.readValue[TestCaseClass](json)
```

#### 3. use registerCaseClassDeserializer()
```scala
import com.fasterxml.jackson.module.deser.CaseClassDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.mapper.CaseClassObjectMapper

case class TestCaseClass(
  intValue:Int,
  stringValue:String,
  seqValue:Seq[_],
  optionValue:Option[_] = None)
  
val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
mapper.registerModule(DefaultScalaModule)
mapper.registerCaseClassDeserializer[TestCaseClass]()  
  
val json =
  """
    |{
    | "intValue": 3,
    | "stringValue": "some strings"
    |}
  """.stripMargin


mapper.readValue[TestCaseClass](json)
```

