package com.jdbcprogram;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollDBService {

	private PreparedStatement empPayrollDataStatement;
	private static EmployeePayrollDBService employeePayrollDBService; 
	
	private EmployeePayrollDBService() {
	}
	
	public static EmployeePayrollDBService getInstance() {
		if(employeePayrollDBService == null) 
			employeePayrollDBService = new EmployeePayrollDBService();
		return employeePayrollDBService;
	}

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
			payrollData = this.getEmployeeData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return payrollData;
	}
	// Updating methods
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
	 * Implementation of update salary of the payroll data using prepared statement
	 * @param name
	 * @param salary
	 * @return
	 */
	private int updateEmployeeDataUsingPreparedStatement(String name, double salary) {
		try (Connection connection = this.getConnection()) {
			String sql = "Update employee_payroll set salary = ? where name = ? ; " ; 
			PreparedStatement prepareStatement = (PreparedStatement) connection.prepareStatement(sql);
			prepareStatement.setDouble(1, salary);
			prepareStatement.setString(2, name);
			return prepareStatement.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Calling Employee Payroll Data
	 * @param name
	 * @param salary
	 * @return
	 */
	public int updateEmployeeData(String name, double salary) {
		return this.updateEmployeeDataUsingPreparedStatement(name, salary);
	}
	
	/**
	 * Getting the Employee Payroll Data from the database
	 * @param name
	 * @return
	 */
	public List<EmployeePayroll> getEmployeePayrollData(String name) {
		List<EmployeePayroll> employeePayrollList = null;
		try {
			if (this.empPayrollDataStatement == null) {
				this.prepareStatementForEmployeeData();
			}
			empPayrollDataStatement.setString(1, name);
			ResultSet resultSet = empPayrollDataStatement.executeQuery();
			employeePayrollList = this.getEmployeeData(resultSet);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return employeePayrollList;
	}
	
	private List<EmployeePayroll> getEmployeePayrollDataUsingDB(String sql) {
		List<EmployeePayroll> list = new ArrayList<>();
		try(Connection connection  = this.getConnection()){
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			list = this.getEmployeeData(result);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * UC3 Prepared Statement Code
	 * @param name
	 * @return
	 */
	public List<EmployeePayroll> getPreparedEmployeeData(String name) {
		List<EmployeePayroll> empPayrollList = null;
		if(this.empPayrollDataStatement == null)
			this.prepareStatementForEmployeeData();
		try {
			empPayrollDataStatement.setString(1, name);
			ResultSet resultSet	= empPayrollDataStatement.executeQuery();
			empPayrollList = this.getEmployeeData(resultSet);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return empPayrollList;
	}

	private List<EmployeePayroll> getEmployeeData(ResultSet resultSet) {
		List<EmployeePayroll> empPayrollList = new ArrayList<>();
		try {
			while(resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("salary");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				empPayrollList.add(new EmployeePayroll(id, name, salary, startDate));
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return empPayrollList;
	}

	public void prepareStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String sql = "Select * from employee_payroll where name = ?";
			empPayrollDataStatement = connection.prepareStatement(sql);
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * UC5_Update data between date range
	 * @param start
	 * @param end
	 * @return
	 */
	public List<EmployeePayroll> getEmployeeForDateRange(LocalDate start, LocalDate end) {
		String sql = String.format("SELECT * FROM employee_payroll WHERE START BETWEEN '%s' AND '%s';",
					 Date.valueOf(start), Date.valueOf(end));
		return this.getEmployeePayrollDataUsingDB(sql);
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
