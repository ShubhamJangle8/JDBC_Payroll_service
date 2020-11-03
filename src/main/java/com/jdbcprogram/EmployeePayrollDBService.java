package com.jdbcprogram;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeePayrollDBService {

	/**
	 * Reading data from database
	 * @return
	 */
	public List<EmployeePayroll> readData() {
		String sql = "Select * from Employee_Payroll";
		List<EmployeePayroll> payrollData = new ArrayList<>();
		try (Connection connection = this.getConnection();){
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("salary");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				payrollData.add(new EmployeePayroll(id, name, salary, startDate));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return payrollData;
	}
	
	/**
	 * Updating the payroll data in database
	 * @param name
	 * @param salary
	 * @return
	 */
	public int updateDataUsingStatement(String name, double salary) {
		int result = 0;
		String sql = String.format("UPDATE Employee_Payroll SET salary = %.2f where name = '%s';", salary, name);
		try (Connection connection = this.getConnection();){
			Statement statement = connection.createStatement();
			result = statement.executeUpdate(sql);
		}
		catch(SQLException s) {
			s.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Getting the Employee Payroll Data from the memory
	 * @param name
	 * @return
	 */
	public List<EmployeePayroll> getEmployeeData(String name) {
		return readData().stream().filter(employee -> employee.name.equals(name)).collect(Collectors.toList());
	}

	/**
	 * Getting connection for each sql query
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		Connection connection = null;
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";;
		String password = "1234";
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		return connection;
	}

}
