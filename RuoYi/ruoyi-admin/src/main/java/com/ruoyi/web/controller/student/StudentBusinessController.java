package com.ruoyi.web.controller.student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.Class;
import com.ruoyi.system.domain.Course;
import com.ruoyi.system.domain.CourseSelection;
import com.ruoyi.system.domain.Student;
import com.ruoyi.system.service.IClassService;
import com.ruoyi.system.service.ICourseService;
import com.ruoyi.system.service.ICourseSelectionService;
import com.ruoyi.system.service.IStudentService;

@Controller
@RequestMapping("/student")
@RequiresRoles("student")
public class StudentBusinessController extends BaseController
{
    @Autowired
    private ICourseService courseService;
    @Autowired
    private ICourseSelectionService courseSelectionService;
    @Autowired
    private IStudentService studentService;
    @Autowired
    private IClassService classService;

    @GetMapping("/index")
    public String index(ModelMap mmap)
    {
        mmap.put("sno", getLoginName());
        return "student/index";
    }

    @GetMapping("/course")
    public String course(ModelMap mmap)
    {
        mmap.put("sno", getLoginName());
        return "student/course/list";
    }

    @PostMapping("/course/list")
    @ResponseBody
    public TableDataInfo courseList(Course course)
    {
        return getDataTable(courseService.selectCourseList(course));
    }

    @PostMapping("/course/select")
    @ResponseBody
    public AjaxResult selectCourse(String sno, String cno, String semester)
    {
        try
        {
            // 查询新课程的上课时间
            Class classQuery = new Class();
            classQuery.setCno(cno);
            List<Class> classList = classService.selectClassList(classQuery);
            if (classList != null && !classList.isEmpty())
            {
                String newClassTime = classList.get(0).getClassTime();
                if (newClassTime != null && !newClassTime.isEmpty())
                {
                    // 查询学生已选课程的上课时间
                    List<Map<String, Object>> scheduleList = courseSelectionService.selectStudentSchedule(sno);
                    for (Map<String, Object> schedule : scheduleList)
                    {
                        String existCno = (String) schedule.get("cno");
                        // 跳过同一门课程（已由重复检测处理）
                        if (cno.equals(existCno))
                        {
                            continue;
                        }
                        String existClassTime = (String) schedule.get("classTime");
                        String existCname = (String) schedule.get("cname");
                        if (existClassTime != null && !existClassTime.isEmpty()
                            && checkTimeConflict(newClassTime, existClassTime))
                        {
                            return error("选课冲突：与已选课程「" + existCname + "」的上课时间冲突，无法选课");
                        }
                    }
                }
            }

            CourseSelection cs = new CourseSelection();
            cs.setSno(sno);
            cs.setCno(cno);
            cs.setSemester(semester);
            return toAjax(courseSelectionService.insertCourseSelection(cs));
        }
        catch (org.springframework.dao.DataIntegrityViolationException e)
        {
            return error("The course has been selected, cannot repeat");
        }
        catch (IllegalArgumentException e)
        {
            return error("Selection failed: " + e.getMessage());
        }
        catch (org.springframework.dao.DataAccessException e)
        {
            return error("Course capacity full, cannot select");
        }
        catch (Exception e)
        {
            return error("Selection failed: " + e.getMessage());
        }
    }

    @GetMapping("/course/my")
    public String myCourse(ModelMap mmap)
    {
        mmap.put("sno", getLoginName());
        return "student/course/my";
    }

    @PostMapping("/course/my/list")
    @ResponseBody
    public TableDataInfo myCourseList(String sno)
    {
        CourseSelection cs = new CourseSelection();
        cs.setSno(sno);
        return getDataTable(courseSelectionService.selectCourseSelectionList(cs));
    }

    @PostMapping("/course/drop")
    @ResponseBody
    public AjaxResult dropCourse(String sno, String cno)
    {
        return toAjax(courseSelectionService.deleteCourseSelectionById(sno, cno));
    }

    @PostMapping("/info")
    @ResponseBody
    public AjaxResult info(String sno)
    {
        Student student = studentService.selectStudentById(sno);
        return success().put("data", student);
    }

