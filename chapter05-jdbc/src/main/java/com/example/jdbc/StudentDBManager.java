package com.example.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDBManager {
    // 数据库连接信息
    private static final String URL = "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Abcd13460761926@";

    // 添加学生
    public void addStudent(int id, String name, double score) {
        String sql = "INSERT INTO student (id, name, score) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setDouble(3, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 查询所有学生
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM student ORDER BY score, id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double score = rs.getDouble("score");
                students.add(new Student(id, name, score));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // 获取最高分学生（可能有并列）
    public List<Student> getMaxStudent() {
        List<Student> maxStudents = new ArrayList<>();
        String sql = "SELECT * FROM student WHERE score = (SELECT MAX(score) FROM student) ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {   // 注意：可能有多个，改成 while
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double score = rs.getDouble("score");
                maxStudents.add(new Student(id, name, score));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxStudents;
    }

    // 获取最低分学生（可能有并列）
    public List<Student> getMinStudent() {
        List<Student> minStudents = new ArrayList<>();
        String sql = "SELECT * FROM student WHERE score = (SELECT MIN(score) FROM student) ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {   // 改成 while
                int id = rs.getInt("id");
                String name = rs.getString("name");
                double score = rs.getDouble("score");
                minStudents.add(new Student(id, name, score));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return minStudents;
    }

    // 获取平均分
    public double getAverageScore() {
        double averageScore = 0.0;
        String sql = "SELECT AVG(score) AS average FROM student";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                averageScore = rs.getDouble("average");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return averageScore;
    }

    // 删除学生
    public void deleteStudent(int id) {
        String sql = "DELETE FROM student WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 修改学生分数
    public void updateStudentScore(int id, double newScore) {
        String sql = "UPDATE student SET score = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newScore);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}