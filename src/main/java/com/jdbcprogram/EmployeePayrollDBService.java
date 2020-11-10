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

	private List<EmployeePayroll> getEmployeeDataFromResultSet(ResultSet resultSet) {
		List<EmployeePayroll> empPayrollList = new ArrayList<>();
		try {
			while(resultSet.next()) {
				int id = resultSet.getInt("emp_id");
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
	
	private List<EmployeePayroll> getEmployeePayrollDataUsingDB(String sql) {
		List<EmployeePayroll> list = new ArrayList<>();
		try(Connection connection  = this.getConnection()){
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			list = this.getEmployeeDataFromResultSet(result);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Reading the Employee Payroll Data from the database
	 * @param name
	 * @return
	 */
	public List<EmployeePayroll> readData() {
		String sql = "Select * from Employee_Payroll";
		return this.getEmployeePayrollDataUsingDB(sql);
	}
	
	public List<EmployeePayroll> getEmployeePayrollData(String name) {
		List<EmployeePayroll> employeePayrollList = null;
		try {
			if (this.empPayrollDataStatement == null) {
				this.prepareStatementForEmployeeData();
			}
			empPayrollDataStatement.setString(1, name);
			ResultSet resultSet = empPayrollDataStatement.executeQuery();
			employeePayrollList = this.getEmployeeDataFromResultSet(resultSet);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return employeePayrollList;
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
	 * adding to payroll_details table
	 * @param salary
	 * @throws payrollServiceDBException 
	 */
	private void addToPayrollDetails(Connection connection, int employeeId, double salary) throws payrollServiceDBException {
		try (Statement statement = (Statement) connection.createStatement()) { 
			double deductions = salary * 0.2;
			double taxable_pay = salary - deductions;
			double tax = taxable_pay * 0.1;
			double netPay = salary - tax;
			String sql = String.format(
					"insert into payroll_details (employeeId, basic_pay, deductions, taxable_pay, tax, net_pay) "
							+ "VALUES ('%s','%s','%s','%s','%s','%s')",
					employeeId, salary, deductions, taxable_pay, tax, netPay);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException exception) {
				throw new payrollServiceDBException(exception.getMessage());
			}
			throw new payrollServiceDBException("Unable to add to database");
		}
	}
	
	/**
	 * adds employee details to database
	 * @param name
	 * @param gender
	 * @param salary
	 * @param date
	 * @return
	 * @throws payrollServiceDBException
	 * @throws SQLException
	 */
	@SuppressWarnings("static-access")
	public EmployeePayroll addEmployeeToPayroll(String name, String gender, double salary, LocalDate date)
			throws payrollServiceDBException, SQLException {
		int employeeId = -1;
		Connection connection = null;
		EmployeePayroll employee = null;
		connection = this.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (SQLException exception) {
			throw new payrollServiceDBException(exception.getMessage());
		}
		
		try (Statement statement = (Statement) connection.createStatement()) { // adding to employee_payroll table
			String sql = String.format(
					"insert into employee_payroll (name, gender, salary, start) values ('%s', '%s', '%s', '%s')", name,
					gender, salary, Date.valueOf(date));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			employee = new EmployeePayroll(employeeId, name, gender, salary, date);
		} catch (SQLException exception) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw new payrollServiceDBException(e.getMessage());
			}
			throw new payrollServiceDBException("Unable to add to database");
		}
		
		this.addToPayrollDetails(connection,employeeId, salary);			// adding to payroll_details table
		
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new payrollServiceDBException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return employee;
	}
	
	/**
	 * deletes employee record in cascade from both tables of database
	 * 
	 * @param id
	 * @throws payrollServiceDBException
	 */
	public int deleteEmployeeFromPayroll(int id) {
		String sql = String.format("delete from employee_payroll where emp_id = ?;", id);
		Connection connection;
		try {
			connection = this.getConnection();
			PreparedStatement prepareStatement = (PreparedStatement) connection.prepareStatement(sql);
			prepareStatement.setDouble(1, id);
			int updateCount = prepareStatement.executeUpdate();
			return updateCount;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
		
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
