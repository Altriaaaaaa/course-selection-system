-- 换课功能数据库脚本
-- 在同学的项目数据库中执行以下SQL

-- 1. 创建换课申请表
CREATE TABLE IF NOT EXISTS course_swap (
    swap_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '换课申请ID',
    from_tno CHAR(8) NOT NULL COMMENT '申请教师工号',
    from_cno CHAR(8) NOT NULL COMMENT '申请换出的课程号',
    to_tno CHAR(8) NOT NULL COMMENT '目标教师工号',
    to_cno CHAR(8) NOT NULL COMMENT '目标换入的课程号',
    swap_week VARCHAR(50) DEFAULT NULL COMMENT '换课周次',
    reason TEXT DEFAULT NULL COMMENT '换课原因',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待审批 1已批准 2已拒绝',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_from_tno (from_tno),
    INDEX idx_to_tno (to_tno),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='换课申请表';
