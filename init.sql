CREATE TABLE departments (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             location VARCHAR(100) NOT NULL
);

CREATE TABLE employees (
                           id SERIAL PRIMARY KEY,
                           full_name VARCHAR(150) NOT NULL,
                           position VARCHAR(100) NOT NULL,
                           salary NUMERIC(10, 2) NOT NULL CHECK (salary > 0),
                           hire_date DATE NOT NULL DEFAULT CURRENT_DATE,
                           department_id INTEGER NOT NULL REFERENCES departments(id) ON DELETE CASCADE,
                           manager_id INTEGER REFERENCES employees(id) ON DELETE SET NULL
);

INSERT INTO departments (name, location) VALUES
                                             ('IT Відділ', 'Київ, корпус А'),
                                             ('Відділ продажів', 'Суми, головний офіс'),
                                             ('Бухгалтерія', 'Суми, корпус Б'),
                                             ('HR та Рекрутинг', 'Львів, філія');

INSERT INTO employees (full_name, position, salary, hire_date, department_id, manager_id) VALUES
                                                                                              ('Коваленко Олександр Іванович', 'Tech Lead', 85000.00, '2021-03-15', 1, NULL),
                                                                                              ('Бондаренко Марія Сергіївна', 'Senior Java Developer', 72000.00, '2022-05-10', 1, 1),
                                                                                              ('Шевченко Дмитро Олегович', 'Middle Java Developer', 48000.00, '2023-01-20', 1, 1),
                                                                                              ('Ткаченко Ірина Василівна', 'QA Engineer', 35000.00, '2024-02-01', 1, 1),

                                                                                              ('Мельник Андрій Петрович', 'Керівник відділу продажів', 55000.00, '2020-11-05', 2, NULL),
                                                                                              ('Кравченко Олена Миколаївна', 'Старший менеджер', 42000.00, '2022-08-12', 2, 5),
                                                                                              ('Поліщук Максим Вікторович', 'Менеджер з продажів', 28000.00, '2024-04-18', 2, 5),

                                                                                              ('Савченко Наталія Григорівна', 'Головний бухгалтер', 46000.00, '2019-09-01', 3, NULL),
                                                                                              ('Лисенко Віктор Павлович', 'Бухгалтер-обліковець', 31000.00, '2023-07-14', 3, 8),

                                                                                              ('Мороз Юлія Олександрівна', 'HR Director', 49000.00, '2021-06-22', 4, NULL),
                                                                                              ('Гаврилюк Артем Романович', 'IT Recruiter', 33000.00, '2023-10-03', 4, 10);