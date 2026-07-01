-- ============================================================
-- OSLS Dummy Seed Data
-- Runs automatically on app startup (spring.sql.init.mode=always)
-- Uses INSERT IGNORE so re-running on an existing DB is safe.
-- ============================================================

-- ---------------------------------------------
-- Users
--   1) admin   / Admin123!     -> ADMIN role
--   2) jdoe     / Password123!  -> USER role (resource/comment author)
--   3) asmith   / Password123!  -> USER role (second contributor)
-- Passwords are BCrypt-hashed (compatible with Spring Security's
-- BCryptPasswordEncoder). Plaintext is given above for local testing only.
-- ---------------------------------------------
INSERT INTO users (id, username, email, password, role, created_at) VALUES
(1, 'admin', 'admin@buzzword.com', '$2a$10$J10g33U8YD1RNRT9K0Z8b.nhUJz3RZcuf/qEgItPgZWFNdfZfwddu', 'ADMIN', NOW()),
(2, 'jdoe',  'jdoe@buzzword.com',  '$2a$10$spBh.72kpjZB/NE.AyTMd.HzRn1yaA8ouI.HAOlJZv7NoXlBIOS8u', 'USER',  NOW()),
(3, 'asmith','asmith@buzzword.com','$2a$10$spBh.72kpjZB/NE.AyTMd.HzRn1yaA8ouI.HAOlJZv7NoXlBIOS8u', 'USER',  NOW())
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------
-- Resources ("products") — 3 dummy entries covering
-- different ResourceCategory values from the requirements doc
-- ---------------------------------------------
INSERT INTO resources (id, title, description, url, category, added_by, created_at, updated_at) VALUES
(1, 'Clean Code: A Handbook of Agile Software Craftsmanship',
    'Open-source-adjacent reference book on writing maintainable, readable code. Widely used as an internal onboarding reference.',
    'https://www.oreilly.com/library/view/clean-code/9780136083238/',
    'BOOK', 1, NOW(), NOW()),

(2, 'The Twelve-Factor App',
    'White paper describing a methodology for building software-as-a-service apps that are portable and resilient when deployed to the cloud.',
    'https://12factor.net/',
    'WHITEPAPER', 2, NOW(), NOW()),

(3, 'OWASP Top Ten',
    'Standard awareness document outlining the most critical security risks to web applications. Used as a baseline for internal security reviews.',
    'https://owasp.org/www-project-top-ten/',
    'STANDARD', 3, NOW(), NOW()),

(4, 'Effective Java (3rd Edition)',
    'A guide to best practices in Java programming, covering lambdas, streams, generics, and concurrency.',
    'https://www.oreilly.com/library/view/effective-java/9780134686097/',
    'BOOK', 1, NOW(), NOW()),

(5, 'Design Patterns: Elements of Reusable Object-Oriented Software',
    'The classic book on software design patterns by the Gang of Four, showing reusable solutions to common problems.',
    'https://www.oreilly.com/library/view/design-patterns-elements/0201633612/',
    'BOOK', 2, NOW(), NOW()),

(6, 'Kubernetes Documentation Portal',
    'Official documentation for production-grade container orchestration. Used for managing our containerized workloads.',
    'https://kubernetes.io/docs/',
    'WEBSITE', 3, NOW(), NOW()),

(7, 'JUnit 5 User Guide',
    'Comprehensive documentation for the JUnit 5 testing framework, including writing and running automated unit tests.',
    'https://junit.org/junit5/docs/current/user-guide/',
    'TOOL', 1, NOW(), NOW()),

(8, 'Docker Deep Dive',
    'Excellent reference guide and handbook for learning containerization, images, volumes, and networks.',
    'https://www.oreilly.com/library/view/docker-deep-dive/9781800565135/',
    'BOOK', 2, NOW(), NOW()),

(9, 'GNU General Public License (GPL) v3.0',
    'The standard open-source copyleft license text. Essential reading for our compliance and licensing workflows.',
    'https://www.gnu.org/licenses/gpl-3.0.html',
    'LEGAL', 3, NOW(), NOW()),

(10, 'Spring Framework Documentation',
    'Reference documentation for Dependency Injection, web apps, security, data access, and microservices.',
    'https://spring.io/projects/spring-framework',
    'WEBSITE', 1, NOW(), NOW()),

(11, 'Mozilla Developer Network (MDN) Web Docs',
    'The premier open-source repository for web technologies including HTML, CSS, JavaScript, and HTTP web APIs.',
    'https://developer.mozilla.org/',
    'WEBSITE', 2, NOW(), NOW()),

(12, 'Google Java Style Guide',
    'The official style guide defining standard Java formatting and coding conventions for Google software projects.',
    'https://google.github.io/styleguide/javaguide.html',
    'STANDARD', 3, NOW(), NOW()),

(13, 'Git Version Control Book',
    'Pro Git book covering simple and complex branching, merging, configuration, internals, and CLI workflows.',
    'https://git-scm.com/book/en/v2',
    'TOOL', 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------
-- Comments — a couple of sample comments per resource
-- ---------------------------------------------
INSERT INTO comments (id, content, resource_id, user_id, created_at, updated_at) VALUES
(1, 'Great refresher for new hires — recommend pairing this with our internal style guide.', 1, 2, NOW(), NOW()),
(2, 'Some of the examples feel dated (pre-Java 8), but the core principles still hold up.', 1, 3, NOW(), NOW()),
(3, 'We adopted most of this for our microservices checklist. Config-via-env-vars section is especially relevant.', 2, 1, NOW(), NOW()),
(4, 'Linked this in the new-service template repo so it shows up automatically for new teams.', 3, 1, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
