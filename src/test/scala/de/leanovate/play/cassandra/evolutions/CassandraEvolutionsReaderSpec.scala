package de.leanovate.play.cassandra.evolutions

import java.io.{ByteArrayInputStream, File}

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, MustMatchers}
import play.api.Environment

class CassandraEvolutionsReaderSpec extends FlatSpec with MockitoSugar with MustMatchers {

  it should "read cql files via environment file" in new WithMocks {

    when(environment.getExistingFile(anyString())).thenReturn(Some(new File("./build.sbt")))

    val result = reader.loadResource("fixture", 1)

    result mustBe defined

    verify(environment).getExistingFile("conf/evolutions/fixture/1.cql")
    verifyNoMoreInteractions(environment)
  }

  it should "read cql files via environment resource" in new WithMocks {

    when(environment.getExistingFile(anyString())).thenReturn(None)
    when(environment.resourceAsStream(anyString())).thenReturn(Some(new ByteArrayInputStream(Array[Byte]())))

    val result = reader.loadResource("fixture", 1)

    result mustBe defined

    verify(environment).getExistingFile("conf/evolutions/fixture/1.cql")
    verify(environment).resourceAsStream("evolutions/fixture/1.cql")
    verifyNoMoreInteractions(environment)
  }

  trait WithMocks {
    val environment = mock[Environment]

    val reader = new CassandraEvolutionsReader(environment)
  }

}
