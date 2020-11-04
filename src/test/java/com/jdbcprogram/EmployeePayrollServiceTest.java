package com.jdbcprogram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.jdbcprogram.EmployeePayrollService.IOService;

public class EmployeePayrollServiceTest {
	@Test
	public void given3Employees_WhenWrittenToFile_ShouldMatchEmployeeEntries() {
		EmployeePayrollService employeePayrollService;
		EmployeePayroll[] arrayOfEmps = { new EmployeePayroll(1, "Shubham", 20000),
				new EmployeePayroll(2, "Rohan", 30000), new EmployeePayroll(3, "Aditya", 40000) };
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		employeePayrollService.writeEmployeePayrollData(IOService.FILE_IO);
		long entriesFile = employeePayrollService.countEntries(IOService.FILE_IO);
		assertEquals(3, entriesFile);
	}
	
	@Test
	public void given3Employees_WhenPrintedToConsole_ShouldMatchEmployeeEntries() {
		EmployeePayrollService employeePayrollService;
		EmployeePayroll[] arrayOfEmps = { new EmployeePayroll(1, "Shubham", 20000),
				new EmployeePayroll(2, "Rohan", 30000), new EmployeePayroll(3, "Aditya", 40000) };
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		employeePayrollService.printData();
		long entriesConsole = employeePayrollService.countEntries(IOService.CONSOLE_IO);
		assertEquals(3, entriesConsole);
	}
	
	@Test
	public void given3Employees_WhenReadToConsole_ShouldMatchEmployeeEntries() {
		EmployeePayrollService employeePayrollService;
		EmployeePayroll[] arrayOfEmps = { new EmployeePayroll(1, "Shubham", 20000),
				new EmployeePayroll(2, "Rohan", 30000), new EmployeePayroll(3, "Aditya", 40000) };
		employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmps));
		employeePayrollService.readEmployeeData(IOService.FILE_IO);
		long entriesConsole = employeePayrollService.countEntries(IOService.CONSOLE_IO);
		assertEquals(3, entriesConsole);
	}
	
	/**
	 * UC2 JDBC
	 * Matching number Of retrieved entries from database
	 */
	@Test
	public void givenEmployeePayrollInDB_WhenRetrieved_ShouldMatchNumberofEntries() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayroll> employeePayrollData = employeePayrollService.readEmployeeDataFromDB(IOService.DB_IO);
		assertEquals(3, employeePayrollData.size());
	}
	
	/**
	 * UC3 and UC4 JDBC
	 * Updating new Salary and syncing with java using prepared statement
	 */
	@Test
	public void givenNewSalaryForEmployee_WhenUpdatedShouldSyncWithDB() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayroll> employeePayrollData = employeePayrollService.readEmployeeDataFromDB(IOService.DB_IO);
		employeePayrollService.updateSalary("Terisa", 10000000.00);
		boolean result = employeePayrollService.checkEmployeeDataSync("Terisa");
		assertEquals(3, employeePayrollData.size());
		assertTrue(result);
	}
	
	/**
	 * //UC5 JDBC retrieve employee payroll between specfic dates
	 */
	@Test
	public void givenDateRange_WhenRetrieved_ShouldMatchEmployeeCount() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeeDataFromDB(IOService.DB_IO);
	    LocalDate start = LocalDate.of(2018, 8, 01);
	    LocalDate end = LocalDate.now();
	    List<EmployeePayroll> employeePayrollData = employeePayrollService.readEmployeePayrollForDateRange(start, end);
	    assertEquals(2, employeePayrollData.size());
	}
	
	/**
	 * //UC6 JDBC average salary for gender
	 */
	@Test
	public void givenPayrollData_WhenAvgSalaryRetrievedByGender_ShouldReturnProperValue() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeeDataFromDB(IOService.DB_IO);
	    Map<String, Double> avgSalaryByGender = employeePayrollService.readAvgSalaryByGender();
	    assertTrue(avgSalaryByGender.get("M").equals(20000000.0));
	    assertTrue(avgSalaryByGender.get("F").equals(10000000.0));
	}
	
	//UC7 Adding Employees To the database
	@Test
	public void givenPayrollData_WhenAddedNewEntry_ShouldSyncWithDB() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		employeePayrollService.readEmployeeData(IOService.DB_IO);
		employeePayrollService.addEmployeeToPayroll("Shivam", 20000000.00, LocalDate.now(), "M");
		
	    boolean result = employeePayrollService.checkEmployeeDataSync("Shivam");
	    assertTrue(result);
	}
	
}
	