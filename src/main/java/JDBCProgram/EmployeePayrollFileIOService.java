package JDBCProgram;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollFileIOService {
	public static String PAYROLL_FILE_NAME = "payroll-file.text";

	public void writeData(List<EmployeePayroll> empPayrollArray) {
		StringBuffer empBuffer = new StringBuffer();
		empPayrollArray.forEach(employee -> {
			String employeeDataString = employee.toString().concat("\n");
			empBuffer.append(employeeDataString);
		});
		try {
			Files.write(Paths.get(PAYROLL_FILE_NAME), empBuffer.toString().getBytes());
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long countEntries() {
		long entries = 0;
		try {
			entries = Files.lines(new File("payroll-file.text").toPath()).count();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return entries;
	}
	
	public void printData() {
		try {
			Files.lines(new File("payroll-file.text").toPath()).forEach(System.out::println);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<EmployeePayroll> readData() {
		List<EmployeePayroll> list = new ArrayList<>();
		try {
			Files.lines(new File(PAYROLL_FILE_NAME).toPath()).map(line -> line.trim()).forEach(line -> {
				String[] data = line.split("(, )");
				String[] newData = new String[10];
				int index = 0;
				for (String d : data) {
					String[] splitData = d.split("(=)");
					newData[index] = splitData[1];
					index++;
				}
				list.add(new EmployeePayroll(Integer.parseInt(newData[0]), newData[1],
						Double.parseDouble(newData[2])));

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}
