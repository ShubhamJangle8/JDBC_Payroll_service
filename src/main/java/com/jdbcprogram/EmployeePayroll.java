package com.jdbcprogram;

import java.time.LocalDate;

public class EmployeePayroll {
	public int id;
	public String name;
	public double salary;
	public LocalDate date;

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmployeePayroll other = (EmployeePayroll) obj;
		return id == other.id && Double.compare(other.salary, salary) == 0 && name.equals(other.name);
	}

	@Override
	public String toString() {
		return "EmployeePayroll [id=" + id + ", name=" + name + ", salary=" + salary + "]";
	}

}
