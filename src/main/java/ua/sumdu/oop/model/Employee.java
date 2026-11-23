package ua.sumdu.oop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    private int id;
    private String fullName;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private int departmentId;
    private Integer managerId;

    @JsonIgnore
    private String departmentName;
    @JsonIgnore
    private String managerName;

    public Employee() {
    }

    public Employee(int id, String fullName, String position, BigDecimal salary,
                    LocalDate hireDate, int departmentId, Integer managerId) {
        this.id = id;
        this.fullName = fullName;
        this.position = position;
        this.salary = salary;
        this.hireDate = hireDate;
        this.departmentId = departmentId;
        this.managerId = managerId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getManagerName() {
        return managerName != null ? managerName : "— (Немає)";
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    @Override
    public String toString() {
        return fullName + " (" + position + ")";
    }
}