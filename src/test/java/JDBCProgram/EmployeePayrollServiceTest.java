package JDBCProgram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import JDBCProgram.EmployeePayrollService.IOService;

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
	 * UC1 JDBC
	 * Matching number Of retrieved entries from database
	 */
	@Test
	public void givenEmployeePayrollInDB_WhenRetrieved_ShouldMatchNumberofEntries() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayroll> employeePayrollData = employeePayrollService.readEmployeeDataFromDB(IOService.DB_IO);
		assertEquals(3, employeePayrollData.size());
	}
	
	/**
	 * UC2 JDBC
	 * Updating new Salary and syncing with java 
	 */
	@Test
	public void givenNewSalaryForEmployee_WhenUpdatedShouldSyncWithDB() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayroll> employeePayrollData = employeePayrollService.readEmployeeDataFromDB(IOService.DB_IO);
		employeePayrollService.updateSalary("Terisa", 10000000.00);
		boolean result = employeePayrollService.checkEmployeeDataSync("Terisa");
		assertTrue(result);
	}
}
	