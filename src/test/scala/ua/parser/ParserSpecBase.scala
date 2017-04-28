package ua.parser

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import org.yaml.snakeyaml.Yaml
import java.util.{ List => JList, Map => JMap }

import org.scalatest.FunSpec

import scala.collection.JavaConverters._

trait ParserSpecBase extends FunSpec {

  val parser: UserAgentStringParser

  describe("Parser") {
    val yaml = new Yaml()

    it("should parse basic ua") {
      val cases = List(
        "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; fr; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5 ,gzip(gfe),gzip(gfe)" ->
          Client(UserAgent("Firefox", Some("3"), Some("5"), Some("5")), OS("Mac OS X", Some("10"), Some("4")), Device("Other")),
        "Mozilla/5.0 (iPhone; CPU iPhone OS 5_1_1 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B206 Safari/7534.48.3" ->
          Client(UserAgent("Mobile Safari", Some("5"), Some("1")), OS("iOS", Some("5"), Some("1"), Some("1")), Device("iPhone"))
      )
      cases.foreach {
        case (agent, expected) => assert(parser.parse(agent) === expected)
      }
    }

    it("should properly quote replacements") {
      val testConfig =
        """
          |user_agent_parsers:
          |  - regex: 'ABC([\\0-9]+)'
          |    family_replacement: 'ABC ($1)'
          |os_parsers:
          |  - regex: 'CatOS OH-HAI=/\^\.\^\\='
          |    os_replacement: 'CatOS 9000'
          |device_parsers:
          |  - regex: 'CashPhone-([\$0-9]+)\.(\d+)\.(\d+)'
          |    device_replacement: 'CashPhone $1'
        """.stripMargin
      val stream = new ByteArrayInputStream(testConfig.getBytes(StandardCharsets.UTF_8))
      val parser = Parser.create(stream)
      val client = parser.parse("""ABC12\34 (CashPhone-$9.0.1 CatOS OH-HAI=/^.^\=)""")
      assert(client.userAgent.family === """ABC (12\34)""")
      assert(client.os.family === "CatOS 9000")
      assert(client.device.family === "CashPhone $9")
    }

    it("should properly parse user agents") {
      List("/tests/test_ua.yaml", "/test_resources/firefox_user_agent_strings.yaml",
        "/test_resources/pgts_browser_list.yaml").foreach { file =>
          readCasesConfig(file).foreach { c =>
            assert(parser.parse(c("user_agent_string")).userAgent === UserAgent.fromMap(c).get)
          }
        }
    }

    it("should properly parse os") {
      List("/tests/test_os.yaml", "/test_resources/additional_os_tests.yaml").foreach { file =>
        readCasesConfig(file).foreach { c =>
          assert(parser.parse(c("user_agent_string")).os === OS.fromMap(c).get)
        }
      }
    }

    it("should properly parse device") {
      readCasesConfig("/tests/test_device.yaml").foreach { c =>
        assert(parser.parse(c("user_agent_string")).device === Device.fromMap(c).get)
      }
    }

    def readCasesConfig(resource: String) = {
      val stream = this.getClass.getResourceAsStream(resource)
      val cases = yaml.load(stream).asInstanceOf[JMap[String, JList[JMap[String, String]]]]
        .asScala.toMap.mapValues(_.asScala.toList.map(_.asScala.toMap))
      cases.getOrElse("test_cases", List()).filterNot(_.contains("js_ua")).map { config =>
        config.filterNot { case (_, value) => value == null }
      }
    }
  }
}
