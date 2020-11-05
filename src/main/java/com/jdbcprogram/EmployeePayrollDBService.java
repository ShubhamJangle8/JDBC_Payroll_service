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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * Reading data from database
	 * @return
	 */
	public List<EmployeePayroll> readData() {
		String sql = "Select * from Employee_Payroll";
		return this.getEmployeePayrollDataUsingDB(sql);
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
	 * Get Employee average salary by gender
	 * @return
	 */
	public Map<String, Double> getAvgSalaryByGender() {
		String sql = "select gender, AVG(salary) as avgSalary from employee_payroll group by gender;";
		Map<String, Double> genderToAvgSalaryMap = new HashMap<>();
		try(Connection connection  = this.getConnection()){
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("avgSalary");
				genderToAvgSalaryMap.put(gender, salary);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToAvgSalaryMap;
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
	 * Insert new employee data into table
	 * @param name
	 * @param salary
	 * @param start
	 * @param gender
	 * @return
	 */
	public EmployeePayroll addEmployeeToPayrollUC7(String name, double salary, LocalDate start, String gender) {
		int id = -1;
		EmployeePayroll data = null;
		String sql = String.format("INSERT INTO employee_payroll (name, gender, salary,start)"+
				"VALUES( '%s', '%s', '%s', '%s')", name, gender, salary, Date.valueOf(start));
		try(Connection connection  = this.getConnection()){
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if(rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if(resultSet.next()) id = resultSet.getInt(1);
			}
			data = new EmployeePayroll(id, name, salary, start);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public EmployeePayroll addEmployeeToPayrollUC8(String name, double salary, LocalDate start, String gender) {
		int id = -1;
		EmployeePayroll data = null;
		Connection connection = null;
		try{
			connection  = this.getConnection();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		try(Statement statement = connection.createStatement()){
			String sql = String.format("INSERT INTO employee_payroll (name, gender, salary,start)"+
						 "VALUES( '%s', '%s', '%s', '%s')", name, gender, salary, Date.valueOf(start));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if(rowAffected == 1) {
			ResultSet resultSet = statement.getGeneratedKeys();
			if(resultSet.next()) id = resultSet.getInt(1);
			}
			data = new EmployeePayroll(id, name, salary, start);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		try(Statement statement = connection.createStatement()){
			double deductions = salary * 0.2;
			double taxablePay = salary - deductions;
			double tax = taxablePay * 0.1;
			double netPay = salary - tax;
			String sql = String.format("INSERT INTO payroll_details (employee_id, basic_pay, deductions,taxable_pay, tax, net_pay) VALUES"+
						 "( '%s', '%s', '%s', '%s', '%s', '%s')", id, salary, deductions,taxablePay, tax, netPay);
			int rowAffected = statement.executeUpdate(sql);
			if(rowAffected == 1) {
			data = new EmployeePayroll(id, name, salary, start);
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return data;
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
