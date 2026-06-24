USE hikebuddy;

CREATE TABLE User (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(50) UNIQUE NOT NULL,
                      password_hash VARCHAR(255) NOT NULL,
                      salt VARCHAR(100) NOT NULL,
                      hiking_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'BEGINNER',
                      bio TEXT,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE HikeRoute (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           region VARCHAR(100),
                           difficulty ENUM('EASY', 'MEDIUM', 'HARD'),
                           distance DECIMAL(5,2),
                           description TEXT
);

CREATE TABLE JourneyEntry (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              hike_route_id INT,
                              date DATE,
                              distance DECIMAL(5,2),
                              difficulty ENUM('EASY', 'MEDIUM', 'HARD'),
                              status ENUM('WISHLIST', 'PENDING', 'COMPLETED') DEFAULT 'PENDING',
                              notes TEXT,
                              FOREIGN KEY (user_id) REFERENCES User(id),
                              FOREIGN KEY (hike_route_id) REFERENCES HikeRoute(id)
);

CREATE TABLE StoryFolder (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             user_id INT NOT NULL,
                             journey_entry_id INT,
                             name VARCHAR(100) NOT NULL,
                             description TEXT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES User(id),
                             FOREIGN KEY (journey_entry_id) REFERENCES JourneyEntry(id)
);

CREATE TABLE Photo (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       folder_id INT NOT NULL,
                       file_path VARCHAR(255) NOT NULL,
                       uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (folder_id) REFERENCES StoryFolder(id)
);