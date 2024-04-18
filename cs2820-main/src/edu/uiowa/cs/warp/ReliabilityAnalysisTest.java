package edu.uiowa.cs.warp;

import static org.junit.jupiter.api.Assertions.*;


import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import edu.uiowa.cs.warp.ReliabilityAnalysis.RANode;
import edu.uiowa.cs.warp.SystemAttributes.ScheduleChoices;
import edu.uiowa.cs.warp.Visualization.SystemChoices;
import java.util.ArrayList;
import java.util.Arrays;

class ReliabilityAnalysisTest {

	WorkLoad workload;
	WarpInterface warp;
	ReliabilityAnalysis ra;
	ProgramSchedule raTable;
	Description description;
	int numChannels;


	//These are variables for getFinalRowReliability.
	WorkLoad workloadOne;
	WarpInterface warpOne;
	ReliabilityAnalysis raOne;
	
	WorkLoad workloadTwo;
	WarpInterface warpTwo;
	ReliabilityAnalysis raTwo;
	
	WorkLoad workloadThree;
	WarpInterface warpThree;
	ReliabilityAnalysis raThree;


	/**
	 * Tests if the FlowNames and Nodes (Column Headers) are set correctly.
	 */
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testSetFlowNamesAndNodes() {
		WorkLoad workload = new WorkLoad(0.9, 0.99, "Example1a.txt");
		WarpInterface warp = SystemFactory.create(workload, 16, SystemAttributes.ScheduleChoices.PRIORITY);
		ReliabilityAnalysis ra = new ReliabilityAnalysis(warp.toProgram());
		
		WorkLoad workloadTwo = new WorkLoad(0.8, 0.99, "Example1a.txt");
		WarpInterface warpTwo = SystemFactory.create(workloadTwo, 16, SystemAttributes.ScheduleChoices.PRIORITY);
		ReliabilityAnalysis raTwo = new ReliabilityAnalysis(warpTwo.toProgram());
		
		System.out.println(Arrays.toString(ra.getHeaderRow()));
		assertEquals("[F0:A, F0:B, F0:C, F1:C, F1:B, F1:A]", Arrays.toString(ra.getHeaderRow()), "header row from example1a M=0.9 was changed");
		assertEquals("[F0:A, F0:B, F0:C, F1:C, F1:B, F1:A]", Arrays.toString(raTwo.getHeaderRow()), "header row from example1a M=0.8 was changed"); 
		
		String[] headerRow = new String [] {"[F0:A]", "[F1:A]", "[F0:B]"};
		ra.setHeaderRow(headerRow);
		
		assertEquals("[[F0:A], [F1:A], [F0:B]]", Arrays.toString(headerRow), "Header row was not created");
		assertEquals("[[F0:A], [F1:A], [F0:B]]", Arrays.toString(ra.getHeaderRow()), "Header row is not being set");
		assertEquals("[F0:A, F0:B, F0:C, F1:C, F1:B, F1:A]", Arrays.toString(raTwo.getHeaderRow()), "header row from example1a M=0.8 was changed");
	}

