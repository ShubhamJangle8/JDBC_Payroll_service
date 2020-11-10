package com.jdbcprogram;

import java.time.LocalDate;
import java.util.List;

public class EmployeePayroll {
	public int id;
	public String name;
	public String gender;
	public double salary;
	public LocalDate date;
	public List<String> departments;

	public EmployeePayroll(int id, String name, double salary) {
		super();
		this.id = id;
		this.name = name;
		this.salary = salary;
	}
	
	public EmployeePayroll(int id, String name, double salary, LocalDate date) {
		this(id, name, salary);
		this.date = date;
	}
	
	public EmployeePayroll(int id, String name, String gender, double salary, LocalDate startDate) {
		this(id, name, salary, startDate);
		this.gender = gender;
	}
	
	public EmployeePayroll(int id, String name, String gender, double salary, LocalDate startDate,
			List<String> departments) {
		this(id, name, gender, salary, startDate);
		this.departments = departments;
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		EmployeePayroll that = (EmployeePayroll) o;
		return id == that.id && Double.compare(that.salary, salary) == 0 && name.equals(that.name);
	}

	@Override
	public String toString() {
		return "id = " + id + ", name = " + name + ", Gender = " + gender + ", Salary = " + salary + ", Start Date = "
				+ date + ", Departments : " + departments;
	}

}
