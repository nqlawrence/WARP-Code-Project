package edu.uiowa.cs.warp;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import java.util.Set;
import java.util.Map.Entry;
import edu.uiowa.cs.warp.SystemAttributes.ScheduleChoices;
import edu.uiowa.cs.warp.WarpDSL.InstructionParameters;

/**
 * ReliabilityAnalysis analyzes the end-to-end reliability of messages transmitted in flows for the
 * WARP system.
 * <p>
 * 
 * Let M represent the Minimum Packet Reception Rate on an edge in a flow. The end-to-end
 * reliability for each flow, flow:src->sink, is computed iteratively as follows:<br>
 * (1)The flow:src node has an initial probability of 1.0 when it is released. All other initial
 * probabilities are 0.0. (That is, the reset of the nodes in the flow have an initial probability
 * value of 0.0.) <br>
 * (2) each src->sink pair probability is computed as NewSinkNodeState = (1-M)*PrevSnkNodeState +
 * M*PrevSrcNodeState <br>
 * This value represents the probability that the message as been received by the node SinkNode.
 * Thus, the NewSinkNodeState probability will increase each time a push or pull is executed with
 * SinkNode as a listener.
 * <p>
 * 
 * The last probability state value for any node is the reliability of the message reaching that
 * node, and the end-to-end reliability of a flow is the value of the last Flow:SinkNode
 * probability.
 * <p>
 * 
 * CS2820 Spring 2023 Project: Implement this class to compute the probabilities the comprise the
 * ReliablityMatrix, which is the core of the file visualization that is requested in Warp.
 * <p>
 * 
 * To do this, you will need to retrieve the program source, parse the instructions for each node,
 * in each time slot, to extract the src and snk nodes in the instruction and then apply the message
 * success probability equation defined above.
 * <p>
 * 
 * I recommend using the getInstructionParameters method of the WarpDSL class to extract the src and
 * snk nodes from the instruction string in a program schedule time slot.
 * 
 * @author sgoddard
 * @version 1.5
 *
 */
public class ReliabilityAnalysis {
	
	/**
	 * The minimum link quality in the system.
	 * This is a double from 0 to 1 that represents the minimum percentage that a 
	 * link can fail to send.
	 * This is a command line option.
	 */
	private static Double minLQ;
	
	/**
	 * The file where the graph workload is read.
	 */
	private static String inputFile;
	
	/**
	 * The schedule that is requested.
	 */
	private static ScheduleChoices schedulerSelected;
	
	/**
	 * The number of wireless channels that can be used to schedule messages.
	 * Each channel can process a set of instructions per time step.
	 */
	private static Integer nChannels;
	
	
	private Integer numFaults = 0;
	/**
	 * The minimum packet reception rate. 0.9 by default
	 */
	private Double minPacketReceptionRate = 0.9;
	/**
	 * The end to end transmission rate. 0.99 by default
	 */
	private Double e2e = 0.99;
	
	private boolean e2eExists;
	
	/**
	 * This boolean keeps track of whether the object was instantiated with numFaults or 
	 * minPacketReceptionRate and e2e.
	 */
	private Boolean indicator = true;
	
	private Boolean conflictExists;
	
	private ArrayList<InstructionParameters> InstructionParameters;
	
	private ReliabilityTable table = new ReliabilityTable();
	
	private Program program;
	
	private WorkLoad workload;
	
	
	private ProgramSchedule programSched;
	
	private ProgramSchedule reliabilityAnalysisTable;
	
	private String[] headerRow;
	
	public NodeMap mapofNodes;
	
	private String[] headerNodes;
	
	/**
	 * Getters:
	 * All of the following methods are getters for important data that is required for building the 
	 * reliabilityAnalysisTable, the min packet reception rate, e2e, numFaults, as well as the flow nodes
	 * for the header and the reliability analysis table itself.
	 */
	public Double getM() {
		return this.minPacketReceptionRate;
		
	}
	public Double getE2e() {
		return this.e2e;
		
	}
	public Integer getNumFaults() {
		return this.numFaults;
	}
	