    @GetMapping("/grade")
    public String grade(ModelMap mmap)
    {
        mmap.put("sno", getLoginName());
        return "student/grade/list";
    }

    @GetMapping("/gpa")
    @ResponseBody
    public AjaxResult gpa(String sno)
    {
        try
        {
            java.math.BigDecimal gpa = courseSelectionService.getStudentGpa(sno);
            return success().put("gpa", gpa);
        }
        catch (Exception e)
        {
            CourseSelection cs = new CourseSelection();
            cs.setSno(sno);
            java.util.List<CourseSelection> list = courseSelectionService.selectCourseSelectionList(cs);
            double totalPoints = 0;
            double totalCredits = 0;
            for (CourseSelection item : list)
            {
                if (item.getNormalScore() != null && item.getTestScore() != null)
                {
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
                    Course course = courseService.selectCourseById(item.getCno());
                    if (course != null)
                    {
                        totalPoints += gp * course.getCredit().doubleValue();
                        totalCredits += course.getCredit().doubleValue();
                    }
                }
            }
            double gpa = totalCredits > 0 ? totalPoints / totalCredits : 0;
            return success().put("gpa", Math.round(gpa * 100.0) / 100.0);
        }
    }

    @PostMapping("/grade/list")
    @ResponseBody
    public TableDataInfo gradeList(String sno)
    {
        CourseSelection cs = new CourseSelection();
        cs.setSno(sno);
        return getDataTable(courseSelectionService.selectCourseSelectionList(cs));
    }

    @GetMapping("/schedule")
    public String schedule(ModelMap mmap)
    {
        mmap.put("sno", getLoginName());
        return "student/schedule";
    }

    @PostMapping("/schedule/list")
    @ResponseBody
    public TableDataInfo scheduleList(String sno)
    {
        List<Map<String, Object>> list = courseSelectionService.selectStudentSchedule(sno);
        return getDataTable(list);
    }
    @GetMapping("/exam")
    public String exam(ModelMap mmap) { mmap.put("sno", getLoginName()); return "student/exam"; }

    /**
     * 检查两个课程的上课时间是否冲突
     * class_time 格式如 "周一3-4节 周三1-2节"
     * 冲突条件：同一天 + 节次范围有交集
     */
    private boolean checkTimeConflict(String time1, String time2)
    {
        // 解析时间字符串为 (星期几, 起始节, 结束节) 列表
        List<int[]> slots1 = parseClassTime(time1);
        List<int[]> slots2 = parseClassTime(time2);
        // 比较所有时间段，同一天且节次有交集则冲突
        for (int[] s1 : slots1)
        {
            for (int[] s2 : slots2)
            {
                if (s1[0] == s2[0] && s1[1] <= s2[2] && s2[1] <= s1[2])
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 解析 class_time 字符串，提取每个时间段的 (星期几, 起始节, 结束节)
     * 支持格式：周一3-4节、周二1-2节、周X N-M节 等
     */
    private List<int[]> parseClassTime(String classTime)
    {
        List<int[]> result = new ArrayList<int[]>();
        if (classTime == null || classTime.isEmpty())
        {
            return result;
        }
        // 匹配 "周X" + 数字-数字 + "节"
        Pattern pattern = Pattern.compile("周([一二三四五六七日天])\\s*(\\d+)\\s*-\\s*(\\d+)\\s*节");
        Matcher matcher = pattern.matcher(classTime);
        while (matcher.find())
        {
            int day = parseDay(matcher.group(1));
            int start = Integer.parseInt(matcher.group(2));
            int end = Integer.parseInt(matcher.group(3));
            result.add(new int[]{day, start, end});
        }
        return result;
    }

    /**
     * 将中文星期转换为数字（1=周一, 7=周日）
     */
    private int parseDay(String dayStr)
    {
        switch (dayStr)
        {
            case "一": return 1;
            case "二": return 2;
            case "三": return 3;
            case "四": return 4;
            case "五": return 5;
            case "六": return 6;
            case "七": case "日": case "天": return 7;
            default: return 0;
        }
    }

}