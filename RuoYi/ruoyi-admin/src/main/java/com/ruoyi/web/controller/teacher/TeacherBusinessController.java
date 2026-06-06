package com.ruoyi.web.controller.teacher;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.Class;
import com.ruoyi.system.domain.CourseSelection;
import com.ruoyi.system.domain.CourseSwap;
import com.ruoyi.system.domain.Teacher;
import com.ruoyi.system.service.IClassService;
import com.ruoyi.system.service.ICourseSelectionService;
import com.ruoyi.system.service.ICourseSwapService;
import com.ruoyi.system.service.ITeacherService;

@Controller
@RequestMapping("/teacher")
@RequiresRoles("teacher")
public class TeacherBusinessController extends BaseController
{
    @Autowired
    private IClassService classService;

    @Autowired
    private ICourseSelectionService courseSelectionService;

    @Autowired
    private ITeacherService teacherService;

    @Autowired
    private ICourseSwapService courseSwapService;

    @GetMapping("/index")
    public String index(ModelMap mmap)
    {
        mmap.put("tno", getLoginName());
        return "teacher/index";
    }

    @GetMapping("/course")
    public String course(ModelMap mmap)
    {
        mmap.put("tno", getLoginName());
        return "teacher/course/list";
    }

    @PostMapping("/info")
    @ResponseBody
    public AjaxResult info(String tno)
    {
        Teacher teacher = teacherService.selectTeacherById(tno);
        return success().put("data", teacher);
    }

    @PostMapping("/course/list")
    @ResponseBody
    public TableDataInfo courseList(String tno)
    {
        Class clazz = new Class();
        clazz.setTno(tno);
        return getDataTable(classService.selectClassList(clazz));
    }

    @GetMapping("/course/students")
    public String students(String tno, String cno, ModelMap mmap)
    {
        mmap.put("tno", tno);
        mmap.put("cno", cno);
        return "teacher/course/students";
    }

    @PostMapping("/course/students/list")
    @ResponseBody
    public TableDataInfo studentsList(String cno)
    {
        String currentTno = getLoginName();
        Class clazz = classService.selectClassById(currentTno, cno);
        if (clazz == null)
        {
            return getDataTable(new ArrayList<>());
        }
        CourseSelection cs = new CourseSelection();
        cs.setCno(cno);
        return getDataTable(courseSelectionService.selectCourseSelectionList(cs));
    }

    @PostMapping("/grade/save")
    @ResponseBody
    public AjaxResult gradeSave(CourseSelection cs)
    {
        try
        {
            return toAjax(courseSelectionService.updateCourseSelection(cs));
        }
        catch (IllegalArgumentException e)
        {
            return error(e.getMessage());
        }
        catch (Exception e)
        {
            return error("Grade save failed: " + e.getMessage());
        }
    }

    @GetMapping("/schedule")
    public String schedule(ModelMap mmap)
    {
        mmap.put("tno", getLoginName());
        return "teacher/schedule";
    }

    @GetMapping("/statistics")
    public String statistics(ModelMap mmap)
    {
        mmap.put("tno", getLoginName());
        return "teacher/statistics";
    }

    @PostMapping("/statistics/list")
    @ResponseBody
    public TableDataInfo statisticsList(String tno)
    {
        List<Map<String, Object>> list = courseSelectionService.selectTeacherGradeStats(tno);
        return getDataTable(list);
    }