	public String[] getNodeInFlow() {
		return headerNodes;
		
	}
	public String[] getHeaderRow() {
		return headerRow;
	}
	public ReliabilityTable getRA() {
		return this.table;
	}

	
	
	
	/**
	 * Setters for reliability table, Important for formatting the table and to make sure the 
	 * results are in the correct positions
	 * 
	 * Is public for testing, but should normally be private
	 */
	public void setRA(ReliabilityTable rTable) {
		this.table = rTable;
	}
	public void setHeaderRow(String[] row) {
		this.headerRow = row;
	}
	public void setRATable(ReliabilityTable RATable) {
		this.table = RATable;
	}
	/**
	 * 
	 * Gets the total number of transmission attempts per link and the total in the flow.
	 * By defining a reliability window and calculating the number of rows it contains after
	 * testing each link in the flow according to the specified e2e value.
	 * 
	 * @param flow The flow to be tested
	 * @return
	 */
	  public ArrayList<Integer> getNumTxAttemptsPerLinkAndTotalTxAttempts(Flow flow) {
		  
		  Double M = 0.9;
		  var nodesInFlow = flow.nodes;
		  var nNodesInFlow = nodesInFlow.size(); // The last entry will contain the worst-case cost of
		  // transmitting E2E in isolation
		  // var nPushes = Array(repeating: 0, count: nNodesInFlow+1);
		  var nPushes = new Integer[nNodesInFlow + 1]; // Array to track nPushes for each node in this
		  // flow (same as nTx per link)
		  Arrays.fill(nPushes, 0); // initialize to all 0 values
		  var nHops = nNodesInFlow - 1;
		  // minLinkReliablityNeded is the minimum reliability needed per link in a flow to hit E2E
		  // reliability for the flow
		  Double minLinkReliablityNeded = Math.max(e2e, Math.pow(e2e, (1.0 / (double) nHops))); // use max
		  // Use max to handle rounding error when e2e == 1.0
		  // Now compute reliability of packet reaching each node in the given time slot
		  // Start with a 2-D reliability window that is a 2-D matrix of no size
		  // each row is a time slot, stating at time 0
		  // each column represents the reliability of the packet reaching that node at the
		  // current time slot (i.e., the row it is in)
		  // will add rows as we compute reliabilities until the final reliability is reached
		  // for all nodes.
		  var reliabilityWindow = new Vector<Vector<Double>>();
		  var newReliabilityRow = new Vector<Double>();
		  for (int i = 0; i < nNodesInFlow; i++) {
			  newReliabilityRow.add(0.0); // create the the row initialized with 0.0 values
		  }
		  reliabilityWindow.add(newReliabilityRow); // now add row to the reliability window, Time 0
		  Vector<Double> tmpVector = reliabilityWindow.get(0);
		  var currentReliabilityRow = tmpVector.toArray(new Double[tmpVector.size()]);
		  // var currentReliabilityRow = (Double[]) reliabilityWindow.get(0).toArray();
		  // Want reliabilityWindow[0][0] = 1.0 (i.e., P(packet@FlowSrc) = 1
		  // but I din't want to mess with the newReliablityRow vector I use below
		  // So, we initialize this first entry to 1.0, wich is reliabilityWindow[0][0]
		  // We will then update this row with computed values for each node and put it
		  // back in the matrix
		  currentReliabilityRow[0] = 1.0; // initialize (i.e., P(packet@FlowSrc) = 1
		  Double e2eReliabilityState = currentReliabilityRow[nNodesInFlow - 1];
		  // the analysis will end when the e2e reliability matrix is met, initially the state is not met and will be 0 with this statement
		  var timeSlot = 0; // start time at 0
		  while (e2eReliabilityState < e2e) { // change to while and increment increment timeSlot because
			  // we don't know how long this schedule window will last
			  var prevReliabilityRow = currentReliabilityRow;
			  currentReliabilityRow = newReliabilityRow.toArray(new Double[newReliabilityRow.size()]);
			  // would be reliabilityWindow[timeSlot] if working through a schedule
			  // Now use each flow:src->sink to update reliability computations
			  // this is the update formula for the state probabilities
			  // nextState = (1 - M) * prevState + M*NextHighestFlowState
			  // use MinLQ for M in above equation
			  // NewSinkNodeState = (1-M)*PrevSnkNodeState + M*PrevSrcNodeState
			  
			  for (int nodeIndex = 0; nodeIndex < (nNodesInFlow - 1); nodeIndex++) { // loop through each
				  // node in the flow and
				  // update the sates for
				  // each link (i.e.,
				  // sink->src pair)
				  var	flowSrcNodeindex = nodeIndex;
				  var flowSnkNodeindex = nodeIndex + 1;
				  var prevSrcNodeState = prevReliabilityRow[flowSrcNodeindex];
				  var prevSnkNodeState = prevReliabilityRow[flowSnkNodeindex];
				  Double nextSnkState;
				  if ((prevSnkNodeState < minLinkReliablityNeded) && prevSrcNodeState > 0) { 
					  // do a push until PrevSnk state > e2e to ensure next node reaches target e2e but skip
					  // if no chance of success (i.e. source doesn't have packets)
					  nextSnkState = ((1.0 - M) * prevSnkNodeState) + (M * prevSrcNodeState); // need to
					  // continue
					  // attempting to
					  // Tx, so update
					  // current state
					  nPushes[nodeIndex] += 1; // increment the number of pushes for for this node to snk node
					  } 
				  else {
					  nextSnkState = prevSnkNodeState; // snkNode has met its reliability. Thus move on to the
					  // next node and record the reliability met
				  }
				  
				  if (currentReliabilityRow[flowSrcNodeindex] < prevReliabilityRow[flowSrcNodeindex]) { 
					  // probabilities are non-decreasing so update if we were higher by carrying old value forward
					  
					  currentReliabilityRow[flowSrcNodeindex] = prevReliabilityRow[flowSrcNodeindex]; 
					  // carry forward previous state for the src node, which may get over written later by another instruction in this slot
				  }
				  currentReliabilityRow[flowSnkNodeindex] = nextSnkState;
			  }
			  
			  e2eReliabilityState = currentReliabilityRow[nNodesInFlow - 1];
			  Vector<Double> currentReliabilityVector = new Vector<Double>();
			  // convert the row to a vector so we can add it to the reliability window
		      Collections.addAll(currentReliabilityVector, currentReliabilityRow);
		      if (timeSlot < reliabilityWindow.size()) {
		    	  reliabilityWindow.set(timeSlot, (currentReliabilityVector));
		      } 
		      else {
		    	  reliabilityWindow.add(currentReliabilityVector);
		      }
		      timeSlot += 1; // increase to next time slot
		  }
		  var size = reliabilityWindow.size();
		  nPushes[nNodesInFlow] = size; // The total (worst-case) cost to transmit E2E in isolation with
		  // specified reliability target is the number of rows in the
		  // reliabilityWindow
		  // Now convert the array to the ArrayList needed to return
		  ArrayList<Integer> nPushesArrayList = new ArrayList<Integer>();
		  Collections.addAll(nPushesArrayList, nPushes);
		  return nPushesArrayList;
	  }
	  
