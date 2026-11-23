package ua.sumdu.oop.model;

import java.util.List;

public class CompanyBackup {
    private List<Department> departments;
    private List<Employee> employees;

    public CompanyBackup() {
    }

    public CompanyBackup(List<Department> departments, List<Employee> employees) {
        this.departments = departments;
        this.employees = employees;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
}