    @GetMapping("/course/students/export")
    public void exportStudents(String cno, HttpServletResponse response)
    {
        try
        {
            CourseSelection cs = new CourseSelection();
            cs.setCno(cno);
            List<CourseSelection> list = courseSelectionService.selectCourseSelectionList(cs);

            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("学生名单");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("学号");
            header.createCell(1).setCellValue("姓名");
            header.createCell(2).setCellValue("平时成绩");
            header.createCell(3).setCellValue("考试成绩");
            header.createCell(4).setCellValue("总评");

            int rowIdx = 1;
            for (CourseSelection item : list)
            {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getSno());
                row.createCell(1).setCellValue(item.getSname());
                if (item.getNormalScore() != null) row.createCell(2).setCellValue(item.getNormalScore().doubleValue());
                if (item.getTestScore() != null) row.createCell(3).setCellValue(item.getTestScore().doubleValue());
                if (item.getNormalScore() != null && item.getTestScore() != null)
                {
                    double total = item.getNormalScore().doubleValue() * 0.4 + item.getTestScore().doubleValue() * 0.6;
                    row.createCell(4).setCellValue(Math.round(total * 10.0) / 10.0);
                }
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("学生名单_" + cno + ".xlsx", StandardCharsets.UTF_8));
            OutputStream os = response.getOutputStream();
            wb.write(os);
            wb.close();
            os.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @PostMapping("/exam/save")
    @ResponseBody
    public AjaxResult examSave(String tno, String cno, String examTime)
    {
        Class clazz = classService.selectClassById(tno, cno);
        if (clazz == null)
        {
            return error("授课记录不存在");
        }
        clazz.setExamTime(examTime);
        classService.updateClass(clazz);
        return success("考试时间已更新");
    }

    @GetMapping("/swap")
    public String swap(ModelMap mmap)
    {
        mmap.put("tno", getLoginName());
        return "teacher/swap";
    }

    @PostMapping("/swap/list")
    @ResponseBody
    public TableDataInfo swapList(String tno)
    {
        CourseSwap cs = new CourseSwap();
        cs.setFromTno(tno);
        List<CourseSwap> list1 = courseSwapService.selectCourseSwapList(cs);
        CourseSwap cs2 = new CourseSwap();
        cs2.setToTno(tno);
        List<CourseSwap> list2 = courseSwapService.selectCourseSwapList(cs2);
        list1.addAll(list2);
        return getDataTable(list1);
    }

    @PostMapping("/swap/request")
    @ResponseBody
    public AjaxResult swapRequest(String fromTno, String fromCno, String toTno, String toCno, String swapWeek, String reason)
    {
        try
        {
            CourseSwap swap = new CourseSwap();
            swap.setFromTno(fromTno);
            swap.setFromCno(fromCno);
            swap.setToTno(toTno);
            swap.setToCno(toCno);
            swap.setSwapWeek(swapWeek);
            swap.setReason(reason);
            courseSwapService.insertCourseSwap(swap);
            return success("换课申请已提交");
        }
        catch (Exception e)
        {
            return error("申请失败: " + e.getMessage());
        }
    }

    @PostMapping("/swap/approve")
    @ResponseBody
    public AjaxResult swapApprove(Integer swapId)
    {
        try
        {
            courseSwapService.approveCourseSwap(swapId);
            return success("换课申请已批准");
        }
        catch (IllegalArgumentException e)
        {
            return error(e.getMessage());
        }
        catch (Exception e)
        {
            return error("批准失败: " + e.getMessage());
        }
    }

    @PostMapping("/swap/reject")
    @ResponseBody
    public AjaxResult swapReject(Integer swapId)
    {
        CourseSwap swap = courseSwapService.selectCourseSwapById(swapId);
        if (swap == null) return error("申请不存在");
        swap.setStatus(2);
        courseSwapService.updateCourseSwap(swap);
        return success("换课申请已拒绝");
    }

    @PostMapping("/swap/available/teachers")
    @ResponseBody
    public AjaxResult availableTeachers(String tno)
    {
        List<Teacher> list = teacherService.selectTeacherList(new Teacher());
        List<Teacher> result = new ArrayList<>();
        for (Teacher t : list)
        {
            if (!t.getTno().equals(tno))
            {
                result.add(t);
            }
        }
        return success().put("data", result);
    }

    /**
     * 教师教学数据概览
     */
    @PostMapping("/dashboard/overview")
    @ResponseBody
    public AjaxResult dashboardOverview(String tno)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 总课程数
        Class clazzQuery = new Class();
        clazzQuery.setTno(tno);
        List<Class> courseList = classService.selectClassList(clazzQuery);
        result.put("totalCourses", courseList.size());
        
        // 2. 总学生数（去重）
        int totalStudents = 0;
        int totalWithGrades = 0;
        double sumTotalScore = 0;
        int passCount = 0;
        int failCount = 0;
        
        // 成绩分布
        int excellent = 0; // 90-100
        int good = 0;      // 80-89
        int medium = 0;    // 70-79
        int pass = 0;      // 60-69
        int fail = 0;      // <60
        
        // 课程成绩统计
        List<Map<String, Object>> courseStats = new ArrayList<>();
        
        for (Class clazz : courseList)
        {
            String cno = clazz.getCno();
            CourseSelection csQuery = new CourseSelection();
            csQuery.setCno(cno);
            List<CourseSelection> students = courseSelectionService.selectCourseSelectionList(csQuery);
            
            int courseStudentCount = students.size();
            totalStudents += courseStudentCount;
            
            double courseSum = 0;
            int courseWithGrades = 0;
            int coursePass = 0;
            int courseFail = 0;
            
            for (CourseSelection cs : students)
            {
                if (cs.getNormalScore() != null && cs.getTestScore() != null)
                {
                    double total = cs.getNormalScore().doubleValue() * 0.4 + cs.getTestScore().doubleValue() * 0.6;
                    totalWithGrades++;
                    sumTotalScore += total;
                    courseSum += total;
                    courseWithGrades++;
                    
                    if (total >= 60)
                    {
                        passCount++;
                        coursePass++;
                    }
                    else
                    {
                        failCount++;
                        courseFail++;
                    }
                    
                    if (total >= 90) excellent++;
                    else if (total >= 80) good++;
                    else if (total >= 70) medium++;
                    else if (total >= 60) pass++;
                    else fail++;
                }
            }
            
            Map<String, Object> stat = new HashMap<>();
            stat.put("cno", cno);
            stat.put("cname", clazz.getCname());
            stat.put("studentCount", courseStudentCount);
            stat.put("avgScore", courseWithGrades > 0 ? Math.round(courseSum / courseWithGrades * 100.0) / 100.0 : 0);
            stat.put("passCount", coursePass);
            stat.put("failCount", courseFail);
            stat.put("passRate", courseWithGrades > 0 ? Math.round((double) coursePass / courseWithGrades * 10000.0) / 100.0 : 0);
            courseStats.add(stat);
        }
        
        result.put("totalStudents", totalStudents);
        result.put("totalWithGrades", totalWithGrades);
        result.put("avgScore", totalWithGrades > 0 ? Math.round(sumTotalScore / totalWithGrades * 100.0) / 100.0 : 0);
        result.put("passRate", totalWithGrades > 0 ? Math.round((double) passCount / totalWithGrades * 10000.0) / 100.0 : 0);
        result.put("passCount", passCount);
        result.put("failCount", failCount);
        
        // 成绩分布
        Map<String, Object> gradeDist = new HashMap<>();
        gradeDist.put("excellent", excellent);
        gradeDist.put("good", good);
        gradeDist.put("medium", medium);
        gradeDist.put("pass", pass);
        gradeDist.put("fail", fail);
        result.put("gradeDistribution", gradeDist);
        
        // 课程统计
        result.put("courseStats", courseStats);
        
        return success().put("data", result);
    }

