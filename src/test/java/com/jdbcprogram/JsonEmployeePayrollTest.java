package com.jdbcprogram;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.jdbcprogram.EmployeePayrollService.IOService;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class JsonEmployeePayrollTest {

	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	/**
	 * retrieving data from json server
	 * @return
	 */
	private EmployeePayroll[] getEmployeeList() {
		Response response = RestAssured.get("/employees");
		System.out.println("Employee payroll entries in JSONServer:\n" + response.asString());
		EmployeePayroll[] arrayOfEmp = new Gson().fromJson(response.asString(), EmployeePayroll[].class);
		return arrayOfEmp;
	}
	
	private Response addEmployeeToJsonServer(EmployeePayroll employeePayrollData) {
		String empJson = new Gson().toJson(employeePayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employee");
	}

	@Test
	public void givenEmployeeDataInJsonServer_WhenRetrieved_shouldMatchtheCount() {
		EmployeePayroll[] arrayOfEmp = getEmployeeList();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmp));
		EmployeePayroll employee = new EmployeePayroll(6, "Ratan Tata", "M", 9000000.0, LocalDate.now());
		long count = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(6, count);
	}
	
	@Test
	public void givenNewEmployeeData_WhenAdded_ShouldMatch201ResponseAndCount() {
		EmployeePayroll[] arrayOfEmp = getEmployeeList();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmp));
		EmployeePayroll employeePayrollData = new EmployeePayroll(0, "Mark", "M", 300000.0,LocalDate.now());
		Response response = addEmployeeToJsonServer(employeePayrollData);
		int statusCode = response.getStatusCode();
		Assert.assertEquals(404, statusCode);
		employeePayrollData = new Gson().fromJson(response.asString(), EmployeePayroll.class);
		employeePayrollService.addEmployeeToPayroll(employeePayrollData, IOService.REST_IO);
		long entries = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(7, entries);
	}

}