	/** 
	 * Tests buildReliabilityTable builds the ReliabilityAnalysis Table correctly.
	 * 
	 * It goes through the rows in the ReliabilityTable and checks if it matches.
	 * It will check the 0th row, the 2nd row, and the 17th row after the first period.
	 * 
	 * It will also check if getReliabilities is accurate.
	 */
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testGetReliabilities() {
		this.SetUpReliabilities("Example1a.txt", 16, 0.9, 0.99);
			
		ReliabilityRow rRow0 = new ReliabilityRow();
		ReliabilityRow rRow6 = new ReliabilityRow();
		ReliabilityRow rRow17 = new ReliabilityRow();
			
		//values are generated for the 0th row of the timeslot.
		rRow0.add(1.0);
		rRow0.add(0.9);
		rRow0.add(0.0);
		rRow0.add(1.0);
		rRow0.add(0.0);
		rRow0.add(0.0);
			
			
		//values are generated for the 6th row of the timeslot.
		rRow6.add(1.0); 
		rRow6.add(0.999);
		rRow6.add(0.9963);
		rRow6.add(1.0);
		rRow6.add(0.999);
		rRow6.add(0.972);
			
		//values are generated for the 17th row of the timeslot.
		rRow17.add(1.0);
		rRow17.add(0.999);
		rRow17.add(0.9963);
		rRow17.add(1.0);
		rRow17.add(0.999);
		rRow17.add(0.9963);
 
		ReliabilityTable raData = ra.getReliabilities();
			
			//10th row of Example1a.txt
			//starts new period
			//1.0	0.9	0.0	1.0	0.999	0.9963
			//1.0	0.999	0.9963	1.0	0.999	0.9963
		assertEquals("[1.0, 0.9, 0.0, 1.0, 0.0, 0.0]", raData.get(0).toString(), "Failed to pull row 0 from ReliabilityTable");
		assertEquals("[1.0, 0.999, 0.9963, 1.0, 0.999, 0.972]", raData.get(6).toString(), "Failed to pull row 6 from ReliabilityTable");
		assertEquals("[1.0, 0.999, 0.9963, 1.0, 0.999, 0.9963]", raData.get(17).toString(), "Failed to pull row 17 from ReliabilityTable");
		assertEquals("[1.0, 0.9, 0.0, 1.0, 0.999, 0.9963]", raData.get(10).toString(), "Failed to pull row after new period from ReliabilityTable");
			
	}
	
	/** 
	 * Tests that verifyReliabilities return true when they are met and false if it is not.
	 * If it does not meet the requirements for E2E and M, it will fail.
	 */
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testVerifyReliabilities() {
		this.SetUpReliabilities("Example1a.txt", 16, 0.9, 0.99);
		assertEquals(true, ra.verifyReliabilities(), "Reliabilities have failed, E2e and M requirements are NOT met");
		// This is for test fail.
		//assertEquals(false, raFalse.verifyReliabilities(), "Reliabilities have passed, E2e and M requirements are met");
	
	}
	
	/** 
	 * Tests if it builds ReliabilityTable correctly.
	 */
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testBuildReliabilityTable() { //tested using getReliabilities 
		WorkLoad workload = new WorkLoad(0.9, 0.99, "Example1a.txt");
		WarpInterface warp = SystemFactory.create(workload, 16, SystemAttributes.ScheduleChoices.PRIORITY);
		ReliabilityAnalysis ra = new ReliabilityAnalysis(warp.toProgram());
		
		
	}
	
