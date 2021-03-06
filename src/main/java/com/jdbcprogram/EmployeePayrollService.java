package com.jdbcprogram;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EmployeePayrollService {
	private static final Logger LOG = LogManager.getLogger(EmployeePayrollService.class);
	
	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	List<EmployeePayroll> empPayrollArrayList = new ArrayList<>();
	private EmployeePayrollDBService employeePayrollDBService;

	/**
	 * Getting instance of EmployeeDBService by one call
	 */
	public EmployeePayrollService() {
		this.employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}
	
	public EmployeePayrollService(List<EmployeePayroll> empPayrollArrayList) {
		this();
		this.empPayrollArrayList = new ArrayList<>(empPayrollArrayList);
	}
	
	/**
	 * Counting number of entries in file operations
	 * @param fileIo
	 * @return
	 */
	public long countEntries(IOService io) {
		long entries = 0;
		if (io.equals(IOService.REST_IO)) {
			entries = empPayrollArrayList.size();
		}
		else {
			try {
				entries = Files.lines(new File("payroll-file.text").toPath()).count();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return entries;
	}

	public List<EmployeePayroll> readEmployeeData(IOService ioService) throws payrollServiceDBException {
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
			return null;
		} else if (ioService.equals(IOService.FILE_IO)) {
			System.out.println("Reading data from file");
			new EmployeePayrollFileIOService().printData();
			return null;
		}
		else if(ioService.equals(IOService.DB_IO)) {
			this.empPayrollArrayList = employeePayrollDBService.readData();
			return this.empPayrollArrayList;
		}
		return null;
		
	}
	
	/**
	 * Updating the salary in database as well as the employee payroll data UC2
	 * @param name
	 * @param salary
	 * @throws payrollServiceDBException 
	 */
	public void updateSalary(String name, double salary) throws payrollServiceDBException {
		int countUpdates = employeePayrollDBService.updateEmployeeData(name, salary);
		if(countUpdates == 0)
			return;
		EmployeePayroll empPayrollData= this.getEmployeePayrollData(name);
		if(empPayrollData != null)
			empPayrollData.salary = salary;
	}
	
	/**
	 * Get employee payroll data from memory
	 * @param name
	 * @return
	 */
	public EmployeePayroll getEmployeePayrollData(String name) {
		return this.empPayrollArrayList.stream().filter(employeePayrollItem -> employeePayrollItem.name.equals(name)).findFirst().orElse(null);
	}
	
	/**
	 * Get employees for particular dates
	 * @param start
	 * @param end
	 * @return
	 * @throws payrollServiceDBException 
	 */
	public List<EmployeePayroll> readEmployeePayrollForDateRange(LocalDate start, LocalDate end) throws payrollServiceDBException {
		return employeePayrollDBService.getEmployeeForDateRange(start, end);
	}	
	
	/**
	 * Getting the average value by gender
	 * @return
	 * @throws payrollServiceDBException 
	 */
	public Map<String, Double> readAvgSalaryByGender() throws payrollServiceDBException {
		return employeePayrollDBService.getAvgSalaryByGender();
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
	
	/**
	 * adds employee details to database
	 * 
	 * @param name
	 * @param gender
	 * @param salary
	 * @param date
	 */
	public void addEmployeeToPayroll(String name, String gender, double salary, LocalDate date,
			List<String> departments) {
		try {
			employeePayrollDBService.addEmployeeToPayroll(name, gender, salary, date, departments);
		} catch (payrollServiceDBException | SQLException exception) {
			System.out.println(exception.getMessage());
		}
	}
	
	public void addMultipleEmployeesToPayroll(List<EmployeePayroll> employeeDataList) {
		employeeDataList.forEach(employee -> {
			try {
				employeePayrollDBService.addEmployeeToPayroll(employee.name,employee.gender,employee.salary,employee.date,employee.departments);
			} catch (SQLException | payrollServiceDBException e) {
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * adding multiple employees to payroll with thread and used logs
	 * @param employeeDataList
	 */
	public void addMultipleEmployeesToPayrollWithThreads(List<EmployeePayroll> employeeDataList) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		employeeDataList.forEach(employee -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(employee.hashCode(), false);
				LOG.info("Employee Being Added: " + Thread.currentThread().getName());
				try {
					employeePayrollDBService.addEmployeeToPayroll(employee.name, employee.gender, employee.salary,
																  employee.date, employee.departments);
				} catch (SQLException | payrollServiceDBException e) {
					System.out.println(e.getMessage());
				}
				employeeAdditionStatus.put(employee.hashCode(), true);
				LOG.info("Employee Added: " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employee.name);
			thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void addEmployeeToPayroll(EmployeePayroll employeePayrollData, IOService ioService) {
		if(ioService.equals(ioService.REST_IO)) {
			empPayrollArrayList.add(employeePayrollData);
		}
	}
	
	/**
	 * Adding employee object to payroll added to json
	 * @param employee
	 */
	public void addEmployeeToPayroll(EmployeePayroll employee) {
		addEmployeeToPayroll(employee.name, employee.gender, employee.salary, employee.date, Arrays.asList(""));
	}

	/**
	 * deletes employee record from database
	 * @param id
	 */
	public void deleteEmployeeFromPayroll(int id) {
		try {
			employeePayrollDBService.deleteEmployeeFromPayroll(id);
		} catch (payrollServiceDBException exception) {
			System.out.println(exception.getMessage());
		}
	}
	
	/**
	 * returns list of active employees
	 * @param id
	 * @return
	 */
	public List<EmployeePayroll> removeEmployeeFromPayroll(int id) {
		List<EmployeePayroll> onlyActiveList = null;
		try {
			onlyActiveList = employeePayrollDBService.removeEmployeeFromCompany(id);
		} catch (payrollServiceDBException e) {
			System.out.println(e.getMessage());
		}
		return onlyActiveList;
	}
	
	/**
	 * given name and updated salary of employee updates in the database
	 * @param name
	 * @param salary
	 */
	public void updateEmployeePayrollSalary(String name, double salary) {
		int result = 0;
		try {
			result = employeePayrollDBService.updateEmployeeData(name, salary);
		} catch (payrollServiceDBException exception) {
			System.out.println(exception.getMessage());
		}
		if (result == 0) {
			return;
		}
		EmployeePayroll employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null) {
			employeePayrollData.salary = salary;
		}
	}
	
	/**
	 * updates multiple rows in database
	 * @param newSalaryMap
	 */
	public void updateMultipleSalaries(Map<String, Double> newSalaryMap) {
		Map<Integer, Boolean> employeeAdditionStatus = new HashMap<Integer, Boolean>();
		newSalaryMap.forEach((k, v) -> {
			Runnable task = () -> {
				employeeAdditionStatus.put(k.hashCode(), false);
				LOG.info("Employee Being updated : " + Thread.currentThread().getName());
				this.updateEmployeePayrollSalary(k, v);
				employeeAdditionStatus.put(k.hashCode(), true);
				LOG.info("Employee updated : " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, k);
			thread.start();
		});
		while (employeeAdditionStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**Json Usecase5: deleting employee from the cache as well as json server
	 * @param name
	 * @param ioService
	 */
	public void deleteEmployee(String name, IOService ioService) {
		if(ioService.equals(IOService.REST_IO)) {
			EmployeePayroll employee = this.getEmployeePayrollData(name);
			empPayrollArrayList.remove(employee);
		}
	}
	
	/**
	 * checks if add data updated is in sync
	 * @param nameList
	 * @return
	 */
	public boolean checkEmployeeListSync(List<String> nameList) {
		List<Boolean> resultList = new ArrayList<>();
		nameList.forEach(name -> {
			resultList.add(checkEmployeePayrollInSyncWithDB(name));
		});
		if (resultList.contains(false)) {
			return false;
		}
		return true;
	}
	
	/**
	 * checks if record matches with the updated record
	 * @param name
	 * @return
	 */
	public boolean checkEmployeePayrollInSyncWithDB(String name) {
		List<EmployeePayroll> employeePayrollDataList = null;
		try {
			employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
		} catch (payrollServiceDBException exception) {
			System.out.println(exception.getMessage());
		}
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}

	/**
	 * Main function
	 * @param args
	 * @throws IOException
	 * @throws payrollServiceDBException 
	 */
	public static void main(String[] args) throws IOException, payrollServiceDBException {
		ArrayList<EmployeePayroll> empPayrollArray = new ArrayList<>();
		Scanner input = new Scanner(System.in);
		EmployeePayrollService empPayrollService = new EmployeePayrollService(empPayrollArray);
		System.out.println("Welcome to Employee Payroll Service");
		empPayrollService.readEmployeeData(IOService.FILE_IO);
		empPayrollService.writeEmployeePayrollData(IOService.FILE_IO);
	}
	
}
