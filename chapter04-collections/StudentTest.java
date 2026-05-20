import java.util.*;

public class StudentTest {
    public static void main(String[] args){
        ArrayList<Student> students = new ArrayList<>();

        try{
            Student.addStudent(students, new Student(1, "Alice", 85.5));
            Student.addStudent(students, new Student(2, "Bob", 79.0));
            Student.addStudent(students, new Student(3, "Maria", 98.5));
            Student.addStudent(students, new Student(4, "Li hong", 87.9));
            Student.addStudent(students, new Student(5, "Liu Qiang", 23));
            Student.addStudent(students, new Student(6, "Zhang San", 45));
            Student.addStudent(students, new Student(7, "Bai Lu", 67));
            Student.addStudent(students, new Student(8, "Li Ming", 79));
            Student.addStudent(students, new Student(9, "Xiao Hong", 45));
            Student.addStudent(students, new Student(10, "Zhao Wei", 78));
            Student.addStudent(students, new Student(11, "Jiao Yu", 90));
        } catch(IllegalArgumentException ex){
            System.out.println(ex);
        }

        Student.sortByScore(students);
        System.out.println("Students sorted by score:");
        for(Student student: students){
            System.out.println(student.name + ": " + student.score);
        }
        System.out.println();

        Student highestStudent = Student.findsHighestScore(students);
        if(highestStudent != null){
            System.out.println("Student with the highest score: " + highestStudent.name + " - " + highestStudent.score);
        }
        Student lowestStudent = Student.findsLowestScore(students);
        if(lowestStudent != null){
            System.out.println("Student with the lowest score: " + lowestStudent.name + " - " + lowestStudent.score);
        }

        double averageScore = Student.calculateAverageScore(students);
        System.out.printf("Average score : %.2f\n", averageScore);

        double minScore = 80.0;
        double maxScore = 90.0;
        ArrayList<Student> studentsInRange = Student.getStudentsByScoreRange(students, minScore, maxScore);
        System.out.println("Students with scores between " + minScore + " and " + maxScore + ":");
        for(Student student: studentsInRange){
            System.out.println(student.name + ": " + student.score);
        }
    }

    public static class Student{
        private int id;
        private String name;
        private double score;

        public Student (int id, String name, double score){
            this.id = id;
            this.name = name;
            this.score = score;
        }

        public static void addStudent(ArrayList<Student> students, Student student) throws IllegalArgumentException{
            if(student == null){
                throw new IllegalArgumentException("Student cannot be null");
            }
            for(Student s: students){
                if(s.id == student.id){
                    throw new IllegalArgumentException("Student with the same ID already exists.");
                }
            }
            students.add(student);
        }

        public static void sortByScore(ArrayList<Student> students){
            Collections.sort(students, new Comparator<Student>() {
                @Override
                public int compare(Student s1, Student s2) {
                    return Double.compare(s2.score, s1.score);
                }
            });
        }

        public static Student findsHighestScore (ArrayList<Student> students){
            if(students.isEmpty()){
                return null;
            }
            Student highestStudent = students.get(0);
            for(Student student: students){
                if(student.score > highestStudent.score){
                    highestStudent = student;
                }
            }
            return highestStudent;
        }

        public static Student findsLowestScore (ArrayList<Student> students){
            if(students.isEmpty()){
                return null;
            }
            Student lowestStudent = students.get(0);
            for(Student student: students){
                if(student.score < lowestStudent.score){
                    lowestStudent = student;
                }
            }
            return lowestStudent;
        }

        public static double calculateAverageScore (ArrayList<Student> students){
            if(students.isEmpty()){
                return 0.0;
            }
            double totalScore = 0.0;
            for(Student student: students){
                totalScore += student.score;
            }
            return totalScore / students.size();
        }

        public static ArrayList<Student> getStudentsByScoreRange (ArrayList<Student> students, double min, double max){
            ArrayList<Student> studentList = new ArrayList<>();
            for(Student student: students){
                if(student.score >= min && student.score <= max){
                    studentList.add(student);
                }
            }
            return studentList;
        }
    }
}