    /**
     * 学生成绩排名
     */
    @PostMapping("/dashboard/rankings")
    @ResponseBody
    public AjaxResult dashboardRankings(String tno)
    {
        List<Map<String, Object>> rankings = new ArrayList<>();
        
        Class clazzQuery = new Class();
        clazzQuery.setTno(tno);
        List<Class> courseList = classService.selectClassList(clazzQuery);
        
        for (Class clazz : courseList)
        {
            String cno = clazz.getCno();
            CourseSelection csQuery = new CourseSelection();
            csQuery.setCno(cno);
            List<CourseSelection> students = courseSelectionService.selectCourseSelectionList(csQuery);
            
            for (CourseSelection cs : students)
            {
                if (cs.getNormalScore() != null && cs.getTestScore() != null)
                {
                    double total = cs.getNormalScore().doubleValue() * 0.4 + cs.getTestScore().doubleValue() * 0.6;
                    Map<String, Object> row = new HashMap<>();
                    row.put("sno", cs.getSno());
                    row.put("sname", cs.getSname());
                    row.put("cno", cno);
                    row.put("cname", clazz.getCname());
                    row.put("normalScore", cs.getNormalScore());
                    row.put("testScore", cs.getTestScore());
                    row.put("totalScore", Math.round(total * 10.0) / 10.0);
                    row.put("gpa", calculateGpa(total));
                    rankings.add(row);
                }
            }
        }
        
        // 按总评降序排序
        rankings.sort((a, b) -> Double.compare((Double) b.get("totalScore"), (Double) a.get("totalScore")));
        
        // 添加排名
        for (int i = 0; i < rankings.size(); i++)
        {
            rankings.get(i).put("rank", i + 1);
        }
        
        return success().put("data", rankings);
    }

    private double calculateGpa(double score)
    {
        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 82) return 3.3;
        if (score >= 78) return 3.0;
        if (score >= 75) return 2.7;
        if (score >= 72) return 2.3;
        if (score >= 68) return 2.0;
        if (score >= 64) return 1.5;
        if (score >= 60) return 1.0;
        return 0.0;
    }
}
