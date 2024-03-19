package org.example.camunda.utils;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.io.SAXReader;

public class MigrationUtils {

  private static final String MIGRATION_BLOCKS =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
          + "<root xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:zeebe=\"http://camunda.org/schema/zeebe/1.0\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\">"
          + "<bpmn:sequenceFlow id=\"migrateFlow\" sourceRef=\"${StartEventRef}\" targetRef=\"migrate_evt\" />"
          + "<bpmn:intermediateThrowEvent id=\"migrate_evt\" name=\"Migrate\">\r\n"
          + "      <bpmn:extensionElements>\r\n"
          + "        <zeebe:taskDefinition type=\"migrate\" />\r\n"
          + "      </bpmn:extensionElements>\r\n"
          + "      <bpmn:incoming>migrateFlow</bpmn:incoming>\r\n"
          + "      <bpmn:outgoing>${startSequenceFlow}</bpmn:outgoing>\r\n"
          + "      <bpmn:messageEventDefinition id=\"MessageEventDefinition_0x0yn5b\" />\r\n"
          + "</bpmn:intermediateThrowEvent>"
          + "<bpmndi:BPMNShape id=\"migrate_evt_di\" bpmnElement=\"migrate_evt\">\r\n"
          + "        <dc:Bounds x=\"${migrateEvtX}\" y=\"${migrateEvtY}\" width=\"36\" height=\"36\" />\r\n"
          + "        <bpmndi:BPMNLabel>\r\n"
          + "          <dc:Bounds x=\"${migrateEvtX}\" y=\"${migrateLabelY}\" width=\"38\" height=\"14\" />\r\n"
          + "        </bpmndi:BPMNLabel>\r\n"
          + "</bpmndi:BPMNShape>\r\n"
          + "<bpmndi:BPMNEdge id=\"migrate_flow_di\" bpmnElement=\"migrateFlow\">\r\n"
          + "        <di:waypoint x=\"${migrateFlowStartX}\" y=\"${migrateFlowY}\" />\r\n"
          + "        <di:waypoint x=\"${migrateEvtX}\" y=\"${migrateFlowY}\" />\r\n"
          + "</bpmndi:BPMNEdge>"
          + "</root>";

  private MigrationUtils() {}

  public static String insertMigrationStep(String xmlProcessDef) throws DocumentException {
    DOMDocumentFactory factory = new DOMDocumentFactory();
    SAXReader reader = new SAXReader();
    reader.setDocumentFactory(factory);

    Document doc = reader.read(new StringReader(xmlProcessDef));
    Element process = doc.getRootElement().element("process");
    List<Element> startEvents = process.elements("startEvent");
    Element startEvent = null;
    String flowName = null;
    for (Element startCandidate : startEvents) {
      List<Element> outgoing = startCandidate.elements("outgoing");
      List<Element> extensionElements = startCandidate.elements("extensionElements");

      if (startCandidate.elements().size() == (extensionElements.size() + outgoing.size())) {
        startEvent = startCandidate;
        flowName = outgoing.get(0).getStringValue();
        break;
      }
    }
    if (startEvent == null) {
      throw new DocumentException("Start Event count not be found");
    }
    List<Element> flowElts = process.elements("sequenceFlow");
    Element flowElt = null;
    for (Element flowEltCandidate : flowElts) {
      if (flowEltCandidate.attribute("id").getValue().equals(flowName)) {
        flowElt = flowEltCandidate;
        break;
      }
    }
    Element bpmnPlane = doc.getRootElement().element("BPMNDiagram").element("BPMNPlane");
    List<Element> bpmnShapes = bpmnPlane.elements("BPMNShape");
    Element startEltShape = null;
    for (Element shapeCandidate : bpmnShapes) {
      if (shapeCandidate
          .attribute("bpmnElement")
          .getValue()
          .equals(startEvent.attribute("id").getValue())) {
        startEltShape = shapeCandidate;
        break;
      }
    }
    List<Element> bpmnEdges = bpmnPlane.elements("BPMNEdge");
    Element flowEltShape = null;
    for (Element edgeCandidate : bpmnEdges) {
      if (edgeCandidate.attribute("bpmnElement").getValue().equals(flowName)) {
        flowEltShape = edgeCandidate;
        break;
      }
    }
    Element startEltBounds = startEltShape.element("Bounds");
    StringSubstitutor sub =
        new StringSubstitutor(
            Map.of(
                "StartEventRef",
                startEvent.attribute("id").getValue(),
                "startSequenceFlow",
                flowName,
                "migrateEvtX",
                startEltBounds.attribute("x").getValue(),
                "migrateEvtY",
                startEltBounds.attribute("y").getValue(),
                "migrateLabelY",
                String.valueOf(Long.valueOf(startEltBounds.attribute("y").getValue()) + 45),
                "migrateFlowStartX",
                String.valueOf(Long.valueOf(startEltBounds.attribute("x").getValue()) - 50),
                "migrateFlowY",
                String.valueOf(Long.valueOf(startEltBounds.attribute("y").getValue()) + 18)));
    String addElts = sub.replace(MIGRATION_BLOCKS);
    Document migrationElts = reader.read(new StringReader(addElts));
    Element sequenceFlow = migrationElts.getRootElement().element("sequenceFlow");
    sequenceFlow.detach();
    process.add(sequenceFlow);
    Element migrateEvt = migrationElts.getRootElement().element("intermediateThrowEvent");
    migrateEvt.detach();
    process.add(migrateEvt);
    Element bpmnShape = migrationElts.getRootElement().element("BPMNShape");
    bpmnShape.detach();
    bpmnPlane.add(bpmnShape);
    Element bpmnEdge = migrationElts.getRootElement().element("BPMNEdge");
    bpmnEdge.detach();
    bpmnPlane.add(bpmnEdge);
    flowElt.attribute("sourceRef").setValue("migrate_evt");
    Long startEventX = Long.valueOf(startEltBounds.attribute("x").getValue()) - 86;
    startEltBounds.attribute("x").setValue(String.valueOf(startEventX));
    if (startEltShape.elements("BPMNLabel").size() > 0) {
      startEltShape
          .element("BPMNLabel")
          .element("Bounds")
          .attribute("x")
          .setValue(String.valueOf(startEventX));
    }
    startEvent.elements("outgoing").get(0).setText("migrateFlow");
    return doc.asXML();
  }
}
