package com.ruoyi.web.controller.admin;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.service.ICourseSelectionService;
import com.ruoyi.system.service.ICourseService;

@Controller
@RequestMapping("/admin/statistics")
@RequiresRoles("admin")
public class StatisticsController extends BaseController
{
    @Autowired
    private ICourseSelectionService courseSelectionService;
    @Autowired
    private ICourseService courseService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping()
    public String index() { return "admin/statistics/index"; }

    @RequestMapping("/enrollment")
    @ResponseBody
    public TableDataInfo enrollment()
    {
        List<Map<String, Object>> list = courseSelectionService.selectCourseEnrollmentStats();
        return getDataTable(list);
    }

    @RequestMapping("/fail")
    @ResponseBody
    public TableDataInfo fail()
    {
        List<Map<String, Object>> list = courseSelectionService.selectFailStudentStats();
        return getDataTable(list);
    }

    @RequestMapping("/gpa")
    @ResponseBody
    public AjaxResult gpa(String sno)
    {
        try {
            java.math.BigDecimal gpa = courseSelectionService.getStudentGpa(sno);
            return success().put("gpa", gpa);
        } catch (Exception e) {
            com.ruoyi.system.domain.CourseSelection cs = new com.ruoyi.system.domain.CourseSelection();
            cs.setSno(sno);
            java.util.List<com.ruoyi.system.domain.CourseSelection> list = courseSelectionService.selectCourseSelectionList(cs);
            double totalPoints = 0;
            double totalCredits = 0;
            for (com.ruoyi.system.domain.CourseSelection item : list) {
                if (item.getNormalScore() != null && item.getTestScore() != null) {
                    double total = item.getNormalScore().doubleValue() * 0.4 + item.getTestScore().doubleValue() * 0.6;
                    double gp = 0;
                    if (total >= 90) gp = 4.0;
                    else if (total >= 85) gp = 3.7;
                    else if (total >= 82) gp = 3.3;
                    else if (total >= 78) gp = 3.0;
                    else if (total >= 75) gp = 2.7;
                    else if (total >= 72) gp = 2.3;
                    else if (total >= 68) gp = 2.0;
                    else if (total >= 64) gp = 1.5;
                    else if (total >= 60) gp = 1.0;
                    String cno = item.getCno();
                    if (cno != null) {
                        com.ruoyi.system.domain.Course course = courseService.selectCourseById(cno);
                        if (course != null && course.getCredit() != null) {
                            totalPoints += gp * course.getCredit().doubleValue();
                            totalCredits += course.getCredit().doubleValue();
                        }
                    }
                }
            }
            double gpa = totalCredits > 0 ? totalPoints / totalCredits : 0;
            return success().put("gpa", Math.round(gpa * 100.0) / 100.0);
        }
    }

    @RequestMapping("/grades")
    @ResponseBody
    public TableDataInfo grades(String sno)
    {
        List<Map<String, Object>> list = courseSelectionService.selectStudentGradeStats(sno);
        return getDataTable(list);
    }

    /** 选课概览 - 总览统计数据 */
    @RequestMapping("/overview")
    @ResponseBody
    public AjaxResult overview()
    {
        try {
            Map<String, Object> result = new java.util.HashMap<>();
            // 总课程数
            Integer totalCourses = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM course", Integer.class);
            result.put("totalCourses", totalCourses != null ? totalCourses : 0);
            // 总学生数
            Integer totalStudents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM student", Integer.class);
            result.put("totalStudents", totalStudents != null ? totalStudents : 0);
            // 总选课人次
            Integer totalEnrollments = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM courseselection", Integer.class);
            result.put("totalEnrollments", totalEnrollments != null ? totalEnrollments : 0);
            // 平均GPA - 计算所有有成绩学生的平均绩点
            Double avgGpa = jdbcTemplate.queryForObject(
                "SELECT AVG(gpa) FROM (SELECT cs.sno, " +
                "SUM(CASE WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 90 THEN 4.0 " +
                "WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 85 THEN 3.7 WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 82 THEN 3.3 " +
                "WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 78 THEN 3.0 WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 75 THEN 2.7 " +
                "WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 72 THEN 2.3 WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 68 THEN 2.0 " +
                "WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 64 THEN 1.5 WHEN (cs.normal_score*0.4+cs.test_score*0.6) >= 60 THEN 1.0 " +
                "ELSE 0 END) * c.credit / NULLIF(SUM(c.credit), 0) AS gpa " +
                "FROM courseselection cs JOIN course c ON cs.cno = c.cno " +
                "WHERE cs.normal_score IS NOT NULL AND cs.test_score IS NOT NULL AND (cs.normal_score*0.4+cs.test_score*0.6) >= 60 " +
                "GROUP BY cs.sno, c.cno, c.credit) t", Double.class);
            result.put("avgGpa", avgGpa != null ? Math.round(avgGpa * 100.0) / 100.0 : 0);
            // 各课程选课人数列表
            List<Map<String, Object>> courseEnrollList = jdbcTemplate.queryForList(
                "SELECT c.cname, COUNT(*) AS enrolledCount FROM courseselection cs " +
                "JOIN course c ON cs.cno = c.cno GROUP BY cs.cno, c.cname ORDER BY enrolledCount DESC");
            result.put("courseEnrollList", courseEnrollList);
            return success(result);
        } catch (Exception e) {
            logger.error("获取选课概览数据失败", e);
            return error("获取选课概览数据失败");
        }
    }

    /** 安全概览 - 今日登录失败统计 */
    @RequestMapping("/security/todayFails")
    @ResponseBody
    public AjaxResult todayFails()
    {
        try {
            String today = DateUtils.dateTimeNow("yyyy-MM-dd");
            String sql = "SELECT COUNT(*) AS cnt FROM sys_logininfor WHERE status = 1 AND login_time LIKE ?";
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, today + "%");
            return success().put("count", result.get("cnt"));
        } catch (Exception e) { return success().put("count", 0); }
    }

    /** 安全概览 - 最近安全事件 */
    @RequestMapping("/security/recentEvents")
    @ResponseBody
    public TableDataInfo recentEvents()
    {
        String sql = "SELECT info_id, login_name AS userName, ipaddr, login_time AS loginTime, status, msg " +
                     "FROM sys_logininfor ORDER BY info_id DESC LIMIT 20";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return getDataTable(list);
    }
}
