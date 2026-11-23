package ua.sumdu.oop.dao;

import ua.sumdu.oop.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao {

    private static final String BASE_SELECT = """
            SELECT e.id, e.full_name, e.position, e.salary, e.hire_date,
                   e.department_id, e.manager_id,
                   d.name AS dept_name,
                   m.full_name AS manager_name
            FROM employees e
            JOIN departments d ON e.department_id = d.id
            LEFT JOIN employees m ON e.manager_id = m.id
            """;

    public List<Employee> findAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY e.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Завдання на 75 балів: Працівники із ЗП вищою за середню по їхньому відділу + сортування
    public List<Employee> findAboveDepartmentAverage(boolean ascending) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String order = ascending ? "ASC" : "DESC";
        String sql = BASE_SELECT + """
                WHERE e.salary > (
                    SELECT AVG(e2.salary)
                    FROM employees e2
                    WHERE e2.department_id = e.department_id
                )
                ORDER BY e.salary """ + " " + order;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Завдання на 85 балів (п. 1): Зміна безпосереднього керівника вибраного працівника
    public void updateManager(int employeeId, Integer newManagerId) throws SQLException {
        String sql = "UPDATE employees SET manager_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (newManagerId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, newManagerId);
            }
            ps.setInt(2, employeeId);
            ps.executeUpdate();
        }
    }

    public void create(Employee emp) throws SQLException {
        String sql = """
                INSERT INTO employees (full_name, position, salary, hire_date, department_id, manager_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getPosition());
            ps.setBigDecimal(3, emp.getSalary());
            ps.setDate(4, Date.valueOf(emp.getHireDate()));
            ps.setInt(5, emp.getDepartmentId());
            if (emp.getManagerId() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, emp.getManagerId());
            }
            ps.executeUpdate();
        }
    }

    public void update(Employee emp) throws SQLException {
        String sql = """
                UPDATE employees
                SET full_name = ?, position = ?, salary = ?, hire_date = ?, department_id = ?, manager_id = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getPosition());
            ps.setBigDecimal(3, emp.getSalary());
            ps.setDate(4, Date.valueOf(emp.getHireDate()));
            ps.setInt(5, emp.getDepartmentId());
            if (emp.getManagerId() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, emp.getManagerId());
            }
            ps.setInt(7, emp.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        int managerIdVal = rs.getInt("manager_id");
        Integer managerId = rs.wasNull() ? null : managerIdVal;

        Employee emp = new Employee(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("position"),
                rs.getBigDecimal("salary"),
                rs.getDate("hire_date").toLocalDate(),
                rs.getInt("department_id"),
                managerId
        );
        emp.setDepartmentName(rs.getString("dept_name"));
        emp.setManagerName(rs.getString("manager_name"));
        return emp;
    }
}