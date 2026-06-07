package com.ruoyi.web.controller.admin;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.Course;
import com.ruoyi.system.service.ICourseService;
import com.ruoyi.system.service.ICourseSwapService;
import com.ruoyi.system.domain.CourseSwap;

@Controller
@RequestMapping("/admin/audit")
@RequiresRoles("admin")
public class AdminAuditController extends BaseController
{
    @Autowired
    private ICourseService courseService;
    @Autowired
    private ICourseSwapService courseSwapService;

    @GetMapping()
    public String audit() { return "admin/course/review"; }

    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Course course) {
        startPage();
        course.setStatus(0); // pending only
        return getDataTable(courseService.selectCourseList(course));
    }

    @PostMapping("/approve")
    @ResponseBody
    public AjaxResult approve(String cno) {
        Course c = courseService.selectCourseById(cno);
        if (c != null) { c.setCno(cno); c.setStatus(1); courseService.updateCourse(c); }
        return success();
    }

    @PostMapping("/reject")
    @ResponseBody
    public AjaxResult reject(String cno, String reason) {
        Course c = courseService.selectCourseById(cno);
        if (c != null) { c.setCno(cno); c.setStatus(2); c.setRejectReason(reason); courseService.updateCourse(c); }
        return success();
    }

    @GetMapping("/swap")
    public String swap() { return "admin/swap/list"; }

    @PostMapping("/swap/list")
    @ResponseBody
    public TableDataInfo swapList() {
        startPage();
        CourseSwap cs = new CourseSwap();
        return getDataTable(courseSwapService.selectCourseSwapList(cs));
    }

    @PostMapping("/swap/approve")
    @ResponseBody
    public AjaxResult swapApprove(Integer swapId) {
        try {
            courseSwapService.approveCourseSwap(swapId);
            return success("已批准换课申请");
        } catch (Exception e) {
            return error("操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/swap/reject")
    @ResponseBody
    public AjaxResult swapReject(Integer swapId) {
        CourseSwap swap = courseSwapService.selectCourseSwapById(swapId);
        if (swap == null) return error("申请不存在");
        swap.setStatus(2);
        courseSwapService.updateCourseSwap(swap);
        return success("已拒绝换课申请");
    }
}
