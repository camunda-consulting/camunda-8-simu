package org.example.camunda.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.example.camunda.exception.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BpmnUtils {

  private BpmnUtils() {}

  private static final Logger LOG = LoggerFactory.getLogger(BpmnUtils.class);

  private static Node getNodeFromBpmn(String xml, String nodeId) {
    return getNodeFromBpmn(
        new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))), nodeId);
  }

  private static Node getNodeFromBpmn(InputStream bpmnInputStream, String nodeId) {
    try {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = builderFactory.newDocumentBuilder();

      Document xmlDocument = builder.parse(bpmnInputStream);
      return getNodeFromDocument(xmlDocument, nodeId);
    } catch (IOException
        | XPathExpressionException
        | SAXException
        | ParserConfigurationException e) {
      LOG.error("Error reading the requested node id " + nodeId, e);
    }
    return null;
  }

  private static Node getNodeFromDocument(Document xmlDocument, String nodeId)
      throws XPathExpressionException {

    XPath xPath = XPathFactory.newInstance().newXPath();
    String expression = "//*[@id=\"" + nodeId + "\"]";
    NodeList nodeList =
        (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
    if (nodeList != null && nodeList.getLength() == 1) {
      return nodeList.item(0);
    }
    return null;
  }

  public static Map<String, String> getProcessIdAndName(String xml) {
    Document xmlDocument = getXmlDocument(xml);

    for (String tagName : List.of("bpmn2:process", "bpmn:process")) {
      NodeList nodeList = xmlDocument.getElementsByTagName(tagName);
      for (int i = 0; i < nodeList.getLength(); i++) {
        NamedNodeMap attributes = nodeList.item(i).getAttributes();
        return Map.of(
            attributes.getNamedItem("id").getNodeValue(),
            attributes.getNamedItem("name").getNodeValue());
      }
    }
    return null;
  }

  public static String getTaskNameFromBpmn(String xml, String activityId) {
    LOG.info("get TaskName for " + activityId);

    Node taskNode = getNodeFromBpmn(xml, activityId);
    if (taskNode != null) {
      Node taskNameNode = taskNode.getAttributes().getNamedItem("name");
      if (taskNameNode != null) return taskNameNode.getNodeValue();
    }
    return activityId;
  }

  public static String getFormSchemaFromBpmn(String xml, String formId) {
    LOG.info("get TaskName for " + formId);
    String schema = null;
    Node formNode = getNodeFromBpmn(xml, formId);
    if (formNode != null) {
      schema = formNode.getFirstChild().getNodeValue();
    }
    return schema;
  }

  public static String getEmbeddedSartingFormSchema(Document xmlDocument, String formKey) {
    try {
      if (formKey != null) {
        String formId = formKey.substring(formKey.lastIndexOf(":") + 1);
        Node formNode;

        formNode = getNodeFromDocument(xmlDocument, formId);

        if (formNode != null) {
          return formNode.getFirstChild().getNodeValue();
        }
      }
      return null;
    } catch (XPathExpressionException e) {
      return null;
    }
  }

  public static String getEmbeddedStartingFormKey(Document xmlDocument) {
    NodeList nodeList = xmlDocument.getElementsByTagName("bpmn:startEvent");

    if (nodeList != null && nodeList.getLength() >= 1) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node startEvent = nodeList.item(i);
        if (startEvent instanceof Element) {
          NodeList formDefs = ((Element) startEvent).getElementsByTagName("zeebe:formDefinition");
          if (formDefs != null && formDefs.getLength() == 1) {
            Node formKey = formDefs.item(0).getAttributes().getNamedItem("formKey");
            if (formKey != null) return formKey.getNodeValue();
          }
        }
      }
    }
    return null;
  }

  public static List<String> getSubProcessBpmnId(String xml) {
    Document doc = getXmlDocument(xml);
    List<String> result = new ArrayList<>();
    for (String tagName : List.of("bpmn2:callActivity", "bpmn:callActivity")) {
      NodeList nodeList = doc.getElementsByTagName(tagName);
      for (int i = 0; i < nodeList.getLength(); i++) {
        result.add(
            ((Element) nodeList.item(i))
                .getElementsByTagName("zeebe:calledElement")
                .item(0)
                .getAttributes()
                .getNamedItem("processId")
                .getNodeValue());
      }
    }
    return result;
  }

  public static List<String> getServiceTasksElementsId(Document xmlDocument) {
    return getElementsIds(xmlDocument, List.of("bpmn2:serviceTask", "bpmn:serviceTask"));
  }

  public static List<String> getUserTasksElementsId(Document xmlDocument) {
    return getElementsIds(xmlDocument, List.of("bpmn2:userTask", "bpmn:userTask"));
  }

  public static Map<String, String> getTimerCatchEvents(String xml) {
    Document doc = getXmlDocument(xml);
    NodeList nodeList = doc.getElementsByTagName("bpmn:intermediateCatchEvent");
    Map<String, String> timers = new HashMap<>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      NodeList timerList = ((Element) node).getElementsByTagName("bpmn:timerEventDefinition");
      if (timerList.getLength() == 1) {
        String flowNode = node.getAttributes().getNamedItem("id").getNodeValue();
        Element timerDef = ((Element) (timerList.item(0)));
        NodeList durations = timerDef.getElementsByTagName("bpmn:timeDuration");
        if (durations.getLength() == 1) {
          timers.put(flowNode, "duration:" + durations.item(0).getNodeValue());
        } else {
          NodeList dates = timerDef.getElementsByTagName("bpmn:timeDate");
          if (dates.getLength() == 1) {
            timers.put(flowNode, "date:" + dates.item(0).getNodeValue());
          }
        }
      }
    }
    return timers;
  }

  public static List<String> getElementsIds(Document xmlDocument, List<String> tagNames) {
    List<String> result = new ArrayList<>();
    for (String tagName : tagNames) {
      NodeList nodeList = xmlDocument.getElementsByTagName(tagName);
      for (int i = 0; i < nodeList.getLength(); i++) {
        result.add(nodeList.item(i).getAttributes().getNamedItem("id").getNodeValue());
      }
    }
    return result;
  }

  public static List<String> getJobTypes(String xml) {
    return getJobTypes(getXmlDocument(xml));
  }

  public static List<String> getJobTypes(Document xmlDocument) {
    List<String> result = new ArrayList<>();
    result.add("io.camunda.zeebe:userTask");
    NodeList nodeList = xmlDocument.getElementsByTagName("zeebe:taskDefinition");
    for (int i = 0; i < nodeList.getLength(); i++) {
      result.add(nodeList.item(i).getAttributes().getNamedItem("type").getNodeValue());
    }
    return result;
  }

  public static String getLinkedStartingFormId(Document xmlDocument) {
    NodeList nodeList = xmlDocument.getElementsByTagName("bpmn:startEvent");

    if (nodeList != null && nodeList.getLength() >= 1) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node startEvent = nodeList.item(i);
        if (startEvent instanceof Element) {
          NodeList formDefs = ((Element) startEvent).getElementsByTagName("zeebe:formDefinition");
          if (formDefs != null && formDefs.getLength() == 1) {
            Node formId = formDefs.item(0).getAttributes().getNamedItem("formId");
            if (formId != null) return formId.getNodeValue();
          }
        }
      }
    }
    return null;
  }

  public static Document getXmlDocument(String xml) {

    try {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = builderFactory.newDocumentBuilder();

      return builder.parse(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
    } catch (SAXException | IOException | ParserConfigurationException e) {
      return null;
    }
  }

  public static String getAttribute(Node node, String attribute) {
    try {
      return node.getAttributes().getNamedItem(attribute).getTextContent();
    } catch (NullPointerException e) {
      return null;
    }
  }

  public static String getXmlContent(Document doc) {
    try {
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      trans.setOutputProperty(OutputKeys.METHOD, "xml");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(2));

      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(doc.getDocumentElement());

      trans.transform(source, result);
      return sw.toString();
    } catch (TransformerException e) {
      throw new TechnicalException("Error getting xml from doc", e);
    }
  }
}
