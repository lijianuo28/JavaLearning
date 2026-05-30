package com.example.jdbc;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface StudentMapper {

    @Insert("INSERT INTO student (id, name, score) VALUES (#{id}, #{name}, #{score})")
    void addStudent(Student student);

    @Select("SELECT * FROM student ORDER BY score, id")
    List<Student> getAllStudents();

    @Select("SELECT * FROM student WHERE score = (SELECT MAX(score) FROM student) ORDER BY id")
    List<Student> getMaxStudent();

    @Select("SELECT * FROM student WHERE score = (SELECT MIN(score) FROM student) ORDER BY id")
    List<Student> getMinStudent();

    @Select("SELECT AVG(score) FROM student")
    double getAverageScore();

    @Delete("DELETE FROM student WHERE id = #{id}")
    void deleteStudent(int id);

    @Update("UPDATE student SET score = #{score} WHERE id = #{id}")
    void updateStudentScore(@Param("id") int id, @Param("score") double newScore);
}