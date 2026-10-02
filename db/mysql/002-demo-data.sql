USE hmdp;

INSERT INTO tb_shop_type (id, name, icon, sort) VALUES
  (1, '美食', '/types/ms.png', 1),
  (2, '休闲娱乐', '/types/yl.png', 2),
  (3, '咖啡茶饮', '/types/cy.png', 3)
ON DUPLICATE KEY UPDATE name = VALUES(name), icon = VALUES(icon), sort = VALUES(sort);

INSERT INTO tb_shop
  (id, name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours)
VALUES
  (900001, '邻里小馆（演示店）', 1, '', '思明区', '厦门市思明区演示路 1 号', 118.0894, 24.4798, 68, 12, 3, 48, '10:00-22:00'),
  (900002, '海风咖啡（演示店）', 3, '', '湖里区', '厦门市湖里区演示路 2 号', 118.1460, 24.5120, 32, 8, 2, 46, '08:00-20:00'),
  (900003, '城市漫步馆（演示店）', 2, '', '集美区', '厦门市集美区演示路 3 号', 118.0970, 24.5750, 45, 5, 1, 45, '09:00-21:00')
ON DUPLICATE KEY UPDATE name = VALUES(name), type_id = VALUES(type_id), address = VALUES(address);
