package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CourseSwap;

public interface CourseSwapMapper
{
    public List<CourseSwap> selectCourseSwapList(CourseSwap courseSwap);

    public CourseSwap selectCourseSwapById(Integer swapId);

    public int insertCourseSwap(CourseSwap courseSwap);

    public int updateCourseSwap(CourseSwap courseSwap);

    public int deleteCourseSwapById(Integer swapId);
}