	/** 
	 * 
	 * Tests if the first node of the reliabilities are carried to the next row of Reliabilities.
	 * It will first create a ReliabilityTable and RAMap testing the first val goes to the next row.
	 * 
	 * This tests different sizes of ReliabilityTable.
	 */
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testFirstNextReliabilities() {
		this.SetUpReliabilities("Example1a.txt", 16, 0.9, 0.99);
		
		ReliabilityTable firstRT = new ReliabilityTable(2,2);
		ReliabilityAnalysis.RAMap firstNodeMap= new ReliabilityAnalysis.RAMap();
		
		firstRT.set(0, 0, 1.0);
		
		ReliabilityAnalysis.RANode newNode = new ReliabilityAnalysis.RANode(0, 0, true);
		firstNodeMap.put("F0", newNode);
		
		for (int i = 1; i < firstRT.getNumRows(); i++) {
			ra.overrideReliabilities(i, firstNodeMap, firstRT);
		}
		
		ReliabilityTable secondRT = new ReliabilityTable(4,2);
		ReliabilityAnalysis.RAMap secondNodeMap= new ReliabilityAnalysis.RAMap();
		
		secondRT.set(0, 0, 4.0);
		
		RANode newNodeTwo = new ReliabilityAnalysis.RANode(0, 0, true);
		secondNodeMap.put("F0", newNodeTwo);
		
		for (int i = 1; i < secondRT.getNumRows(); i++) {
			ra.overrideReliabilities(i, secondNodeMap, secondRT);
		}
		
		ReliabilityTable RTFourthRow = new ReliabilityTable(4,5);
		ReliabilityAnalysis.RAMap thirdNodeMap= new ReliabilityAnalysis.RAMap();
		
		RTFourthRow.set(0, 0, 1.0);
		
		RANode newNodeThree = new ReliabilityAnalysis.RANode(0, 0, true);
		thirdNodeMap.put("F0", newNodeThree);
		
		for (int i = 1; i < RTFourthRow.getNumRows(); i++) {
			ra.overrideReliabilities(i, thirdNodeMap, RTFourthRow);
		}
		
		assertEquals("[[1.0, 0.0], [1.0, 0.0]]", firstRT.toString(), "ReliabilityTable does not carry the timeslot for next timeslot.");
		assertEquals("[[4.0, 0.0], [4.0, 0.0], [4.0, 0.0], [4.0, 0.0]]", secondRT.toString(), "ReliabilityTable does not carry the next timeslot for the 4.0 timeslot");
		assertEquals("[[1.0, 0.0, 0.0, 0.0, 0.0], [1.0, 0.0, 0.0, 0.0, 0.0], [1.0, 0.0, 0.0, 0.0, 0.0], [1.0, 0.0, 0.0, 0.0, 0.0]]", RTFourthRow.toString(), "ReliabilityTable does not carry for all rows");
	}

	
	/** 
	 * Tests if the reliabilities are set correctly.
	 * 
	 * Checks if the old ReliabilityTable is not overwritten with other reliabilities.
	 * DummyTables are created to check if RT is set correctly on empty columns and rows.
	 */
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testsetRA() {
		this.SetUpReliabilities("Example1a.txt", 16, 0.9, 0.99);
		
		ReliabilityTable RT = new ReliabilityTable(3,3);
		ReliabilityAnalysis.RAMap RAmapofNodes = new ReliabilityAnalysis.RAMap();
		
		RT.set(0, 0, 2.0);
		RANode newNode = new ReliabilityAnalysis.RANode(0, 0, true);
		RAmapofNodes.put("F1", newNode);
		
		ra.setRA(RT);
		assertEquals("[[2.0, 0.0, 0.0], [0.0, 0.0, 0.0], [0.0, 0.0, 0.0]]", RT.toString(), "reliabilities are not being set correctly");
		
		RT.set(0, 0, 3.0);
		ra.setRA(RT);
		assertEquals("[[3.0, 0.0, 0.0], [0.0, 0.0, 0.0], [0.0, 0.0, 0.0]]", RT.toString(), "new reliabilities are not overwriting old reliabilities");
		
		ReliabilityTable emptyRT = new ReliabilityTable(1,0);
		ReliabilityAnalysis.RAMap secondNodeMap= new ReliabilityAnalysis.RAMap();
		secondNodeMap.put("F1", newNode);
		RT.set(0, 0, 0.0);
		ra.setRA(emptyRT);
		assertEquals("[[]]", emptyRT.toString(), "reliabilty columns should be empty");
		
		ReliabilityTable emptyRowRT = new ReliabilityTable(0,1);
		ReliabilityAnalysis.RAMap thirdNodeMap= new ReliabilityAnalysis.RAMap();
		thirdNodeMap.put("F1", newNode);
		RT.set(0, 0, 0.0);
		ra.setRA(emptyRowRT);
		assertEquals("[]", emptyRowRT.toString(), "reliability table should have no rows");
		
	}
	
	/**
	 * Tests the flows set to the initial state.
	 * 
	 * It sets the src nodes to equal 1, then converts each hashmap to iterate through those entries.
	 * afterwards, it then changes the value of the reliabilityTable.
	 */
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testSetInitialStateFlows() {
		this.SetUpReliabilities("Example1a.txt", 16, 0.9, 0.99);
		
		ReliabilityTable RT = new ReliabilityTable(3,3);
		ReliabilityAnalysis.RAMap nodeMap= new ReliabilityAnalysis.RAMap();
		
		RT.set(0, 0, 2.0);
		RANode newNode = new ReliabilityAnalysis.RANode(0, 0, true);
		
		nodeMap.put("F1", newNode);
		
		assertEquals("[[2.0, 0.0, 0.0], [0.0, 0.0, 0.0], [0.0, 0.0, 0.0]]", RT.toString(), "Original reliability table has been changed");
		
		ra.setInitialSrcFlows(nodeMap, RT);

		assertEquals("[[1.0, 0.0, 0.0], [1.0, 0.0, 0.0], [1.0, 0.0, 0.0]]", RT.toString(), "Reliability table has not be set to initial state");
		
	}
	
