# jackson-module-caseclass

## Features
- If one field present in json, just deserialize it.
- If one field not present in json, but the case class has default value, deserialize it as the case class default value.
- If one field not present in json, and the case class doesn't has default value, deserialize it as a zero value.
- Use scala reflect instead of java reflect to constract JavaType, so that jackson-module-caseclass can extract type parameter correctly https://github.com/FasterXML/jackson-module-scala/issues/62.

### Zero value
For some scala type, if jackson-module-caseclass doesn't know what a field value should be set, it will be deserialized as a zero value so that we can avoid NullPointerException at most case.
- Number(Int, Long, Char ...): 0
- Boolean: false
- Option: None
- ~~collection.Map: Map.empty~~(you should set it as default value in case class definition)
- ~~Iterable: Nil~~(same as above)

## Dependency

### sbt
```scala
libraryDependencies += "com.github.changvvb" %% "jackson-module-caseclass" % "1.1.0"
```

## Usage

### 1. Use @CaseClassDeserialize

```scala
import com.fasterxml.jackson.module.caseclass.annotation.CaseClassDeserialize
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.caseclass.mapper.CaseClassObjectMapper

@CaseClassDeserialize()
case class TestCaseClass(
  intValue:Int,
  stringValue:String,
  seqValue:Seq[_],
  optionValue:Option[_] = None)

val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper

val json =
  """
    |{
    | "intValue": 3,
    | "stringValue": "some strings"
    |}
  """.stripMargin


mapper.readValue[TestCaseClass](json)
```

### 2. Use @JsonDeserialize
```scala
import com.fasterxml.jackson.module.caseclass.deser.CaseClassDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.caseclass.mapper.CaseClassObjectMapper

class MyDeserializer extends CaseClassDeserializer[TestCaseClass]

@JsonDeserialize(using = classOf[MyDeserializer])
case class TestCaseClass(
  intValue:Int,
  stringValue:String,
  seqValue:Seq[_],
  optionValue:Option[_] = None)
  
val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
  
val json =
  """
    |{
    | "intValue": 3,
    | "stringValue": "some strings"
    |}
  """.stripMargin


mapper.readValue[TestCaseClass](json)
```

### 3. Use registerCaseClassDeserializer()
```scala
import com.fasterxml.jackson.module.caseclass.deser.CaseClassDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.caseclass.mapper.CaseClassObjectMapper

case class TestCaseClass(
  intValue:Int,
  stringValue:String,
  seqValue:Seq[_],
  optionValue:Option[_] = None)
  
val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
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

### 4. Enable all case class
```scala
import com.fasterxml.jackson.module.caseclass.deser.CaseClassDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.caseclass.mapper.CaseClassObjectMapper

case class TestCaseClass(
  intValue:Int,
  stringValue:String,
  seqValue:Seq[_],
  optionValue:Option[_] = None)
  
val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
mapper.setAllCaseClassEnabled(true)
val json =
  """
    |{
    | "intValue": 3,
    | "stringValue": "some strings"
    |}
  """.stripMargin

mapper.readValue[TestCaseClass](json)
```
