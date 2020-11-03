package com.jdbcprogram;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EmployeePayrollService {
	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	List<EmployeePayroll> empPayrollArrayList;

	public EmployeePayrollService() {

	}
	
	public EmployeePayrollService(List<EmployeePayroll> empPayrollArray) {
		super();
		this.empPayrollArrayList = empPayrollArray;
	}

	public void writeEmployeePayrollData(IOService ioService) {
		if (ioService == IOService.CONSOLE_IO) {
			System.out.println("The employee details are : " + empPayrollArrayList);
		} else if (ioService == IOService.FILE_IO)
			new EmployeePayrollFileIOService().writeData(empPayrollArrayList);
	}

	public void printData() {
		try {
			Files.lines(new File("payroll-file.text").toPath()).forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readEmployeeData(IOService ioService) {
		Scanner scanner = new Scanner(System.in);
		if (ioService.equals(IOService.CONSOLE_IO)) {
			System.out.println("Enter Employee ID : ");
			int id = scanner.nextInt();
			scanner.nextLine();
			System.out.println("Enter Employee Name : ");
			String name = scanner.nextLine();
			System.out.println("Enter Employee Salary : ");
			double salary = scanner.nextDouble();
			empPayrollArrayList.add(new EmployeePayroll(id, name, salary));
		} else if (ioService.equals(IOService.FILE_IO)) {
			System.out.println("Reading data from file");
			new EmployeePayrollFileIOService().printData();
		}
	}
	
	/**
	 * Reading details from database UC1
	 * @param ioService
	 * @return
	 */
	public List<EmployeePayroll> readEmployeeDataFromDB(IOService ioService){
		if(ioService.equals(IOService.DB_IO)) {
			this.empPayrollArrayList = new EmployeePayrollDBService().readData();
		}
		return this.empPayrollArrayList;
	}
	
	/**
	 * Updating the salary in database as well as the employee payroll data UC2
	 * @param name
	 * @param salary
	 */
	public void updateSalary(String name, double salary) {
		int countUpdates = new EmployeePayrollDBService().updateDataUsingStatement(name, salary);
		if(countUpdates == 0)
			return;
		EmployeePayroll empPayrollData= this.getEmployeePayrollData(name);
		if(empPayrollData != null)
			empPayrollData.salary = salary;
	}
	
	private EmployeePayroll getEmployeePayrollData(String name) {
		return this.empPayrollArrayList.stream().filter(employeePayrollItem -> employeePayrollItem.name.equals(name)).findFirst().orElse(null);
	}
	
	/**
	 * Checking for Employee Data in sync with database
	 * @param name
	 * @return
	 */
	public boolean checkEmployeeDataSync(String name) {
		List<EmployeePayroll> employees = null;
		employees = new EmployeePayrollDBService().getEmployeeData(name);
		System.out.println(employees);
		System.out.println(getEmployeePayrollData(name));
		return employees.get(0).equals(getEmployeePayrollData(name));
	}
	
	/**
	 * Counting number of entries in file operations
	 * @param fileIo
	 * @return
	 */
	public long countEntries(IOService fileIo) {
		long entries = 0;
		try {
			entries = Files.lines(new File("payroll-file.text").toPath()).count();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entries;
	}

	/**
	 * Main function
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ArrayList<EmployeePayroll> empPayrollArray = new ArrayList<>();
		Scanner input = new Scanner(System.in);
		EmployeePayrollService empPayrollService = new EmployeePayrollService(empPayrollArray);
		System.out.println("Welcome to Employee Payroll Service");
		empPayrollService.readEmployeeData(IOService.FILE_IO);
		empPayrollService.writeEmployeePayrollData(IOService.FILE_IO);
	}

}
