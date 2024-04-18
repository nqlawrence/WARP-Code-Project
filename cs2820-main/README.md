# CS2820 Spring 2023 WARP Project Code
This code base will be used for the University of Iowa CS 2820 Introduction to Software
Development course. The code was developed by Steve Goddard for the WARP sensor network 
research project. It was first written in Swift and rewritten in Java. It was then 
rewritten again in an object-oriented programming style. It was a quick
hack, and it needs a lot of cleanup and refactoring. A perfect code base to teach
the value of software developement fundamentals!
<br>

* -v (verbose)
* -sch priority (priority scheduler)
* --all (all options)
* -i <input file>
* -o <output folder>

# Homework 1

For homework 1 I handled a variety of edge cases

1. empty lines at the beginning and of the file
2. extra spaces around filename and final curly bracket
3. empty lines scattered throughout the flows

# Homework 2

What each team member documented.

## Zach

* Flow.java
* a through k in Workload.java
* Warp.java
* Program.java

## Noah

* ProgramVisualization.java
* VisualizationImplementation.java
* l through w in Workload.java

# Homework 3

## Zach

* Tests c,d,g,h,k,l,o,p

## Noah

* Tests a,b,e,f,i,j,m,n


# Homework 4

Created UML diagrams:

SchedulableObject.umlcd,
Workload.umlcd,
Reliability.umlcd
	
## Reliability.java

* Added getReliability() method, made by auto-generating code from UML Diagram








## Homework 5
		
# HW5 Task:
	The goal of this assignment was generate new code from a UML diagram and
	re factor code used in ReliablityAnalysis.java and from WorkLoad.java.
		Alain Christe Tello:
			Refactored numTxAttemptsPerLinkAndTotalTxAttempts and getFixedTxPerLinkAndTotalTxCost
			Generated JavaDoc comments for changes
			Removed unnecessary methods from WorkLoad.java
		Carter Schminke:
			Partial refactor of numTxAttemptsPerLinkAndTotalTxAttempts
			Partial refactor of getFixedTxPerLinkAndTotalTxCost
			Implemented numTxPerLinkAndTotalTxCost
		Noah Lawrence:
			Reliability.umlcd
			Generated code for ReliabilityAnalysis
			
			
			
## Sprint 1

#Sprint Tasks:
	The goal for the first sprint is to lay out the framework to be built upon 
	for the entirety of the project. A sequence diagram will be made for the warp 
	code to allow for easy visualization for the order and processes of the code. 
	Then, plan documents will be generated via either UML or pdf that will clearly
	lay out details and plans. Said documents will contain what tasks need to be done,
	the order in which they will be completed, plans to test, who will be doing them,
	etc. There should also be identification for methods that are needed for the 
	Visualization class.
	
	Before any meaningful changes to the code are made, we will begin with creating 
	the sequence diagram, which will roughly depict the ordering of the WARP code. 
	The sequence diagram will be followed by project plan documents. We need these first 
	so that it will make it very clear what to do and when. The plan documents will describe
	the main goals of the project, and will lay out how our group will complete various tasks.
	After completion of the plans, we should then start to work on the Visualization class,
	and complete the unfinished methods in the class, as well as creating documentation for them.
	
	
	Contributions:
	
	Most of the work done on project plans as well as  code/tests will be in groups,
	 which will be decided on when implementation of code begins
	
	Noah Lawrence:
	Work on the Plan Document, README
	
	Payton Lovan:
	Work on the Plan Document, Sequence Diagram
	
	Carter Schminke:
	Work on the Plan Document
	
	Alain Christe Tello:
	Work on the Plan Document
	
	Minhyuk Lee:
	Work on the Plan Document
	
	
