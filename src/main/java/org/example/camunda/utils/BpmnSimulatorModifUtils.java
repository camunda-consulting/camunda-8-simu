package org.example.camunda.utils;

import java.util.HashMap;
import java.util.Map;
import org.example.camunda.Constants;
import org.example.camunda.dto.ExecutionPlan;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BpmnSimulatorModifUtils {

  private static Map<String, Map<String, String>> planXmls = new HashMap<>();

  public static void prepareSimulation(ExecutionPlan plan) {
    keepOriginals(plan);
    fakeConnectorsAndWorkers(plan);
    propagateUniqueIdIfNecessary(plan);
    removeLinkedForms(plan);
    addProcessEndListener(plan);
  }

  private static void addProcessEndListener(ExecutionPlan plan) {
    String content = plan.getXml();
    Document doc = BpmnUtils.getXmlDocument(content);
    NodeList processes = doc.getElementsByTagName("bpmn:process");
    for (int i = 0; i < processes.getLength(); i++) {
      Node process = processes.item(i);
      String id = BpmnUtils.getAttribute(process, "id");
      if (plan.getName().startsWith(id)) {
        NodeList extensionElements =
            ((Element) process).getElementsByTagName("bpmn:extensionElements");
        Element extension = null;
        for (int u = 0; u < extensionElements.getLength(); u++) {
          Node extensionElement = extensionElements.item(u);
          if (extensionElement.getParentNode().equals(process)) {
            extension = (Element) extensionElement;
          }
        }
        if (extension == null) {
          extension = doc.createElement("bpmn:extensionElements");
          Element proc = (Element) process;
          Node firstChild = proc.getFirstChild();
          proc.insertBefore(extension, firstChild);
        }
        NodeList executionListeners = extension.getElementsByTagName("bpmn:executionListeners");
        Element execListener = null;
        if (executionListeners.getLength() == 0) {
          execListener = doc.createElement("zeebe:executionListeners");
          extension.appendChild(execListener);
        } else {
          execListener = (Element) executionListeners.item(0);
        }
        Element startEventListener = doc.createElement("zeebe:executionListener");
        startEventListener.setAttribute("eventType", "start");
        startEventListener.setAttribute("retries", "3");
        startEventListener.setAttribute("type", "startEventListener");
        execListener.appendChild(startEventListener);

        Element processTerminatedListener = doc.createElement("zeebe:executionListener");
        processTerminatedListener.setAttribute("eventType", "end");
        processTerminatedListener.setAttribute("retries", "3");
        processTerminatedListener.setAttribute("type", "processTerminated");
        execListener.appendChild(processTerminatedListener);
      }
    }
    plan.setXml(BpmnUtils.getXmlContent(doc));
  }

  public static void keepOriginals(ExecutionPlan plan) {
    if (!planXmls.containsKey(plan.getDefinition().getBpmnProcessId())) {
      planXmls.put(plan.getDefinition().getBpmnProcessId(), new HashMap<>());
    }
    planXmls.get(plan.getDefinition().getBpmnProcessId()).put("0", plan.getXml());
    if (plan.getXmlDependencies() != null) {
      for (String dep : plan.getXmlDependencies().keySet()) {
        planXmls
            .get(plan.getDefinition().getBpmnProcessId())
            .put(dep, plan.getXmlDependencies().get(dep));
      }
    }
  }

  public static void revertToInitial(ExecutionPlan plan) {
    plan.setXml(planXmls.get(plan.getDefinition().getBpmnProcessId()).get("0"));
    if (plan.getXmlDependencies() != null) {
      for (String dep : plan.getXmlDependencies().keySet()) {
        plan.getXmlDependencies()
            .put(dep, planXmls.get(plan.getDefinition().getBpmnProcessId()).get(dep));
      }
    }
  }

  public static String fakeConnectorsAndWorkers(String xml) {
    return xml.replaceAll("taskDefinition type=\"", "taskDefinition type=\"datasimulator.");
  }

  public static void fakeConnectorsAndWorkers(ExecutionPlan plan) {
    plan.setXml(fakeConnectorsAndWorkers(plan.getXml()));
    if (plan.getXmlDependencies() != null) {
      for (String dep : plan.getXmlDependencies().keySet()) {
        plan.getXmlDependencies()
            .put(dep, fakeConnectorsAndWorkers(plan.getXmlDependencies().get(dep)));
      }
    }
  }

  public static void propagateUniqueIdIfNecessary(ExecutionPlan plan) {
    plan.setXml(propagateUniqueIdIfNecessary(plan.getXml()));
    if (plan.getXmlDependencies() != null) {
      for (String dep : plan.getXmlDependencies().keySet()) {
        plan.getXmlDependencies()
            .put(dep, propagateUniqueIdIfNecessary(plan.getXmlDependencies().get(dep)));
      }
    }
  }

  public static String propagateUniqueIdIfNecessary(String xml) {
    return xml.replaceAll(
        "propagateAllParentVariables=\"false\" />[\\r\\n\\s]*<zeebe:ioMapping>",
        "$0<zeebe:input source=\"="
            + Constants.UNIQUE_ID_KEY
            + "\" target=\""
            + Constants.UNIQUE_ID_KEY
            + "\" />");
  }

  public static void removeLinkedForms(ExecutionPlan plan) {
    plan.setXml(switchUserTasksToWorkers(removeLinkedForms(plan.getXml())));
    if (plan.getXmlDependencies() != null) {
      for (String dep : plan.getXmlDependencies().keySet()) {
        plan.getXmlDependencies()
            .put(
                dep,
                switchUserTasksToWorkers(removeLinkedForms(plan.getXmlDependencies().get(dep))));
      }
    }
  }

  public static String removeLinkedForms(String xml) {
    return xml.replaceAll("<zeebe:formDefinition formId=\"[a-zA-Z0-9\\_\\-]*\" />[\\r\\n\\s]*", "");
  }

  public static String switchUserTasksToWorkers(String xml) {
    return xml.replaceAll("<zeebe:userTask[\s]*/>", "");
  }

  /*
   Easier to manage simulation sequentially

  public static void updateCallActivityVersion(
      ExecutionPlan plan, Map<String, Integer> deployedVersion) {
    // TODO : should be executed on every xml, not on the main only
    String xml = plan.getXml();
    for (Map.Entry<String, Integer> deployed : deployedVersion.entrySet()) {
      xml =
          xml.replace(
              "<zeebe:calledElement processId=\"" + deployed.getKey() + "\"",
              "<zeebe:calledElement processId=\""
                  + deployed.getKey()
                  + "\" versionTag=\""
                  + deployed.getValue()
                  + "\"");
    }
    plan.setXml(xml);
  }
   */
}
