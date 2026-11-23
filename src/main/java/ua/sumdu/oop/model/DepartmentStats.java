package ua.sumdu.oop.model;

import java.math.BigDecimal;

public class DepartmentStats {
    private final int departmentId;
    private final String departmentName;
    private final String location;
    private final int employeeCount;
    private final BigDecimal avgSalary;
    private final BigDecimal minSalary;
    private final BigDecimal maxSalary;
    private final BigDecimal totalPayroll;

    public DepartmentStats(int departmentId, String departmentName, String location,
                           int employeeCount, BigDecimal avgSalary, BigDecimal minSalary,
                           BigDecimal maxSalary, BigDecimal totalPayroll) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.location = location;
        this.employeeCount = employeeCount;
        this.avgSalary = avgSalary;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.totalPayroll = totalPayroll;
    }

    public int getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getLocation() { return location; }
    public int getEmployeeCount() { return employeeCount; }
    public BigDecimal getAvgSalary() { return avgSalary; }
    public BigDecimal getMinSalary() { return minSalary; }
    public BigDecimal getMaxSalary() { return maxSalary; }
    public BigDecimal getTotalPayroll() { return totalPayroll; }
}