## Sprint 2
#  Sprint Tasks:
	ReliabilityAnalysis(Program program):
	This makes a call to create the reliability table for the data in createReliabilityAnalysisDataTable.


	Team:
	Payton Lovan
	Noah Lawrence
	Minhyuk Lee
	Carter Schminke
	Alain Christetello

	Task Assignment & Method:
	For our Agile Process, we will be doing Pair Programming, where we will delegate tasks into (at most) even teams. More information on the tasks will be 	shown below this section.

	Team 1: Payton Lovan & Minhyuk Lee
	Team 2: Noah Lawrence, Carter Schminke, and Alain Christetello

	Implementation Tasks for ReliabilityAnalysis:

	Team 1 - Implement getReliabilityAnalysis

	Team 2 - Implement ReliabilityAnalysis(Program program)

	Teams 1 & 2: Implement createReliabilityAnalysisTable

	Implementation Tasks for ReliabilityVisualization:
	Team 1 - Implement createHeaders (Done!)

	Team 2 - Implement createColumnHeaders (Done!)

	Teams 1 & 2 - Implement createVisualizationData (In Progress..)
	Testing Tasks:
	Team 1 - Test for headers (graphName,schedulerName, e2e, M, nChannels) Additionally, the headers can be tested individually if needed.

	Team 1 - Test for nullHeaders

	Team 2 - Test for ColumnHeaders (FlowNames)

	Team 1 & 2 - Test for reliabilityData (Probability of sink nodes, and updated nodes)

	Team 1 or 2 - Test for PriorityOrder timeslots/flowNames

	Team 1 - Test for DefaultPriorityOrder timeslots/flowNames

	Team 2 - Test for NonPriorityOrder timeslots/flowNames

	Overview & Problem:
	WARP does not have an Analysis to check on the End-to-End (E2E) reliabilities, so we will need to implement this for WARP so that the communications/		messages of nodes are visible to see when it's updated! We will also need to check the probability of the message at that node when it's updated at that 		time.


	Helper Methods & Classes for Implementation:
	ProgramVisualization (Helps with ReliabilityVisualization)
	WarpInterface (Helps with a trace of code)
	VisualizationObject (helps in terms of creating the visualization data) [Look for not implemented methods]

	Terminology & Dictionary:
	Listeners:
	Also known as Observers, they ‘watch’ for any updates for Methods.

	Sink Node:
	Sink nodes are nodes where they have no edge to go to, this can be a node that is in the middle of two end nodes (start and end) that DOES NOT point 		back to those nodes or it can be an individual node without any edges.
	Tasks to be Implemented in ReliabilityAnalysis:
	getReliabilityAnalysis:
	To get the reliabilityAnalysis, we will need to obtain a reliabilityTable which gets the reliabilityAnalysisData table, more information will be 		provided in the createReliabilityAnalysisTable on how to get the data.

	createReliabilityAnalysisTable:
	ArrayList<InstructionParameters> (String instructions) which iterates over the ProgramSchedule table entries.

	We can also access the InstructionParameters() for the names of the data, such as the flowName, the src, the sinkNodes, and channels.

	To create the ReliabilityAnalysisTable, we will need to implement the data of these flows, which for each flow in src -> sinkNode, can be iteratively 	used by this formula:

	This value represents the probability that the message has been received by the node, SinkNode.
	Where the newSinkNodeState probability will increase each time a push or pull is executed with SinkNode as a listener/observer.

	The number of rows will be the number of timeSlots, and each Column will be the name of the FlowNodes.


	Tasks to be Implemented in ReliabilityVisualization:

	createHeader (override): [Done!]
	Headers like the graph name, Scheduler Name, M, E2E, nChannels will be implemented and visualized by using strings or descriptions here.

	createColumnHeader(override): [Done!]
	ColumnHeaders are where the FlowNames will show for the nodes, which is implemented by using an array of strings that is converted from the 				reliabilityAnalysisDataTable.

	createVisualizationData (override):  [In Progress..]
	Visualization data will make a call to ra.getReliabilities, where afterwards, we convert the reliabilityTable to a 2D String array.


	Things to Test (needs updating):

	testForHeaders:
	This JUnit Test will test out if the strings match the headers, by using comparisons or equalTo, we can check if the Graph has the headers, such as the 	E2E, M, SchedulerName, and the graph name to be identical to where they were grabbed from. Ideally, this may be split into multiple tests, which can be 	testForE2E, testForM, testForSchedulerName, testForGraphName.

	testForE2E:
	This JUnit Test will test the percentage value of the End-to-End Reliability.

	testForM:
	This JUnit Test will test the values of the minimum 

	testForColumnHeaders:
	This JUnit Test will be testing out the FlowNames and checking if those FlowNames are correct and identical to the expected string of FlowNames/			ColumnHeaders.

	testForNullHeaders:
	This JUnit Test will be testing out if the strings are null, by using comparisons, equalTo, or isNull, we can check if the Graph has no headers, such as 	E2E, M, SchedulerName, and if the graph name has nothing. Ideally, this may be split into multiple tests, such as testforNullE2E, testForNullM, 			testForNullSchedulerName, and testforNullGraphName.

	testForReliabilityData:
	This JUnit Test will be testing out the reliability analysis data that is received from the ReliabilityTable. If it is equal to the expected value of 	the probability, then it will pass, if not, it will fail.

	testForPriorityOrder:
	This JUnit Test will be testing out if the ColumnHeaders/FlowNames are in PriorityOrder. We will implement an expected Order from a StressList and if it 	matches, the test will pass, if not, the test will fail.


	Debugs to fix:
	There seems to be an AssertionError in our testForHeaders, as there is both an extra line.

	
