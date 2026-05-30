package com.example.jdbc;

import java.util.List;

public class Student01 {
    public static void main(String[] args) {
        // Ê¹ÓÃ try-with-resources ×Ô¶¯¹Ø±Õ SqlSession
        try (var session = MyBatisUtil.getSqlSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);

            // Ìí¼ÓÑ§Éú
            mapper.addStudent(new Student(1, "Alice", 85.5));
            mapper.addStudent(new Student(2, "Bob", 92.0));
            mapper.addStudent(new Student(3, "Charlie", 78.0));

            // ²éÑ¯ËùÓÐ
            List<Student> students = mapper.getAllStudents();
            System.out.println("All Students:");
            students.forEach(s -> System.out.println(s.getId() + ": " + s.getName() + " - " + s.getScore()));

            // ×î¸ß·Ö
            List<Student> maxStudents = mapper.getMaxStudent();
            System.out.println("\nMax Score Students:");
            maxStudents.forEach(s -> System.out.println(s.getId() + ": " + s.getName() + " - " + s.getScore()));

            // ×îµÍ·Ö
            List<Student> minStudents = mapper.getMinStudent();
            System.out.println("\nMin Score Students:");
            minStudents.forEach(s -> System.out.println(s.getId() + ": " + s.getName() + " - " + s.getScore()));

            // Æ½¾ù·Ö
            double avg = mapper.getAverageScore();
            System.out.printf("\nAverage Score: %.2f\n", avg);

            // ¸üÐÂ·ÖÊý
            mapper.updateStudentScore(1, 90.0);
            System.out.println("\nUpdated Alice's score to 90.0");

            // É¾³ý Charlie
            mapper.deleteStudent(3);
            System.out.println("Deleted Charlie from the database");

            // ÔÙ´Î²é¿´ËùÓÐÑ§Éú
            System.out.println("\nAll Students after update & delete:");
            mapper.getAllStudents().forEach(s -> 
                System.out.println(s.getId() + ": " + s.getName() + " - " + s.getScore())
            );
        }
    }
}