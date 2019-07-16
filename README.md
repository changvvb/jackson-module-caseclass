# jackson-module-caseclass

### Usage

#### use @CaseClassDeserialize


```scala
import annotation.CaseClassDeserialize
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import mapper.CaseClassObjectMapper

@CaseClassDeserialize()
case class TestCaseClass(
  intValue:Int,
  stringValue:String,
  optionValue:Option[_] = None)
  
val mapper = new ObjectMapper with ScalaObjectMapper with CaseClassObjectMapper
mapper.registerModule(DefaultScalaModule)
```
