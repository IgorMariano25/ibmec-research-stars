package br.com.ibmec.researchstars.report.dto;

public class CourseComplianceDto {

    private Long courseId;
    private String courseName;
    private String courseCode;
    private long totalApprovedProfessors;
    private long totalCompliantProfessors;
    private double compliancePercentage;

    public CourseComplianceDto() {
    }

    public CourseComplianceDto(Long courseId, String courseName, String courseCode, long totalApprovedProfessors, long totalCompliantProfessors, double compliancePercentage) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.totalApprovedProfessors = totalApprovedProfessors;
        this.totalCompliantProfessors = totalCompliantProfessors;
        this.compliancePercentage = compliancePercentage;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public long getTotalApprovedProfessors() {
        return totalApprovedProfessors;
    }

    public void setTotalApprovedProfessors(long totalApprovedProfessors) {
        this.totalApprovedProfessors = totalApprovedProfessors;
    }

    public long getTotalCompliantProfessors() {
        return totalCompliantProfessors;
    }

    public void setTotalCompliantProfessors(long totalCompliantProfessors) {
        this.totalCompliantProfessors = totalCompliantProfessors;
    }

    public double getCompliancePercentage() {
        return compliancePercentage;
    }

    public void setCompliancePercentage(double compliancePercentage) {
        this.compliancePercentage = compliancePercentage;
    }
}