	/** 
	 * 
	 * It first creates the ReliabilityAnalysis object and method and grabs the last ReliabilityRow in the
	 * ReliabilityTable.
	 * 
	 * It then compares the final row from each ReliabilityAnalysis object.
	 */
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testGetFinalRowReliability() {
		
		this.FinalSetUpReliabilityOne("Example1a.txt", 16, 0.9, 0.99);
		this.FinalSetUpReliabilityTwo("Example1a.txt", 16, 0.75, 0.99);
		this.FinalSetUpReliabilityThree("Example1a.txt", 16, 0.8, 0.99);
		
		
		assertEquals("[1.0, 0.999, 0.9963, 1.0, 0.999, 0.9963]", Arrays.toString(ra.getFinalRow()), "The last reliability row in the ra table does not match");
		assertEquals("[1.0, 0.99609375, 0.9931640625, 1.0, 0.99609375, 0.9931640625]", Arrays.toString(raTwo.getFinalRow()), "The last reliability row in the ra table does not match");
		assertEquals("[1.0, 0.9984, 0.9932799999999999, 1.0, 0.9984, 0.9932799999999999]", Arrays.toString(raThree.getFinalRow()), "The last reliability row in the ra table does not match");
		//expected row10 1.0	0.9984	0.9932799999999999	1.0	0.9984	0.9932799999999999
		// 1.0	0.999	0.9963	1.0	0.999	0.9963
	}


	/**
	 * Helper method takes the file and number of channels before creating ReliabilityAnalysis
	 * @param file of text file
	 * @param numChannels of Reliabilities 
	 */


	public void SetUpReliabilities(String file, int numChannels, double M, double E2E) {
		workload = new WorkLoad(0.9,0.99, file);
		ra = new ReliabilityAnalysis (warp.toProgram());
		warp = SystemFactory.create(workload, numChannels, ScheduleChoices.RTHART);

	}
	
	/**
	 * Helper method for the testGetFinalRow
	 * @param file of the text file
	 * @param numChannels of Reliabilities
	 * @param M
	 * @param E2E
	 */
	public void FinalSetUpReliabilityOne(String file, int numChannels, double M, double E2E) {
		workloadOne = new WorkLoad(M, E2E, file);
		warpOne = SystemFactory.create(workload, numChannels, ScheduleChoices.PRIORITY);
		raOne = new ReliabilityAnalysis(warp.toProgram());
	}
	
	/**
	 * Helper method for the testGetFinalRow
	 * @param file of the text file
	 * @param numChannels of Reliabilities
	 * @param M
	 * @param E2E
	 */
	public void FinalSetUpReliabilityTwo(String file, int numChannels, double M, double E2E) {
		workloadTwo = new WorkLoad(M, E2E, file);
		warpTwo = SystemFactory.create(workload, numChannels, ScheduleChoices.PRIORITY);
		raTwo = new ReliabilityAnalysis(warpTwo.toProgram());
	}
	
	/**
	 * Helper method for the testGetFinalRow
	 * @param file of the text file
	 * @param numChannels of Reliabilities
	 * @param M
	 * @param E2E
	 */
	public void FinalSetUpReliabilityThree(String file, int numChannels, double M, double E2E) {
		workloadThree = new WorkLoad(M, E2E, file);
		warpThree = SystemFactory.create(workload, numChannels, ScheduleChoices.PRIORITY);
		raThree = new ReliabilityAnalysis(warpThree.toProgram());
	}
}
