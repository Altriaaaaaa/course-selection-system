package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CourseSwap;

public interface ICourseSwapService
{
    public List<CourseSwap> selectCourseSwapList(CourseSwap courseSwap);

    public CourseSwap selectCourseSwapById(Integer swapId);

    public int insertCourseSwap(CourseSwap courseSwap);

    public int updateCourseSwap(CourseSwap courseSwap);

    public int deleteCourseSwapById(Integer swapId);

    public int approveCourseSwap(Integer swapId);
}
