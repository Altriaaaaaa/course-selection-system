package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.Class;
import com.ruoyi.system.domain.CourseSwap;
import com.ruoyi.system.mapper.CourseSwapMapper;
import com.ruoyi.system.service.IClassService;
import com.ruoyi.system.service.ICourseSwapService;
import com.ruoyi.system.service.ICourseSelectionService;
import com.ruoyi.system.service.INotificationService;
import com.ruoyi.system.domain.CourseSelection;
import com.ruoyi.system.domain.Notification;

@Service("courseSwapService")
public class CourseSwapServiceImpl implements ICourseSwapService
{
    @Autowired
    private CourseSwapMapper courseSwapMapper;

    @Autowired
    private IClassService classService;
    @Autowired
    private ICourseSelectionService courseSelectionService;
    @Autowired
    private INotificationService notificationService;

    @Override
    public List<CourseSwap> selectCourseSwapList(CourseSwap courseSwap)
    {
        return courseSwapMapper.selectCourseSwapList(courseSwap);
    }

    @Override
    public CourseSwap selectCourseSwapById(Integer swapId)
    {
        return courseSwapMapper.selectCourseSwapById(swapId);
    }

    @Override
    public int insertCourseSwap(CourseSwap courseSwap)
    {
        courseSwap.setStatus(0); // 默认待审批
        return courseSwapMapper.insertCourseSwap(courseSwap);
    }

    @Override
    public int updateCourseSwap(CourseSwap courseSwap)
    {
        return courseSwapMapper.updateCourseSwap(courseSwap);
    }

    @Override
    public int deleteCourseSwapById(Integer swapId)
    {
        return courseSwapMapper.deleteCourseSwapById(swapId);
    }

    @Override
    @Transactional
    public int approveCourseSwap(Integer swapId)
    {
        CourseSwap swap = courseSwapMapper.selectCourseSwapById(swapId);
        if (swap == null)
        {
            throw new IllegalArgumentException("换课申请不存在");
        }
        if (swap.getStatus() != 0)
        {
            throw new IllegalArgumentException("该申请已处理");
        }

        // 获取双方授课记录
        Class fromClass = classService.selectClassById(swap.getFromTno(), swap.getFromCno());
        Class toClass = classService.selectClassById(swap.getToTno(), swap.getToCno());

        if (fromClass == null || toClass == null)
        {
            throw new IllegalArgumentException("授课记录不存在，无法换课");
        }

        // 交换上课时间
        String fromTime = fromClass.getClassTime();
        String toTime = toClass.getClassTime();

        fromClass.setClassTime(toTime);
        toClass.setClassTime(fromTime);

        classService.updateClass(fromClass);
        classService.updateClass(toClass);

        // 更新申请状态为已批准
        swap.setStatus(1);
        int result = courseSwapMapper.updateCourseSwap(swap);

        // 通知受影响的学生
        try {
            notifyStudents(swap.getFromCno(), "换课通知",
                "您的课程「" + fromClass.getCname() + "」上课时间已调整为 " + toTime);
            notifyStudents(swap.getToCno(), "换课通知",
                "您的课程「" + toClass.getCname() + "」上课时间已调整为 " + fromTime);
        } catch (Exception ignored) {}

        return result;
    }

    private void notifyStudents(String cno, String title, String content)
    {
        CourseSelection query = new CourseSelection();
        query.setCno(cno);
        List<CourseSelection> students = courseSelectionService.selectCourseSelectionList(query);
        for (CourseSelection cs : students)
        {
            Notification notif = new Notification();
            notif.setRecipientType("student");
            notif.setRecipientId(cs.getSno());
            notif.setTitle(title);
            notif.setContent(content);
            notif.setIsRead(0);
            notif.setSourceType("course_swap");
            try { notificationService.insertNotification(notif); } catch (Exception ignored) {}
        }
    }
}