	  private ArrayList<Integer> getFixedTxPerLinkAndTotalTxCost(Flow flow) {
		    var nodesInFlow = flow.nodes;
		    var nNodesInFlow = nodesInFlow.size();
		    ArrayList<Integer> txArrayList = new ArrayList<Integer>();
		    /*
		     * Each node will have at most numFaults+1 transmissions. Because we don't know which nodes will
		     * send the message over an edge, we give the cost to each node.
		     */
		    for (int i = 0; i < nNodesInFlow; i++) {
		      txArrayList.add(numFaults + 1);
		    }
		    /*
		     * now compute the maximum # of TX, assuming at most numFaults occur on an edge per period, and
		     * each edge requires at least one successful TX.
		     */
		    var numEdgesInFlow = nNodesInFlow - 1;
		    var maxFaultsInFlow = numEdgesInFlow * numFaults;
		    txArrayList.add(numEdgesInFlow + maxFaultsInFlow);
		    return txArrayList;
		  }

	  /**
	   * If the indicator is true and the object was created with numFaults, returns
	   * the fixed transmissions per link and transmission cost. If it was not, returns 
	   * the number of attempts per link and total attempts.
	   * @param flow
	   * @return
	   */
public ArrayList<Integer> numTxPerLinkAndTotalTxCost(Flow flow) {
   if (indicator == true) {
	   return getFixedTxPerLinkAndTotalTxCost(flow);
   }
   else {
	   return getNumTxAttemptsPerLinkAndTotalTxAttempts(flow);
   }
}
/**
 * Constructor that passes e2e and minimum reception rate values and sets values.
 * @param e2e
 * @param minPacketReceptionRate
 */
public ReliabilityAnalysis(Double e2e, Double minPacketReceptionRate) {
	this.e2e = e2e;
	this.minPacketReceptionRate = minPacketReceptionRate;
	this.e2eExists = true;

}

/**
 * Constructor that passes the desired number of faults and sets the indicator to false in order to
 * return the correct output.
 * @param numFaults
 */
public ReliabilityAnalysis (Integer numFaults) {
	this.numFaults = numFaults;
	this.e2eExists = false;
}


// Makes a call to new method to build the reliabilityTable. 
  public ReliabilityAnalysis(Program program) {
	  this.program = program;
	  this.workload = program.toWorkLoad();
	  conflictExists = false;
	  this.minPacketReceptionRate = program.getMinPacketReceptionRate();
	  this.e2e = program.getE2e();
	  this.numFaults = program.getNumFaults();
	  this.e2eExists = true;
	  
	  buildReliabilityAnalysisTable();
	  
  }
/**
 * This method builds the main ReliabilityAnalysisTable that will be used as the base 
 * for retrieving all of the data that we need. It sorts workload flows in order of priority,
 * iterates through them to get all of the nodes in a flow. The nodes for reliability are required
 * to be initiallized first. We use a NodeMap to hold objects for ReliabilityNodes.
 * 
 * Uses several helper methods for different purposes that are explained in further detail 
 * in the javadocs for the individual methods.
 * 
 * This method is public for testing but should normally be private.
 */
  
