-- Тестовые клиенты
INSERT INTO clients (id, name, phone, password_hash, email, address_id, status, is_active)
VALUES
  (3001, 'Анна Сергеева', '+79990000001', 'hash01', 'anna.sergeeva@test.local', 1001, 'ACTIVE', TRUE),
  (3002, 'Иван Петров', '+79990000002', 'hash02', 'ivan.petrov@test.local', 1002, 'ACTIVE', TRUE),
  (3003, 'Мария Орлова', '+79990000003', 'hash03', 'maria.orlova@test.local', 1003, 'ACTIVE', TRUE),
  (3004, 'Дмитрий Смирнов', '+79990000004', 'hash04', 'dmitry.smirnov@test.local', 1004, 'ACTIVE', TRUE),
  (3005, 'Екатерина Волкова', '+79990000005', 'hash05', 'ekaterina.volkova@test.local', 1005, 'ACTIVE', TRUE),
  (3006, 'Никита Соколов', '+79990000006', 'hash06', 'nikita.sokolov@test.local', 1006, 'ACTIVE', TRUE),
  (3007, 'Ольга Васильева', '+79990000007', 'hash07', 'olga.vasileva@test.local', 1007, 'ACTIVE', TRUE),
  (3008, 'Сергей Кузнецов', '+79990000008', 'hash08', 'sergey.kuznetsov@test.local', 1008, 'ACTIVE', TRUE),
  (3009, 'Юлия Фёдорова', '+79990000009', 'hash09', 'yulia.fedorova@test.local', 1009, 'ACTIVE', TRUE),
  (3010, 'Павел Морозов', '+79990000010', 'hash10', 'pavel.morozov@test.local', 1010, 'ACTIVE', TRUE),
  (3011, 'Светлана Белова', '+79990000011', 'hash11', 'svetlana.belova@test.local', 1011, 'ACTIVE', TRUE),
  (3012, 'Андрей Никитин', '+79990000012', 'hash12', 'andrey.nikitin@test.local', 1012, 'ACTIVE', TRUE),
  (3013, 'Ксения Миронова', '+79990000013', 'hash13', 'kseniya.mironova@test.local', 1013, 'ACTIVE', TRUE),
  (3014, 'Роман Захаров', '+79990000014', 'hash14', 'roman.zakharov@test.local', 1014, 'ACTIVE', TRUE),
  (3015, 'Людмила Ковалева', '+79990000015', 'hash15', 'lyudmila.kovaleva@test.local', 1015, 'ACTIVE', TRUE),
  (3016, 'Алексей Ефимов', '+79990000016', 'hash16', 'aleksey.efimov@test.local', 1016, 'ACTIVE', TRUE),
  (3017, 'Виктория Синицина', '+79990000017', 'hash17', 'viktoriya.sinitsina@test.local', 1017, 'ACTIVE', TRUE),
  (3018, 'Глеб Карпов', '+79990000018', 'hash18', 'gleb.karpov@test.local', 1018, 'ACTIVE', TRUE)
ON CONFLICT DO NOTHING;
