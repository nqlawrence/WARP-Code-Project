/**
 * 
 */
package edu.uiowa.cs.warp;

import edu.uiowa.cs.utilities.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
// import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Build the nodes and flows for the workload described in the workload description file, whose name
 * is passed into the Constructor via the parameter inputFileName. Good default values for the
 * constructors are m = 0.9, e2e = 0.99, and numFaults = 1 when the second constructor is used.
 * 
 * @author sgoddard
 * @version 1.4
 *
 */

public class WorkLoad extends WorkLoadDescription implements ReliabilityParameters {

  private static final Integer DEFAULT_PRIORITY = 0;
  private static final Integer DEFAULT_INDEX = 0;
  private static final Integer DEFAULT_TX_NUM = 0;
  private static final String FLOW_WARNING =
      "\n\tWarning! Bad situation: " + "Flow %s doesn't exist but trying to ";

  private Integer numFaults = 0;
  private Double minPacketReceptionRate = 0.0;
  private Double e2e = 0.0;
  private Boolean intForNodeNames = false;
  private Boolean intForFlowNames = false;
  private FlowMap flows; // map of all flow nodes in the WARP graph (<name, Flow>)
  // private Integer nFlows = 0;
  private NodeMap nodes; // map of all graph nodes in the WARP graph (<name, Node>)
  private String name; // name of the WARP graph defining the workload
  private ArrayList<String> flowNamesInOriginalOrder = new ArrayList<>(); // array to hold names of
                                                                          // flows to preserve their
                                                                          // order
  private ArrayList<String> flowNamesInPriorityOrder = new ArrayList<>();
  // private FileManager fm;
  
  /**
   * This ReliabilityAnalysis object is instantiated with either numFaults or e2e and minPacketReceptionRate depending
   * on whether the user wants getFixedTxPerLinkAndTotalTxCost or getNumTxAttemptsPerLinkAndTotalTxAttempts.
   * 
   */
  private ReliabilityAnalysis ra;

  /**
   * This method takes the below parameters and constructs nodes and flows using the
   * WorkloadListener build nodes and flows function.
   *  
   * @param m is a double that represents the minimum packet reception rate
   * @param e2e is a double that populates this flow object as the input is read
   * @param inputFileName is a String that represents the name of the file used to create a flow.
   */
  WorkLoad(Double m, Double e2e, String inputFileName) {
    super(inputFileName);
    setDefaultParameters();
    minPacketReceptionRate = m; // use file manager passed to this object
    this.e2e = e2e; // use populate this flows object as the input file is read
    /*
     * Read input file, build the AST of graph and the listener will build the node and flow data
     * objects
     */
    WorkLoadListener.buildNodesAndFlows(this);
  }

  /**
   * This method takes the below parameters and constructs nodes and flows using the
   * WorkloadListener build nodes and flows function.
   * 
   * @param numFaults is an Integer that represents the number of faults in a flow.
   * @param m is a double that represents the minimum packet reception rate
   * @param e2e is a double that populates this flow object as the input is read
   * @param inputFileName is a String that represents the name of the file used to create a flow.
   */
  WorkLoad(Integer numFaults, Double m, Double e2e, String inputFileName) {
    super(inputFileName);
    setDefaultParameters();
    this.numFaults = numFaults;
    minPacketReceptionRate = m; // use file manager passed to this object
    this.e2e = e2e; // use populate this flows object as the input file is read
    /*
     * Read input file, build the AST of graph and the listener will build the node and flow data
     * objects
     */
    WorkLoadListener.buildNodesAndFlows(this);
  }

  private void setDefaultParameters() {
    intForNodeNames = true; // default is that node names are all alpha names
    intForFlowNames = true; // default is that node names are all alpha names
    flows = new FlowMap(); // map of all flow nodes in the WARP graph (<name, Flow>)
    nodes = new NodeMap(); // map of all graph nodes in the WARP graph (<name, Node>)
    flowNamesInOriginalOrder = new ArrayList<>(); // array to hold names of flows to preserve their
                                                  // order
    flowNamesInPriorityOrder = new ArrayList<>();
    numFaults = DEFAULT_TX_NUM;
  }

