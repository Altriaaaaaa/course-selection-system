package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Course
{
    private String cno;
    private String cname;
    private BigDecimal credit;
    private Integer hours;
    private Integer courseType;
    private Integer deptId;
    private String adminId;
    private String deptName;
    private Integer maxStudents;
    private Integer enrolledCount;
    private Integer status;
    private String rejectReason;
    private String teacherName;
    private BigDecimal creditMin;
    private BigDecimal creditMax;

    public String getCno() { return cno; }
    public void setCno(String cno) { this.cno = cno; }

    public String getCname() { return cname; }
    public void setCname(String cname) { this.cname = cname; }

    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }

    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }

    public Integer getCourseType() { return courseType; }
    public void setCourseType(Integer courseType) { this.courseType = courseType; }

    public Integer getDeptId() { return deptId; }
    public void setDeptId(Integer deptId) { this.deptId = deptId; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }

    public Integer getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(Integer enrolledCount) { this.enrolledCount = enrolledCount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public BigDecimal getCreditMin() { return creditMin; }
    public void setCreditMin(BigDecimal creditMin) { this.creditMin = creditMin; }

    public BigDecimal getCreditMax() { return creditMax; }
    public void setCreditMax(BigDecimal creditMax) { this.creditMax = creditMax; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("cno", getCno())
            .append("cname", getCname())
            .append("credit", getCredit())
            .append("hours", getHours())
            .append("courseType", getCourseType())
            .append("deptId", getDeptId())
            .append("adminId", getAdminId())
            .append("maxStudents", getMaxStudents())
            .append("enrolledCount", getEnrolledCount())
            .toString();
    }
}