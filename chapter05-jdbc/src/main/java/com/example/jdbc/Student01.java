package com.example.jdbc;

import java.util.List;

public class Student01 {
    public static void main(String[] args) {
        StudentDBManager dbManager = new StudentDBManager();

        dbManager.addStudent(1, "Alice", 85.5);
        dbManager.addStudent(2, "Bob", 92.0);
        dbManager.addStudent(3, "Charlie", 78.0);

        List<Student> students = dbManager.getAllStudents();
        System.out.println("All Students:");
        for (Student student : students) {
            System.out.println(student.getId() + ": " + student.getName() + " - " + student.getScore());
        }

        List<Student> maxStudents = dbManager.getMaxStudent();
        System.out.println("\nMax Score Students:");
        for (Student student : maxStudents) {
            System.out.println(student.getId() + ": " + student.getName() + " - " + student.getScore());
        }

        List<Student> minStudents = dbManager.getMinStudent();
        System.out.println("\nMin Score Students:");
        for (Student student : minStudents) {
            System.out.println(student.getId() + ": " + student.getName() + " - " + student.getScore());
        }

        double averageScore = dbManager.getAverageScore();
        System.out.println("\nAverage Score: " + averageScore);

        dbManager.updateStudentScore(1, 90.0);
        System.out.println("\nUpdated Alice's score to 90.0");

        dbManager.deleteStudent(3);
        System.out.println("\nDeleted Charlie from the database");
    }
}