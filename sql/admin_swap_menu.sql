-- 管理员换课审批菜单
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time, remark)
VALUES (2043, '换课审批', 2017, 5, '/admin/audit/swap', 'C', '0', 'admin:audit:swap', '#', 'admin', NOW(), '管理员审批教师换课申请');

-- 分配权限给管理员角色 (role_id=1)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 2043);

SELECT 'Menu added' AS result;
