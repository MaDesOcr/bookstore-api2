-- Fixtures de test pour TP 2 et TP 3
INSERT INTO books (title, author, isbn, price, stock, category, created_at) VALUES
  ('Clean Code',          'R. Martin',  '978-0132350884', 29.99, 50, 'TECH',    now()),
  ('Clean Architecture',  'R. Martin',  '978-0134494166', 34.99, 30, 'TECH',    now()),
  ('DDD Distilled',       'V. Vernon',  '978-0134434421', 39.99, 20, 'TECH',    now()),
  ('Effective Java',      'J. Bloch',   '978-0134685991', 44.99, 40, 'TECH',    now()),
  ('The Hobbit',          'Tolkien',    '978-0547928227', 14.99, 80, 'FICTION', now()),
  ('LOTR Fellowship',     'Tolkien',    '978-0547928210', 16.99, 60, 'FICTION', now()),
  ('Design Patterns',     'GoF',        '978-0201633610', 54.99, 15, 'TECH',    now());
