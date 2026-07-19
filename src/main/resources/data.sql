
-- CLEAR EXISTING DATA (safe to re-run on restart)
DELETE FROM notification_history;
DELETE FROM user_preferences;
DELETE FROM users;

-- USERS
INSERT INTO users (id, name, email, phone, device_token, created_at) VALUES
(1, 'Alice Johnson', 'alice@example.com',  '+919999911111', 'device-token-alice-fcm-001', NOW()),
(2, 'Bob Smith',     'bob@example.com',    '+919999922222', 'device-token-bob-fcm-002',   NOW()),
(3, 'Charlie Brown', 'charlie@example.com','+919999933333',  NULL,                         NOW());

-- USER PREFERENCES

INSERT INTO user_preferences (user_id, channel, opted_in) VALUES
(1, 'EMAIL',  true),
(1, 'SMS',    true),
(1, 'PUSH',   true),
(1, 'IN_APP', true);

INSERT INTO user_preferences (user_id, channel, opted_in) VALUES
(2, 'EMAIL',  true),
(2, 'SMS',    false),
(2, 'PUSH',   false),
(2, 'IN_APP', true);

INSERT INTO user_preferences (user_id, channel, opted_in) VALUES
(3, 'EMAIL',  false),
(3, 'SMS',    false),
(3, 'PUSH',   false),
(3, 'IN_APP', false);