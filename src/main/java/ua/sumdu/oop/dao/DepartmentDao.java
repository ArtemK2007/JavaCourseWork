package ua.sumdu.oop.dao;

import ua.sumdu.oop.model.Department;
import ua.sumdu.oop.model.DepartmentStats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {

    public List<Department> findAll() throws SQLException {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT id, name, location FROM departments ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Department(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("location")
                ));
            }
        }
        return list;
    }

    public void create(Department dept) throws SQLException {
        String sql = "INSERT INTO departments (name, location) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getLocation());
            ps.executeUpdate();
        }
    }

    public void update(Department dept) throws SQLException {
        String sql = "UPDATE departments SET name = ?, location = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dept.getName());
            ps.setString(2, dept.getLocation());
            ps.setInt(3, dept.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM departments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Завдання на 85 балів (п. 2): Підрозділ з найбільшою середньою ЗП та його показники
    public DepartmentStats findTopDepartmentByAvgSalary() throws SQLException {
        String sql = """
                SELECT d.id, d.name, d.location,
                       COUNT(e.id) AS emp_count,
                       ROUND(AVG(e.salary), 2) AS avg_sal,
                       MIN(e.salary) AS min_sal,
                       MAX(e.salary) AS max_sal,
                       SUM(e.salary) AS total_payroll
                FROM departments d
                JOIN employees e ON d.id = e.department_id
                GROUP BY d.id, d.name, d.location
                ORDER BY avg_sal DESC
                LIMIT 1
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new DepartmentStats(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getInt("emp_count"),
                        rs.getBigDecimal("avg_sal"),
                        rs.getBigDecimal("min_sal"),
                        rs.getBigDecimal("max_sal"),
                        rs.getBigDecimal("total_payroll")
                );
            }
        }
        return null;
    }
}