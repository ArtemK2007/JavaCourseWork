package ua.sumdu.oop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ua.sumdu.oop.dao.DatabaseConnection;
import ua.sumdu.oop.dao.DepartmentDao;
import ua.sumdu.oop.dao.EmployeeDao;
import ua.sumdu.oop.model.CompanyBackup;
import ua.sumdu.oop.model.Department;
import ua.sumdu.oop.model.Employee;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;

public class JsonService {
    private final ObjectMapper mapper;
    private final DepartmentDao departmentDao;
    private final EmployeeDao employeeDao;

    public JsonService(DepartmentDao departmentDao, EmployeeDao employeeDao) {
        this.departmentDao = departmentDao;
        this.employeeDao = employeeDao;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void exportToFile(File file) throws SQLException, IOException {
        List<Department> departments = departmentDao.findAll();
        List<Employee> employees = employeeDao.findAll();
        CompanyBackup backup = new CompanyBackup(departments, employees);
        mapper.writeValue(file, backup);
    }

    public void importFromFile(File file) throws IOException, SQLException {
        CompanyBackup backup = mapper.readValue(file, CompanyBackup.class);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (backup.getDepartments() != null) {
                    String deptSql = """
                            INSERT INTO departments (id, name, location)
                            VALUES (?, ?, ?)
                            ON CONFLICT (id) DO UPDATE
                            SET name = EXCLUDED.name, location = EXCLUDED.location
                            """;
                    try (PreparedStatement ps = conn.prepareStatement(deptSql)) {
                        for (Department d : backup.getDepartments()) {
                            ps.setInt(1, d.getId());
                            ps.setString(2, d.getName());
                            ps.setString(3, d.getLocation());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                if (backup.getEmployees() != null) {
                    String empSql = """
                            INSERT INTO employees (id, full_name, position, salary, hire_date, department_id, manager_id)
                            VALUES (?, ?, ?, ?, ?, ?, NULL)
                            ON CONFLICT (id) DO UPDATE
                            SET full_name = EXCLUDED.full_name,
                                position = EXCLUDED.position,
                                salary = EXCLUDED.salary,
                                hire_date = EXCLUDED.hire_date,
                                department_id = EXCLUDED.department_id
                            """;
                    try (PreparedStatement ps = conn.prepareStatement(empSql)) {
                        for (Employee e : backup.getEmployees()) {
                            ps.setInt(1, e.getId());
                            ps.setString(2, e.getFullName());
                            ps.setString(3, e.getPosition());
                            ps.setBigDecimal(4, e.getSalary());
                            ps.setDate(5, Date.valueOf(e.getHireDate()));
                            ps.setInt(6, e.getDepartmentId());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }

                    String mgrSql = "UPDATE employees SET manager_id = ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(mgrSql)) {
                        for (Employee e : backup.getEmployees()) {
                            if (e.getManagerId() == null) {
                                ps.setNull(1, Types.INTEGER);
                            } else {
                                ps.setInt(1, e.getManagerId());
                            }
                            ps.setInt(2, e.getId());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }

                try (Statement st = conn.createStatement()) {
                    st.execute("SELECT setval('departments_id_seq', COALESCE((SELECT MAX(id) FROM departments), 1))");
                    st.execute("SELECT setval('employees_id_seq', COALESCE((SELECT MAX(id) FROM employees), 1))");
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}