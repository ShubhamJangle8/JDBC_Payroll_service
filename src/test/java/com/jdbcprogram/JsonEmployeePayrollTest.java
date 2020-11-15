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

	@Test
	public void givenNewEmployee_WhenAdded_ShouldMatch201ResponseAndCount() {
		EmployeePayroll[] arrayOfEmp = getEmployeeList();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmp));
		EmployeePayroll employee = new EmployeePayroll(6, "Ratan Tata", "M", 9000000.0, LocalDate.now());
		long count = employeePayrollService.countEntries(IOService.REST_IO);
		Assert.assertEquals(6, count);
	}
}
