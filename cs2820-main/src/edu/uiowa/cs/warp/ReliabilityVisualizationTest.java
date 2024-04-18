package edu.uiowa.cs.warp;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import edu.uiowa.cs.warp.SystemAttributes.ScheduleChoices;
class ReliabilityVisualizationTest {
	
	WorkLoad workload;
	WarpInterface warp;
	ReliabilityVisualization raVisualization;
	ReliabilityAnalysis ra;
	ProgramSchedule raTable;
	Description description;
	int numChannels;
	double M;
	double E2E;
	
/**
* Tests to see if a header is properly created with the correct ordering and information for the name,
* Schedule name, minimum packet reception, E2E, and number of Channels. It uses the helper method SetUpReliabilities
* in order to initialize the file that will be tested.
* IMPORTANT NOTE: The test appears to be returning all the correct values, however, there seems to be a formatting
* error that is causing the test to fail. there are extra spaces after the string for expectedName,
* and we were not able to determine how to fix it. Aside from that, the test returns the asked values
*/
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testCreateHeader() {
		this.SetUpReliabilities("StressTest.txt", 16, 0.9, 0.99);
		String expectedName;
		String expectedSchedName;
		String expectedM;
		String expectedE2E;
		String expectednChannels;
			
		Description actualNames = raVisualization.createHeader();
			
		expectedName = String.format("Reliability Analysis for graph %s","StressTest\n");
		assertEquals(expectedName, actualNames.get(0), "createHeader did not return the right name." );
		
		expectedSchedName = String.format("Scheduler Name: %s","RealtimeHART\n");
		assertEquals(expectedSchedName, actualNames.get(1), "createHeader did not return the right scheduler name");
	
		expectedM = String.format("Min Packet Reception rate: %s", "0.9\n");
		assertEquals(expectedM, actualNames.get(2), "createHeader did not return the right amount for M" );
		
		expectedE2E = String.format("E2E: %s", "0.99\n");
		assertEquals(expectedE2E, actualNames.get(3), "createHeader did not return the right E2E" );
		
		expectednChannels = String.format("number of Channels: %s", "16\n");
		assertEquals(expectednChannels, actualNames.get(4), "createHeader did not return the right number of Channels" );
		
		String expectedNames = expectedName + expectedSchedName + expectedM + expectedE2E + expectednChannels;
		assertEquals(expectedNames, actualNames.toString(), "createHeader did not correctly create in the right order.");
	}
/**
* Edge case for testCreateHeader: tests on an empty file to see if correct results are returned
*/
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testCreateNullHeader() {
		this.SetUpReliabilities("EmptyWorkload.txt", 16, 0.9, 0.99);
		String expectedName;
		String expectedSchedName;
		String expectedM;
		String expectedE2E;
		String expectednChannels;
			
		Description actualNames = raVisualization.createHeader();
			
		expectedName = String.format("Reliability Analysis for graph %s","EmptyWorkload\n");
		assertEquals(expectedName, actualNames.get(0), "createHeader did not return the right name." );
		
		expectedSchedName = String.format("Scheduler Name: %s","RealtimeHART\n");
		assertEquals(expectedSchedName, actualNames.get(1), "createHeader did not return the right scheduler name");
	
		expectedM = String.format("Min Packet Reception rate: %s", "0.9\n");
		assertEquals(expectedM, actualNames.get(2), "createHeader did not return the right amount for M" );
		
		expectedE2E = String.format("E2E: %s", "0.99\n");
		assertEquals(expectedE2E, actualNames.get(3), "createHeader did not return the right E2E" );
		
		expectednChannels = String.format("number of Channels: %s", "16\n");
		assertEquals(expectednChannels, actualNames.get(4), "createHeader did not return the right number of Channels" );
		
		String expectedNames = expectedName + expectedSchedName + expectedM + expectedE2E + expectednChannels;
		assertEquals(expectedNames, actualNames.toString(), "createHeader did not correctly create in the right order.");
	}
	
	
/**
* When Implemented, this test will be able to test that a Column Header is created with the correct column names,
* in the correct order. We cannot complete this test until we get a ReliabilityAnalysis table, because
* that is used to get the information we need for initializing columns.
*
*/
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testCreateColumnHeader() {
		this.SetUpReliabilities("EmptyWorkload.txt", 16, 0.9, 0.99);
		var actualHeader = raVisualization.createColumnHeader();
		System.out.print(Arrays.toString(actualHeader));
		String[] expectedHeader = {"Time Slot"};
		assertEquals(Arrays.toString(expectedHeader),Arrays.toString(actualHeader), "Visualization creates an incorrect number of reliabilities");
	}
/**
* When CreateVisualizationData is implemented, this test will take a given file, and make sure that
* all of the data will be properly made into a matrix, and that all of the data provided is accurate to
* the file that is being tested on.
*/
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testCreateVisualizationData1() {
		this.SetUpReliabilities("ExampleX.txt", 5, 0.9, 0.99);
		ra.buildReliabilityAnalysisTable();
		int expectedChannels = 5;
		String expectedVisualization = "";
		
		for (int i = 0; i < expectedChannels; i++) {
			String expectedRow = String.format("%s\thi\thi\thi\thi\thi\thi\thi\thi\thi\thi\n", i);
			expectedVisualization += expectedRow;
		}
		
		String actualVisualization = raVisualization.createVisualizationData().toString();
		System.out.print(false);
		assertEquals(expectedVisualization,actualVisualization,"Visualizations do not match");
	}
	
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testCreateVisualizationData2() {
		
	}
	
/**
 * The goal of testReliabilityNumRows is to test that the visualization table is generating the correct amount of rows.
 */
	/*
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testReliabilityNumRows() {
		this.SetUpReliabilities("EmptyWorkload.txt", 16, 0.9, 0.99);
		int actualRows = raVisualization.createVisualizationData().size()-1;
		int expectedRows =16;
		assertEquals(expectedRows,actualRows, "Visualization creates an incorrect number of reliabilities");
	}
	*/

	
/**
 * The goal of testReliabilityNumRows is to test that the visualization table is generating the correct amount of rows.
 */
	/*
	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	void testReliabilityTimeSlots() {
		this.SetUpReliabilities("ExampleX.txt", 16, 0.9, 0.99);
		String expectedTimeSlots = "Time Slot\t0\t1\t2\t3\t4\t5\t6\t7\t8\t9\t";
		String actualTimeSlots = raVisualization.visualizeAnalysisData().get(0);
		assertEquals(expectedTimeSlots,actualTimeSlots, "Visualization creates an incorrect number of time slots");
	}
	*/

/**
* Helper method to help initialize test files.
* @param file to be tested on
* @param numChannels number of channels
* @param M minimum packet reception
* @param E2E edge to edge reliability.
*/
	
	
	public void SetUpReliabilities(String file, int numChannels, double M, double E2E) {
		workload = new WorkLoad(0.9,0.99, file);
		warp = SystemFactory.create(workload, numChannels, ScheduleChoices.RTHART);
		raVisualization = new ReliabilityVisualization(warp);
		raVisualization.createHeader();
		System.out.println(raVisualization.createHeader());
	}
}
	

