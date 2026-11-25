-- Student Support V4: cấu trúc database SQLite thực tế.
PRAGMA foreign_keys=ON;
CREATE TABLE cart_items (user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,package_id INTEGER NOT NULL REFERENCES packages(id),PRIMARY KEY(user_id,package_id));

CREATE TABLE chat_messages (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER NOT NULL REFERENCES users(id),role TEXT NOT NULL CHECK(role IN ('USER','ASSISTANT')),message TEXT NOT NULL,created_at INTEGER NOT NULL);

CREATE TABLE favorites (user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,package_id INTEGER NOT NULL REFERENCES packages(id),PRIMARY KEY(user_id,package_id));

CREATE TABLE learning_progress (user_id INTEGER NOT NULL REFERENCES users(id),resource_id INTEGER NOT NULL REFERENCES learning_resources(id),completed_at INTEGER NOT NULL,PRIMARY KEY(user_id,resource_id));

CREATE TABLE learning_resources (id INTEGER PRIMARY KEY AUTOINCREMENT,package_id INTEGER NOT NULL REFERENCES packages(id),title TEXT NOT NULL,type TEXT NOT NULL CHECK(type IN ('VIDEO','PDF','LINK')),url TEXT NOT NULL);

CREATE TABLE notifications (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER NOT NULL REFERENCES users(id),event_key TEXT NOT NULL,message TEXT NOT NULL,read_flag INTEGER NOT NULL DEFAULT 0,created_at INTEGER NOT NULL,UNIQUE(user_id,event_key));

CREATE TABLE orders (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id),
  package_id INTEGER NOT NULL REFERENCES packages(id),
  amount INTEGER NOT NULL,
  status TEXT NOT NULL CHECK(status IN ('PENDING','PAID','COMPLETED','CANCELLED')),
  created_at INTEGER NOT NULL
);

CREATE TABLE package_extras (package_id INTEGER PRIMARY KEY REFERENCES packages(id) ON DELETE CASCADE,lecturer TEXT NOT NULL DEFAULT '',course_code TEXT NOT NULL DEFAULT '',study_path TEXT NOT NULL DEFAULT '',trial_url TEXT NOT NULL DEFAULT '',sale_price INTEGER NOT NULL DEFAULT 0, package_code TEXT NOT NULL DEFAULT '', target_students TEXT NOT NULL DEFAULT '', level TEXT NOT NULL DEFAULT '', lesson_count INTEGER NOT NULL DEFAULT 0, support_channel TEXT NOT NULL DEFAULT '', benefits TEXT NOT NULL DEFAULT '', prerequisites TEXT NOT NULL DEFAULT '');

CREATE TABLE package_ratings (user_id INTEGER NOT NULL REFERENCES users(id),package_id INTEGER NOT NULL REFERENCES packages(id),stars INTEGER NOT NULL CHECK(stars BETWEEN 1 AND 5),comment TEXT NOT NULL DEFAULT '',PRIMARY KEY(user_id,package_id));

CREATE TABLE packages (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  description TEXT NOT NULL,
  price INTEGER NOT NULL CHECK(price >= 0),
  duration_days INTEGER NOT NULL CHECK(duration_days > 0),
  is_active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE payments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  order_id INTEGER NOT NULL UNIQUE REFERENCES orders(id),
  amount INTEGER NOT NULL,
  method TEXT NOT NULL,
  status TEXT NOT NULL,
  confirmed_at INTEGER NOT NULL
);

CREATE TABLE quiz_attempts (id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER NOT NULL REFERENCES users(id),question_id INTEGER NOT NULL REFERENCES quiz_questions(id),correct INTEGER NOT NULL,created_at INTEGER NOT NULL);

CREATE TABLE quiz_questions (id INTEGER PRIMARY KEY AUTOINCREMENT,package_id INTEGER NOT NULL REFERENCES packages(id),question TEXT NOT NULL,option_a TEXT NOT NULL,option_b TEXT NOT NULL,option_c TEXT NOT NULL,option_d TEXT NOT NULL,correct_index INTEGER NOT NULL CHECK(correct_index BETWEEN 0 AND 3));

CREATE TABLE ratings (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id),
  ticket_id INTEGER NOT NULL UNIQUE REFERENCES tickets(id),
  stars INTEGER NOT NULL CHECK(stars BETWEEN 1 AND 5),
  comment TEXT NOT NULL DEFAULT '',
  created_at INTEGER NOT NULL
);

CREATE TABLE student_profiles (user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE, university TEXT NOT NULL DEFAULT '',major TEXT NOT NULL DEFAULT '',cohort TEXT NOT NULL DEFAULT '');

CREATE TABLE study_notes (
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
   title TEXT NOT NULL,
   body TEXT NOT NULL DEFAULT '',
   created_at INTEGER NOT NULL,
   updated_at INTEGER NOT NULL
 );

CREATE TABLE subscriptions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id),
  package_id INTEGER NOT NULL REFERENCES packages(id),
  started_at INTEGER NOT NULL,
  expires_at INTEGER NOT NULL,
  UNIQUE(user_id, package_id)
);

CREATE TABLE tickets (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id),
  subject TEXT NOT NULL,
  message TEXT NOT NULL,
  reply TEXT NOT NULL DEFAULT '',
  status TEXT NOT NULL CHECK(status IN ('OPEN','RESOLVED')),
  created_at INTEGER NOT NULL,
  updated_at INTEGER NOT NULL
);

CREATE TABLE user_flags (user_id INTEGER PRIMARY KEY REFERENCES users(id),locked INTEGER NOT NULL DEFAULT 0);

CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  student_code TEXT UNIQUE,
  phone TEXT NOT NULL DEFAULT '',
  role TEXT NOT NULL CHECK(role IN ('ADMIN','STUDENT')),
  password_hash TEXT NOT NULL,
  password_salt TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

CREATE TABLE vouchers (code TEXT PRIMARY KEY,percent INTEGER NOT NULL CHECK(percent BETWEEN 1 AND 90),active INTEGER NOT NULL DEFAULT 1,expires_at INTEGER NOT NULL);

CREATE INDEX idx_orders_user ON orders(user_id);

CREATE UNIQUE INDEX idx_package_extras_code ON package_extras(package_code) WHERE package_code<>'';

CREATE INDEX idx_study_notes_user ON study_notes(user_id, id);

CREATE INDEX idx_tickets_user ON tickets(user_id);