##Sprint 3:
#Goals:
Our Main goal for Sprint3 is to finalize the project design and implement all of the code that was not previously implemented to
ReliabilityAnalysis & ReliabilityVisualization.

#Team
Payton Lovan
Noah Lawrence
Minhyuk Lee 
Carter Schminke

#Artifacts
The Sequence diagram should now correctly reflect the sequence of events in the WARP code involving 
ReliabilityAnalysis & ReliabilityVisualization

The UML Diagrams for reliability have been updated to match the implemented code.
This can be viewed in the file Reliability.umlcd

#Implementation Tasks for Reliability:

Team 1 - Implement getReliabilityAnalysis 

Team 2 - Implement ReliabilityAnalysis(Program program) 

Teams 1 & 2: Implement buildReliabilityAnalysisTable

Implementation Tasks for ReliabilityVisualization:
Team 1 - Implement createHeaders 

Team 2 - Implement createColumnHeaders 

Teams 1 & 2 - Implement createVisualizationData 
	Testing Tasks:
	Team 1 - Test for headers (graphName,schedulerName, e2e, M, nChannels) Additionally, the headers can be tested individually if needed.

Team 1 - Test for nullHeaders 

Team 2 - Test for ColumnHeaders (FlowNames)

Team 1 & 2 - Test for reliabilityData (Probability of sink nodes, and updated nodes)

Team 1 or 2 - Test for PriorityOrder timeslots/flowNames

Team 1 - Test for DefaultPriorityOrder timeslots/flowNames

Team 2 - Test for NonPriorityOrder timeslots/flowNames

Team 1 - Javadocs for ReliabilityVisualization & ReliabilityAnalysis

Team 2 - Javadocs for Test files (Visualization & Analysis)

Team 1 - Update UML Diagrams

Team 2 - Update Sequence Diagram



#Overview
WARP does not have an Analysis to check on the End-to-End (E2E) reliabilities, so we have implemented this for WARP so that 
the communications/messages of nodes are visible to see when it's updated! We will also need to check the probability 
of the message at that node when it's updated at that time.

#Helper Methods & Classes for Implementation:

ProgramVisualization (Helps with ReliabilityVisualization)

WarpInterface (Helps with a trace of code)

VisualizationObject (helps in terms of creating the visualization data)

ReliabilityAnalysis:
overrideReliabilities(Integer timeSlot, RAMap nodeMap, ReliabilityTable reliabilities)

verifyReliabilities()

getFinalRow()

setInitialSrcFlows(RAMap rm, ReliabilityTable reliabilities)

RAMap class to allow for converting the nodes of Reliability to hash map

RANode class to easily identify the phase, column index, and determines if node is a source.

ReliabilityAnalysisTest:
SetUpValues & SetUpReliabilities



#Tasks Implemented in ReliabilityAnalysis:

getReliabilityAnalysisTable:
To get the reliabilityAnalysis, we will need to obtain a reliabilityTable which gets the reliabilityAnalysisData table, more information will be provided in the buildReliabilityAnalysisTable on how to get the data.

buildReliabilityAnalysisTable:
Retrieving the nodes in each time slot is crucial to get the node probabilities when they are updated. This can be accessed by using the ArrayList<InstructionParameters> (String instructions) which iterates over the ProgramSchedule table entries.

We can also access the InstructionParameters() for the names of the data, such as the flowName, the src, the sinkNodes, and channels.

To create the ReliabilityAnalysisTable, we will need to implement the data of these flows, which for each flow in src -> sinkNode, can be iteratively used by this formula:

This value represents the probability that the message has been received by the node, SinkNode.
Where the newSinkNodeState probability will increase each time a push or pull is executed with SinkNode as a listener/observer.

The number of rows will be the number of timeSlots, and each Column will be the name of the FlowNodes.

#Tasks Implemented in ReliabilityVisualization:

createHeader (override): [Done!]
Headers like the graph name, Scheduler Name, M, E2E, nChannels will be implemented and visualized by using strings or descriptions here.

createColumnHeader(override): [Done!]
ColumnHeaders are where the FlowNames will show for the nodes, which is implemented by using an array of strings that is converted from the reliabilityAnalysisDataTable.

createVisualizationData (override):  
Visualization data will make a call to ra.getReliabilities, where afterwards, we convert the reliabilityTable to a 2D String array.

#Tests for ReliabilityVisualization
testCreateColumnHeader()
testCreateVisualizationData1() 
testCreateVisualizationData2() 
testReliabilityNumRows()
testReliabilityTimeSlots()
SetUpReliabilities(String file, int numChannels, double M, double E2E)

#Tests for ReliabilityAnalysis



		
	