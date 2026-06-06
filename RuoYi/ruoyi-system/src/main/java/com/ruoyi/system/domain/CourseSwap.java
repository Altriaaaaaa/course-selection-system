package com.ruoyi.system.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CourseSwap
{
    private Integer swapId;
    private String fromTno;
    private String fromCno;
    private String toTno;
    private String toCno;
    private String swapWeek;
    private String reason;
    private Integer status;
    private Date createdAt;
    private Date updatedAt;
    // 非数据库字段，用于展示教师姓名和课程名称
    private String fromTname;
    private String fromCname;
    private String toTname;
    private String toCname;

    public Integer getSwapId() { return swapId; }
    public void setSwapId(Integer swapId) { this.swapId = swapId; }
    public String getFromTno() { return fromTno; }
    public void setFromTno(String fromTno) { this.fromTno = fromTno; }
    public String getFromCno() { return fromCno; }
    public void setFromCno(String fromCno) { this.fromCno = fromCno; }
    public String getToTno() { return toTno; }
    public void setToTno(String toTno) { this.toTno = toTno; }
    public String getToCno() { return toCno; }
    public void setToCno(String toCno) { this.toCno = toCno; }
    public String getSwapWeek() { return swapWeek; }
    public void setSwapWeek(String swapWeek) { this.swapWeek = swapWeek; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    public String getFromTname() { return fromTname; }
    public void setFromTname(String fromTname) { this.fromTname = fromTname; }
    public String getFromCname() { return fromCname; }
    public void setFromCname(String fromCname) { this.fromCname = fromCname; }
    public String getToTname() { return toTname; }
    public void setToTname(String toTname) { this.toTname = toTname; }
    public String getToCname() { return toCname; }
    public void setToCname(String toCname) { this.toCname = toCname; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("swapId", getSwapId())
            .append("fromTno", getFromTno())
            .append("fromCno", getFromCno())
            .append("toTno", getToTno())
            .append("toCno", getToCno())
            .append("swapWeek", getSwapWeek())
            .append("reason", getReason())
            .append("status", getStatus())
            .toString();
    }
}