  /**
   * @return the numFaults
   */
  public Integer getNumFaults() {
    return numFaults;
  }

  /**
   * @return the minPacketReceptionRate
   */
  public Double getMinPacketReceptionRate() {
    return minPacketReceptionRate;
  }

  /**
   * @return the e2e
   */
  public Double getE2e() {
    return e2e;
  }

  /**
   * @return the intForNodeNames
   */
  public Boolean getIntForNodeNames() {
    return intForNodeNames;
  }

  /**
   * @return the intForFlowNames
   */
  public Boolean getIntForFlowNames() {
    return intForFlowNames;
  }

  /**
   * @return the flows
   */
  public FlowMap getFlows() {
    return flows;
  }

  /**
   * @return the nodes
   */
  public NodeMap getNodes() {
    return nodes;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the flowNamesInOriginalOrder
   */
  public ArrayList<String> getFlowNamesInOriginalOrder() {
    return flowNamesInOriginalOrder;
  }

  /**
   * @return the flowNamesInPriorityOrder
   */
  public ArrayList<String> getFlowNamesInPriorityOrder() {
    return flowNamesInPriorityOrder;
  }

  /**
   * @param minPacketReceptionRate the minPacketReceptionRate to set
   */
  public void setMinPacketReceptionRate(Double minPacketReceptionRate) {
    this.minPacketReceptionRate = minPacketReceptionRate;
  }

  /**
   * @return the maximum phase of all flows
   */
  public Integer getMaxPhase() {
    var queue = new SchedulableObjectQueue<Flow>(new MaxPhaseComparator<Flow>(), flows.values());
    return queue.poll().getPhase();
  }

  /**
   * @return the minimum period of all flows
   */
  public Integer getMinPeriod() {
    var queue = new SchedulableObjectQueue<Flow>(new PeriodComparator<Flow>(), flows.values());
    return queue.poll().getPeriod();
  }

  /**
   * @param e2e the e2e to set
   */
  public void setE2e(Double e2e) {
    this.e2e = e2e;
  }

  /**
   * @param intForNodeNames the intForNodeNames to set
   */
  public void setIntForNodeNames(Boolean intForNodeNames) {
    this.intForNodeNames = intForNodeNames;
  }

  /**
   * @param intForFlowNames the intForFlowNames to set
   */
  public void setIntForFlowNames(Boolean intForFlowNames) {
    this.intForFlowNames = intForFlowNames;
  }

  /**
   * @param flows the flows to set
   */
  public void setFlows(FlowMap flows) {
    this.flows = flows;
  }

  /**
   * @param nodes the nodes to set
   */
  public void setNodes(NodeMap nodes) {
    this.nodes = nodes;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param flowNamesInOriginalOrder the flowNamesInOriginalOrder to set
   */
  public void setFlowNamesInOriginalOrder(ArrayList<String> flowNamesInOriginalOrder) {
    this.flowNamesInOriginalOrder = flowNamesInOriginalOrder;
  }

  /**
   * @param flowNamesInPriorityOrder the flowNamesInPriorityOrder to set
   */
  public void setFlowNamesInPriorityOrder(ArrayList<String> flowNamesInPriorityOrder) {
    this.flowNamesInPriorityOrder = flowNamesInPriorityOrder;
  }

  /**
   * @param name the node whose channel is to be set
   * @param channel the channel to set
   */
  public void setNodeChannel(String name, Integer channel) {
    var node = nodes.get(name); // get the node object
    node.setChannel(channel);
    nodes.put(name, node); // update the nodes map with the updated object
  }

  /**
   * @return the node channel
   */
  public Integer getNodeChannel(String name) {
    var node = nodes.get(name); // get the node object
    return node.getChannel();
  }
  
  /**
   * This method will add a new flow to the Flows dictionary.
   * If the flow being added is already in the dictionary it will overwrite the current entry but
   * also produce a warning so the user knows if a flow has already been added
   * 
   * @param flowName
   */
  public void addFlow(String flowName) {
    /*
     * add a new flow node to the Flows dictionary. Only name, priority, and index are changed from
     * default values priority is set to the number of flows already added (index), 0 for first flow
     * This will create a default priority equal to the order listed in the input graph file. We
     * also set index to the same value so we can preserve that order as a secondary sort key. The
     * initalPriority field is probably not needed, but it might be useful in the future?? If the
     * optional flow parameters (priority, period, ...) is set, then this default priority will be
     * over written
     */
    if (flows.containsKey(flowName)) {
      System.out.printf("\n\tWarning! A flow with name %s already exists. "
          + "It has been replaced with a new flow\n.");
    }
    var index = flows.size();
    var flowNode = new Flow(flowName, index, index);
    flows.put(flowName, flowNode);
    if (!Utilities.isInteger(flowName) && intForFlowNames) {
      intForFlowNames = false; // set false because name not is a number; && above makes sure we
                               // only set it once
    }
    flowNamesInOriginalOrder.add(flowName);
  }
  
 /**
  * @return intForNodeNames
  */
  public Boolean isIntForNodeNames() { // returns true if all node names are ints
    return intForNodeNames;
  }

  /**
   * @return intForFlowNames
   */
  public Boolean isIntForFlowNames() { // returns true if all flow names are an ints
    return intForFlowNames;
  }

  /**
   * This method takes a node name and adds that node to a flow corresponding to the flow name.
   * The node is appended to the node array for the given flow name.
   * 
   * @param flowName name of which flow nodeName will be added to
   * @param nodeName name of the node to be added to flowName
   */
  public void addNodeToFlow(String flowName, String nodeName) {
    if (!Utilities.isInteger(nodeName) && intForNodeNames) {
      /* set false because name not is a number; && above makes sure we only set it once */
      intForNodeNames = false;
    }
    if (!nodes.containsKey(nodeName)) { // create the node and add it to nodes if map doesn't have
                                        // this node already.
      /* If the node already exists, just need to add to the flow */
      var index = nodes.size(); // nodeIndex will be the order added
      var graphNode = new Node(nodeName, DEFAULT_PRIORITY, index); // create a new graph node
      nodes.put(nodeName, graphNode); // add it to the map of nodes
    }
    /*
     * Node is now created and in the nodes map Next we need to get the current flow and add this
     * node to that flow by appending it to the node array for that flow
     */
    var flowNode = getFlow(flowName);
    var graphNode = new Node(nodeName, flowNode.nodes.size(), DEFAULT_INDEX);
    /* the priority is the node's index in the flow, which is the current array size */
    flowNode.addNode(graphNode);
    flowNode.linkTxAndTotalCost.add(DEFAULT_TX_NUM);
  }

/**
 * This method returns the index priority of a node in a flow.
 * It gets the nodes from a given flow name and then iterates through that set flows nodes in order
 * to find the priority of a of the nodeName.
 * 
 * @param flowName name of the flow that will contain the desired nodes
 * @param nodeName name of the node in which the priority is desired
 * @return the priority of a node in a specific flow
 */
  public Integer getFlowPriority(String flowName, String nodeName) {
    var priority = 0;
    var flow = getFlow(flowName);
    Iterator<Node> nodes = flow.nodes.iterator();
    while (nodes.hasNext()) {
      var node = nodes.next();
      if (node.getName() == nodeName) {
        priority = node.getPriority(); // found the src node, set its index
        break;
      }
    }
    return priority;
  }

  public void setFlowPriority(String flowName, Integer priority) {
    var flowNode = getFlow(flowName);
    flowNode.setPriority(priority);
  }

  public void setFlowPeriod(String flowName, Integer period) {
    var flowNode = getFlow(flowName);
    flowNode.setPeriod(period);
  }

  public void setFlowDeadline(String flowName, Integer deadline) {
    var flowNode = getFlow(flowName);
    flowNode.setDeadline(deadline);
  }

  public void setFlowPhase(String flowName, Integer phase) {
    var flowNode = getFlow(flowName);
    flowNode.setPhase(phase);
  }

  public Integer getFlowIndex(String flowName) {
    var flowNode = getFlow(flowName);
    return flowNode.index;
  }
/**
 * This method takes in the name of a flow and retrieves the priority of the flow node
 * @param flowName the name of the flow
 * @return an Integer that represents the priority of the flow node
 */
  public Integer getFlowPriority(String flowName) {
    var flowNode = getFlow(flowName);
    return flowNode.getPriority();
  }
/**
 * This method takes in the name of a flow and retrieves the period from the flow node.
 * @param flowName the name of the flow.
 * @return an Integer that is the period of a flow node.
 */
  public Integer getFlowPeriod(String flowName) {
    var flowNode = getFlow(flowName);
    return flowNode.getPeriod();
  }
/**
 * This method takes in the name of a flow and retrieves the deadline from the flow node.
 * @param flowName the name of the flow.
 * @return an Integer that is the deadline of a flowNode.
 */
  public Integer getFlowDeadline(String flowName) {
    var flowNode = getFlow(flowName);
    return flowNode.getDeadline();
  }
/**
 * This method takes in the name of a flow and retrieves the phase number of the flow node.
 * @param flowName the name of the flow.
 * @return an Integer that represents the phase of the flow node.
 */
  public Integer getFlowPhase(String flowName) {
    var flowNode = getFlow(flowName);
    return flowNode.getPhase();
  }
/**
 * This method takes in the name of a flow and retrieves the number of tx per link in a flow node.
 * @param flowName the name of the flow
 * @return and Integer to represent the number of tx per link.
 */
  public Integer getFlowTxAttemptsPerLink(String flowName) {
    var flowNode = getFlow(flowName);
    return flowNode.numTxPerLink;
  }
/**
 * When called this method sorts flows into priority order.
 * First sort the flows by index and then sort them by priority.
 * Creates a new ArrayList and adds the flows in the new priority order.
 * no parameters or return value.
 */
  public void setFlowsInPriorityOrder() {
    // create a list of Flow objects from the FlowMap using the stream interface.
    List<Flow> unsortedFlows = flows.values().stream().collect(Collectors.toList());
    // Now sort by a secondary key, which is index in this case
    List<Flow> sortedByIndex = unsortedFlows.stream().sorted(Comparator.comparing(Flow::getIndex))
        .collect(Collectors.toList());
    // Now sort by primary key, which is priority in this case
    List<Flow> sortedFlows = sortedByIndex.stream().sorted(Comparator.comparing(Flow::getPriority))
        .collect(Collectors.toList());
    // Finally, create a new flowNamesInPriorityOrder that contains the flow names in the requested
    // order
    flowNamesInPriorityOrder = new ArrayList<>();
    sortedFlows.forEach((node) -> flowNamesInPriorityOrder.add(node.getName()));
  }
/**
 * When called this method sorts flows into deadline order.
 * First sort the flows by priority and then sort them by deadline.
 * Creates a new ArrayList and adds the flows in the new deadline order.
 * no parameters or return value.
 */
  public void setFlowsInDMorder() {
    /* create a list of Flow objects from the FlowMap using the stream interface. */
    List<Flow> unsortedFlows = flows.values().stream().collect(Collectors.toList());
    /* Now sort by a secondary key, which is priority in this case */
    List<Flow> sortedByPriority = unsortedFlows.stream()
        .sorted(Comparator.comparing(Flow::getPriority)).collect(Collectors.toList());
    /* Now sort by primary key, which is deadline in this case */
    List<Flow> sortedFlows = sortedByPriority.stream()
        .sorted(Comparator.comparing(Flow::getDeadline)).collect(Collectors.toList());
    /*
     * Finally, create a new flowNamesInPriorityOrder that contains the flow names in the requested
     * order
     */
    flowNamesInPriorityOrder = new ArrayList<>();
    sortedFlows.forEach((node) -> flowNamesInPriorityOrder.add(node.getName()));
  }
  /**
   * When called this method sorts flows into priority order.
   * First sort the flows by priority and then sort them by period.
   * Creates a new ArrayList and adds the flows in the new period order.
   * no parameters or return value.
   */
  public void setFlowsInRMorder() {
    // create a list of Flow objects from the FlowMap using the stream interface.
    List<Flow> unsortedFlows = flows.values().stream().collect(Collectors.toList());
    // Now sort by a secondary key, which is priority in this case
    List<Flow> sortedByPriority = unsortedFlows.stream()
        .sorted(Comparator.comparing(Flow::getPriority)).collect(Collectors.toList());
    // Now sort by primary key, which is period in this case
    List<Flow> sortedFlows = sortedByPriority.stream().sorted(Comparator.comparing(Flow::getPeriod))
        .collect(Collectors.toList());
    // Finally, create a new flowNamesInPriorityOrder that contains the flow names in the requested
    // order
    flowNamesInPriorityOrder = new ArrayList<>();
    sortedFlows.forEach((node) -> flowNamesInPriorityOrder.add(node.getName()));
  }

  public void setFlowsInRealTimeHARTorder() {
    setFlowsInPriorityOrder(); // use Priority order for RealTimeHART
  }

  public void finalizeCurrentFlow(String flowName) {
    if (numFaults > 0) {
      finalizeFlowWithFixedFaultTolerance(flowName);
    } else {
      finalizeFlowWithE2eParameters(flowName);
    }
  }

  public Integer nextReleaseTime(String flowName, Integer currentTime) {
    var flow = getFlow(flowName);
    flow.setLastUpdateTime(currentTime);
    flow.setNextReleaseTime(currentTime);
    return flow.getReleaseTime(); // next release Time at or after currentTime
  }

  public Integer nextAbsoluteDeadline(String flowName, Integer currentTime) {
    var flow = getFlow(flowName);
    flow.setLastUpdateTime(currentTime);
    flow.setNextReleaseTime(currentTime);
    return flow.getReleaseTime() + flow.getDeadline(); // next deadline after currentTime
  }

 /**
  * Finalizes the flow by initializing a ReliabilityAnalysis with end to end requirements and
  * minimum packet reception rate, updating the linkTxAndTotalCost of the flow.
  * @param flowName
  */
  private void finalizeFlowWithE2eParameters(String flowName) {
    var flowNode = flows.get(flowName);
    var m = minPacketReceptionRate; // shorten the name :-)
    if (flowNode != null) {
      var nodes = flowNode.nodes;
      int nHops = nodes.size();
      if (nHops < 1) {
        /*
         * number of hops in flow, but make sure it will be at least 1, else it isn't a flow! || was
         * -1 at end
         */
        nHops = 2;
      }
      Double nTx = 1.0; // set nTx to 1 by default (1 transmission per link required at a minimum
                        // and when m == 1.0
      if (m < 1.0) {
        /*
         * now compute nTXper link based on Ryan's formula: log(1 - e2e^(1/hops)) / log(1 - M) = #
         * txs per hop
         */
        nTx = Math.log((1.0 - Math.pow(e2e, (1.0 / (double) nHops)))) / Math.log(1.0 - m);
      }
      /* set numTxPerLink based on M, E2E, and flow length */
      flowNode.numTxPerLink = (int) Math.ceil(nTx);
      /* Now compute nTx per link to reach E2E requirement. */
      ra = new ReliabilityAnalysis(e2e, minPacketReceptionRate);
      ArrayList<Integer> linkTxAndTotalCost =
          ra.numTxPerLinkAndTotalTxCost(getFlow(flowName));
      flowNode.linkTxAndTotalCost = linkTxAndTotalCost;
      flows.put(flowName, flowNode); // update flow node in Flows array
    } else { // should never happen...
      System.out.printf("\n\tWarning! Bad situation: Flow %s doesn't exist but "
          + "trying to get its numTxPerLink property\n.", flowName);
    }
  }

  /**
   * Using numFaults, initializes a ReliabilityAnalysis using the fault tolerance and finalizes the
   * flow, updating the linkTxAndTotalCost of the flowNode.
   * @param flowName
   */
  private void finalizeFlowWithFixedFaultTolerance(String flowName) {
	ra = new ReliabilityAnalysis(numFaults);
    var flowNode = flows.get(flowName);
    if (flowNode != null) {
      /* set numTxPerLink based on numFaults */
      flowNode.numTxPerLink = numFaults + 1;
      /* Now compute nTx per link to reach E2E requirement. */
      ArrayList<Integer> linkTxAndTotalCost = ra.numTxPerLinkAndTotalTxCost(this.getFlow(flowName));
      flowNode.linkTxAndTotalCost = linkTxAndTotalCost;
      flows.put(flowName, flowNode); // update flow node in Flows array
    } else { // should never happen...
      System.out.printf("\n\tWarning! Bad situation: Flow %s doesn't exist but "
          + "trying to get its numTxPerLink property\n.", flowName);
    }
  }

/**
 * This method shall sort the nodes alphabetically. Three different sorting processes are needed based
 * on whether the nodes are integers or letters.
 * @return a String Array of the node names in an alphabetically sorted order
 */
  public String[] getNodeNamesOrderedAlphabetically() {
    var nodes = getNodes();
    Set<String> keys = nodes.keySet(); // get the names from the node map
    String[] nodeNames = keys.toArray(new String[keys.size()]);
    Arrays.sort(nodeNames); // NodeNames are now sorted
    // However, if names are actually strings of integers, then the sort doesn't come out
    // the way we would like. So, handle that case
    var nodeNamesAsInts = new Integer[nodeNames.length];
    var allIntNames = true; // flag to see if all names are Ints or not
    for (int i = 0; i < nodeNames.length; i++) {
      var nodeName = nodeNames[i];
      if (Utilities.isInteger(nodeName) && allIntNames) {
        // nodeName is an alpha representation of an integer
        nodeNamesAsInts[i] = Integer.parseInt(nodeName);
      } else {
        // nodeName is an alpha name and not an integer, so set flag and terminate loop
        allIntNames = false; // set false because name not is a number; && above makes sure we only
                             // set it once
        break; // can stop the loop once we know not all of the names are ints
      }
    }
    if (allIntNames) {
      // If all names are ints, then we need to sort them accordingly
      // Otherwise, we get names in what appears to not be in order because
      Arrays.sort(nodeNamesAsInts); // sort the ints in assending order
      for (int i = 0; i < nodeNamesAsInts.length; i++) {
        nodeNames[i] = Integer.toString(nodeNamesAsInts[i]); // convert int to string
      }
    }
    return nodeNames;
  }

  // private function to the flow node with specified name
  private Flow getFlow(String flowName) {
    var flow = flows.get(flowName); // get the requested flow node
    if (flow == null) {// return empty node if not found
      flow = new Flow();
      System.out.printf(FLOW_WARNING + "retrieve it\n.", flowName);
    }
    return flow;
  }
/**
 * This method returns the flow names in their original order.
 * @return a String Array containing the flow names in their original order.
 */
  // public function to return an array of flow names in the order they were read from
  // the graph file
  public String[] getFlowNames() {
    return flowNamesInOriginalOrder.toArray(new String[0]);
    // could use new String[list.size()], but due to JVM optimizations new (new String[0] is better
  }
  /**
   * This method takes in a node name and returns the index of that node
   * @param nodeName name of the node that the user wants the index of.
   * @return index   this is the location of the nodeName variable in nodes.
   */
  // public function to return the dictionary of nodes
  public Integer getNodeIndex(String nodeName) {
    var index = 0;
    var node = nodes.get(nodeName); // could throw an exception if null, but just return 0 for now
    if (node != null) {
      index = node.getIndex();
    }
    return index;
  }

  /**
   * This method takes in a flow name and retrieves the nodes in the given flow.
   * If no nodes are present the method will return an empty array.
   * @param flowName the name of the flow.
   * @return a String Array of the nodes in the flow
   */
  public String[] getNodesInFlow(String flowName) {
    // get the flow node for requested Flow and then loop through the
    // nodes in the flow to create an array of the node names in
    // the same order as they exist in the flow specification (i.e., Graph file)
    var flow = flows.get(flowName);
    String[] nodes;
    if (flow != null) {
      nodes = new String[flow.nodes.size()];
      for (int i = 0; i < nodes.length; i++) {
        var node = flow.nodes.get(i); // get the node from the arrayList
        nodes[i] = node.getName(); // get the name and store it in the array
      }

    } else {
      nodes = new String[0]; // return empty array
      System.out.printf("\n\t Warning! No Flow with name %s.\n", flowName);
    }
    return nodes;
  }
  
/**
 * This method computes the LCM of all periods and then updates the current hyperPeriod to new number.
 * @return an Integer that represents the hyperPeriod
 */
  public Integer getHyperPeriod() {
    var hyperPeriod = 1; // hyperPeriod is LCM of all periods. Initialize to 1
    for (String flowName : getFlowNames()) {
      // var dm = new Utlitities();
      hyperPeriod = Utilities.lcm(hyperPeriod, getFlowPeriod(flowName)); // Find LCM of hyperPeriod
                                                                         // so far and the current
                                                                         // period, then update the
                                                                         // hyperPeriod with that
                                                                         // value
    }
    return hyperPeriod;
  }
  
/**
 * This method takes in a flow name and calculates the total cost of the specified flow.
 * @param flowName the name of the flow
 * @return an Integer representing the total cost of the flow
 */
  public Integer getTotalTxAttemptsInFlow(String flowName) {
    var flow = getFlow(flowName);
    var linkTxAndTotalCost = flow.getLinkTxAndTotalCost();
    var totalCostIndex = linkTxAndTotalCost.size() - 1;
    var totalCost = linkTxAndTotalCost.get(totalCostIndex);
    return totalCost;
  }
  
/**
 * This method takes in a flow name, gets the flow object and sets a new Array List to 
 * the link tx and total cost of the flow. Removes last element because it is just a sum
 * of the others.
 * 
 * @param flowName the name of the flow
 * @return an Integer Array containing the link tx and total cost of a flow.
 */
  // return an array of the number of transmission needed for each
  // link (i.e. edge in the flow graph) to meet E2E target
  public Integer[] getNumTxAttemptsPerLink(String flowName) {
    var flow = getFlow(flowName);
    var linkTxAndTotalCost = new ArrayList<Integer>(flow.getLinkTxAndTotalCost());
    var lastElement = linkTxAndTotalCost.size() - 1;
    linkTxAndTotalCost.remove(lastElement); // remove the last element, because that is the sum of
                                            // others
    return linkTxAndTotalCost.toArray(new Integer[0]);
  }

/**
 * This method adds an edge to a node by getting the node object a and adding an edge.
 * @param nodeName name of the node.
 * @param edge name of the edge that will be added.
 */
  public void addEdge(String nodeName, Edge edge) {
    var node = nodes.get(nodeName); // get the node object
    node.addEdge(edge);
  }
/**
 * This method records the max flow length by looping through all flows and keeping track of the max value.
 * @return an Integer that represents the max length of the flows
 */
  public Integer maxFlowLength() {
    Integer maxLength = 0;
    for (Flow flow : flows.values()) {
      maxLength = Math.max(maxLength, flow.nodes.size());
    }
    return maxLength;
  }
}