  public void buildReliabilityAnalysisTable() {	
			
			Double M = workload.getMinPacketReceptionRate();
			ArrayList<String> header = new ArrayList<String>();
			int columnIndex = 0; 
			boolean source = false;
			RAMap nodeMap = new RAMap();
			//Beginning of iteration process to introduce flow and nodes do nodeMap
			ArrayList<String> flows = workload.getFlowNamesInPriorityOrder();
			for (int i=0; i < flows.size(); i++) {
				String[] nodes = workload.getNodesInFlow(flows.get(i));
				for (String node : nodes) {
				}
				
				for(int j=0; j < nodes.length; j++) {
					if (j==0) {
						source = true;
					}
					else {
						source = false;
					}
					
					RANode RN = new RANode(columnIndex,0,source);
					String ReliabilityNodeName = (""+flows.get(i)+":"+nodes[j]);
					nodeMap.put(ReliabilityNodeName, RN);
					header.add(ReliabilityNodeName);
					columnIndex++;
				}
			}
			String[] headerRow = new String[header.size()]; //Use an array of strings to easily read
			for (int i =0;i<header.size();i++) {            //the header
				headerRow[i] = header.get(i);
			}
			setHeaderRow(headerRow); 
			
			int hyperPeriod = workload.getHyperPeriod(); //Use the num of header rows to get hyper
														// period.
			
			int columns = header.size();
			ReliabilityTable raTable = new ReliabilityTable(hyperPeriod, columns);
			this.setInitialSrcFlows(nodeMap, raTable);
			
			ProgramSchedule instructions = program.getSchedule();
			WarpDSL warp = new WarpDSL();
		
			int i = 0;	 //Initiallize an index variable, as well as the Src and Snk for the
			Double prevSrcState = 0.0; //previous nodes.
			Double prevSnkState = 0.0;
			
			//Loop through the instructions for each time slot.
			for (InstructionTimeSlot instructionRow : instructions) { 
				for (String instruction : instructionRow) {
					ArrayList<InstructionParameters> inst = warp.getInstructionParameters(instruction);
					for (InstructionParameters param : inst) {
						
						String command = param.getName();	
						// Save time here by looking for periods of stalling.
						if (command.equals("push") || command.equals("pull")) {
							
							String flowName = param.getFlow();
							
							RANode flowSrc = nodeMap.get(flowName + ":" + param.getSrc());
							RANode flowSnk = nodeMap.get(flowName + ":" + param.getSnk());
							
							
							boolean isPeriod = false;
							
							if (i == 0) {
								prevSrcState = raTable.get(i, flowSrc.getColIndex());
								prevSnkState = 0.0;
							}
							//Make sure to reset a flow if the period is over
							else if (i % workload.getFlowPeriod(flowName) == 0) {
								System.out.println("Flow Period Over");
								prevSrcState = 1.0;
								prevSnkState = 0.0;
								
								System.out.println(((1 - M)*prevSnkState) + (M*prevSrcState));
								isPeriod = true;
								
							}
							
							else {
								prevSrcState = raTable.get(i - 1, flowSrc.getColIndex());
								prevSnkState = raTable.get(i - 1, flowSnk.getColIndex());
							}
							
							//Apply formula given in project PDF
							double newSnkState = ((1 - M)*prevSnkState) + (M*prevSrcState);
							raTable.set(i, flowSnk.getColIndex(), newSnkState);
							if (isPeriod) {
								System.out.println(raTable.get(i, flowSnk.getColIndex()));
							}
							
							
						}
					}
				}
				//This part overrides reliabilities if they are less than their previous value.
			if (i > 0) {
				this.overrideReliabilities(i, nodeMap, raTable);
			}
			i++; 
			}
			
			this.setRATable(raTable);
		}
/**
 * Getter for the program schedule
 * @return programSched: Self explanatory, a schedule for the program.
 */
public ProgramSchedule getProgramSchedule() {
	return this.programSched;
}
/*
 * Method that is a boolean that returns true if channel conflicts exist and false otherwise.
 */
public Boolean isReliabilityConflict() {
	return conflictExists;
}
/**
 * Getter for reliability analysis table.
 * @return reliabilityAnalysisTable: self explanatory, just returns the table.
 */
public ProgramSchedule getReliabilityAnalysisTable() {
	return reliabilityAnalysisTable;
}



/**
 * Below This point are helper methods and classes for building the reliability table.
 */


/**
 * Goes over the current given timeslot, checks if the values
 * are less than their previous value, and if they are they get overridden.
 * 
 * This method is public for testing, but should normally be private.
 * 
 * @param timeSlot the finished timeslot in the current position
 * @param nodeMap: a nodeMap in which the key will be retrieved and set
 * @param reliabilities: reliability table that will be set with the updated values.
 */
public void overrideReliabilities(Integer timeSlot, RAMap nodeMap, ReliabilityTable reliabilities) {
	
	
	int currentIndex;
	Double prevNode;
	Double currentNode;
	
	Set<String> keys = nodeMap.keySet();
	
	for(String key : keys) {
		RANode node = nodeMap.get(key);
		
		String flowName = key.split(":")[0];
		int flowPeriod = workload.getFlowPeriod(flowName);
		
		if (timeSlot % flowPeriod != 0) {
			currentIndex = node.getColIndex();
			
			prevNode = reliabilities.get(timeSlot-1, currentIndex);
			currentNode = reliabilities.get(timeSlot, currentIndex);
			
			if (currentNode < prevNode) {
				reliabilities.set(timeSlot, currentIndex, prevNode);
			}
		}
		else System.out.println(flowName + " " + flowPeriod );
	}
	}




public ReliabilityTable getReliabilities() {
	return this.table;
}

/**
 * Helper method for ReliabilityAnalysisTable that Verifies if the reliabilities for the table
 * are acceptable using a verified list.
 * @return boolean false if the verified index is less than e2e, true otherwise.
 */

public Boolean verifyReliabilities() {
	Double[] verified = this.getFinalRow();
	for (int i=0; i<verified.length; i++) {
		if (verified[i] < e2e) {
			return false;
		}
	}
	return true;
}
/**
 * Helper method for the reliabilityAnalysisTable to get the final row. After getting the final row,
 * the method iterates through it, gets the values at each index and puts them in the finalRow array.
 * Public for testing, but should normally be private.
 * @return
 */
public Double[] getFinalRow() {
	ReliabilityTable matrix = this.getRA();
	Double[] finalRow = new Double[matrix.getNumColumns()];
	for (int i=0; i<matrix.getNumColumns(); i++) {
		finalRow[i] = matrix.get(matrix.getNumRows()-1, i);
	}
	return finalRow;
	}
/**
 * Helper method for reliabilityAnalysisTable that has differing behaviors
 * depending if the values given are a source. If it is not a source, the value
 * should be 0.0, otherwise if it is, it should be 1.0.
 * This method is public for testing, but should normally be private.
 * @param rm a ReliabilityAnalysis map in which is used to retrieve data from RA Nodes.
 * @param reliabilities: a RA Table that will be used to set the flows in.
 */
public void setInitialSrcFlows(RAMap rm, ReliabilityTable reliabilities){
	
	Set<Entry<String, RANode>> nodeSet = rm.entrySet();
	int numColumns = reliabilities.getNumColumns();
	ReliabilityRow rRow = new ReliabilityRow(numColumns, 0.0);
	for (Entry<String, RANode> entry : nodeSet) {
		RANode rNode = entry.getValue();
		if (rNode.getSrcCondition()) { 
			rRow.set(rNode.getColIndex(), 1.0);
		}
	}
	for (int i = 0; i < reliabilities.getNumRows(); i++) {
		for (int j = 0; j < numColumns; j++) {
			reliabilities.set(i, j, rRow.get(j));
		}
	}
}
static class RANode {
	Integer colIndex;
	Integer phase;
	Boolean isASrc;
	
	RANode(Integer colIndex, Integer phase, Boolean isASrc){
		this.colIndex = colIndex;
		this.phase = phase;
		this.isASrc = isASrc;
	}
	
	/**
	 * Getters for the column Index, phase, and the condition of whether or not 
	 * a node is a source node.
	 * @return
	 */
	
	/**
	 * getter for the index of a node in a column.
	 * @return: returns the index of a node's column.
	 */
	public Integer getColIndex() {
		return colIndex;
	}
	/**
	 * Getter for the phase of a node
	 * @return: the phase of a node.
	 */
	public Integer getPhase() {
		return phase;
	}
	/**
	 * Checks if node is a source
	 * @return: True if a source, false if not.
	 */
	public Boolean getSrcCondition() {
		return isASrc;
	}
	/**
	 * Setter for the phase of a node
	 * @param sPhase: phase to be set.
	 */
	public void setPhase(Integer sPhase) {
		this.phase = sPhase;
	}
}
static class RAMap extends AbstractMap<String, RANode>{
	public static final long serialVersionUID = 1L;
}
	
}
