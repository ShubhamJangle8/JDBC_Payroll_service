package JDBCProgram;

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

	List<EmployeePayroll> empPayrollArray;

	public EmployeePayrollService() {

	}
	
	public EmployeePayrollService(List<EmployeePayroll> empPayrollArray) {
		super();
		this.empPayrollArray = empPayrollArray;
	}

	public void writeEmployeePayrollData(IOService ioService) {
		if (ioService == IOService.CONSOLE_IO) {
			System.out.println("The employee details are : " + empPayrollArray);
		} else if (ioService == IOService.FILE_IO)
			new EmployeePayrollFileIOService().writeData(empPayrollArray);
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
			empPayrollArray.add(new EmployeePayroll(id, name, salary));
		} else if (ioService.equals(IOService.FILE_IO)) {
			System.out.println("Reading data from file");
			new EmployeePayrollFileIOService().printData();
		}
	}
	
	public List<EmployeePayroll> readEmployeeDataFromDB(IOService ioService){
		if(ioService.equals(IOService.DB_IO)) {
			this.empPayrollArray = new EmployeePayrollDBService().readData();
		}
		return this.empPayrollArray;
	}
	
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
