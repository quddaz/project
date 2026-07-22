package com.woowapractice.grading;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Component
public class JunitXmlResultParser {

  public List<TestResultValue> parse(InputStream inputStream) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      var document = factory.newDocumentBuilder().parse(inputStream);
      NodeList testCases = document.getElementsByTagName("testcase");
      List<TestResultValue> results = new ArrayList<>();
      for (int index = 0; index < testCases.getLength(); index++) {
        Element testCase = (Element) testCases.item(index);
        String identifier =
            testCase.getAttribute("classname") + "#" + testCase.getAttribute("name");
        String displayName = testCase.getAttribute("name");
        long durationMillis = parseDurationMillis(testCase.getAttribute("time"));
        Element failure = child(testCase, "failure");
        Element skipped = child(testCase, "skipped");
        TestResultStatus status = TestResultStatus.PASSED;
        String failureMessage = null;
        if (failure != null) {
          status = TestResultStatus.FAILED;
          failureMessage = failure.getAttribute("message");
          if (failureMessage.isBlank()) {
            failureMessage = failure.getTextContent();
          }
        } else if (skipped != null) {
          status = TestResultStatus.SKIPPED;
        }
        results.add(
            new TestResultValue(identifier, displayName, status, durationMillis, failureMessage));
      }
      return results;
    } catch (Exception exception) {
      throw new IllegalArgumentException("JUnit XML 결과를 읽을 수 없습니다.", exception);
    }
  }

  private Element child(Element parent, String name) {
    NodeList children = parent.getElementsByTagName(name);
    return children.getLength() == 0 ? null : (Element) children.item(0);
  }

  private long parseDurationMillis(String seconds) {
    if (seconds == null || seconds.isBlank()) {
      return 0L;
    }
    return Math.round(Double.parseDouble(seconds) * 1000);
  }
}
