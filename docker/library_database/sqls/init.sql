CREATE TABLE "Library" (
    "ID" serial PRIMARY KEY,
    "Name" varchar(150) NOT NULL,
    "Address" varchar(50) NOT NULL
);

CREATE TABLE "ReaderCategory" (
    "ID" serial PRIMARY KEY,
    "Name" varchar(30) NOT NULL
);

CREATE TABLE "Reader" (
    "LibraryCardNumber" serial PRIMARY KEY,
    "Surname" varchar(15) NOT NULL,
    "Name" varchar(15) NOT NULL,
    "Patronymic" varchar(15) NOT NULL,
    "BirthDay" date NOT NULL CHECK ("BirthDay" <= CURRENT_DATE - INTERVAL '12 YEARS'),
    "CategoryID" int REFERENCES "ReaderCategory"("ID") ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TYPE "LiteraryWorkCategory" AS ENUM (
    'Повесть',
    'Роман',
    'Статья',
    'Учебник',
    'Стихотворение',
    'Диссертация',
    'Реферат',
    'Тезис доклада',
    'Литература по саморазвитию',
    'Сказка',
    'Поэма',
    'Мифология',
    'Научно-популярная литература');

CREATE TABLE "LiteraryWork" (
    "ID" serial PRIMARY KEY,
    "Title" varchar(100) NOT NULL,
    "Author" varchar(150) NOT NULL,
    "WritingYear" int,
    "Category" "LiteraryWorkCategory"
);

CREATE TABLE "PublicationStorageLocation" (
    "StorageLocationID" serial PRIMARY KEY,
    "LibraryID" int REFERENCES "Library"("ID") ON UPDATE CASCADE ON DELETE CASCADE,
    "RoomNumber" int CHECK ("RoomNumber" > 0),
    "ShelvingNumber" int CHECK ("ShelvingNumber" > 0),
    "ShelfNumber" int CHECK ("ShelfNumber" > 0)
);

CREATE TYPE "PublicationCategory" AS ENUM (
    'Книга',
    'Журнал',
    'Газета',
    'Сборник статей',
    'Сборник стихов',
    'Диссертация',
    'Реферат',
    'Сборник докладов',
    'Сборник тезисов докладов');

CREATE TYPE "PublicationState" AS ENUM (
    'Выдано',
    'В наличии',
    'Списано');

CREATE TABLE "Publication" (
    "NomenclatureNumber" serial PRIMARY KEY,
    "Title" varchar(200) NOT NULL,
    "Publisher" varchar(50) NOT NULL,
    "ReceiptDate" date NOT NULL,
    "YearOfPrinting" int,
    "Category" "PublicationCategory",
    "AgeRestriction" int,
    "StorageLocationID" int REFERENCES "PublicationStorageLocation"("StorageLocationID") ON UPDATE SET NULL
        ON DELETE SET NULL,
    "State" "PublicationState" NOT NULL,
    "PermissionToIssue" boolean NOT NULL,
    "DaysForReturn" int NOT NULL
);

CREATE TABLE "PublicationPermissionToIssue" (
    "PublicationNomenclatureNumber" int NOT NULL REFERENCES "Publication"("NomenclatureNumber") ON UPDATE CASCADE ON
        DELETE CASCADE,
    "ReaderCategoryID" int NOT NULL REFERENCES "ReaderCategory"("ID") ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY ("PublicationNomenclatureNumber", "ReaderCategoryID")
);

CREATE TABLE "Librarian" (
    "LibrarianID" serial PRIMARY KEY,
    "Surname" varchar(15) NOT NULL,
    "Name" varchar(15) NOT NULL,
    "Patronymic" varchar(15) NOT NULL,
    "BirthDay" date NOT NULL CHECK ("BirthDay" <= CURRENT_DATE - INTERVAL '18 YEARS'),
    "PhoneNumber" varchar(12) NOT NULL,
    "LibraryID" int NOT NULL REFERENCES "Library"("ID") ON UPDATE CASCADE ON DELETE CASCADE,
    "RoomNumber" int NOT NULL CHECK ("RoomNumber" > 0)
);

CREATE TABLE "HistoryOfIssueOfPublications" (
    "IssuedPublicationID" serial PRIMARY KEY,
    "PublicationNomenclatureNumber" int NOT NULL REFERENCES "Publication"("NomenclatureNumber") ON UPDATE CASCADE ON
        DELETE CASCADE,
    "LibraryCardNumber" int NOT NULL REFERENCES "Reader"("LibraryCardNumber") ON UPDATE CASCADE ON DELETE CASCADE,
    "IssueDate" date NOT NULL,
    "ReturnDate" date,
    "LibrarianID" int REFERENCES "Librarian"("LibrarianID") ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE "WrittenOffPublications" (
    "WriteOffID" serial PRIMARY KEY,
    "PublicationNomenclatureNumber" int NOT NULL REFERENCES "Publication"("NomenclatureNumber") ON UPDATE CASCADE ON
    DELETE CASCADE,
    "WriteOffDate" date NOT NULL
);

CREATE TABLE "PublicationToLiteraryWork" (
    "LiteraryWorkID" int NOT NULL REFERENCES "LiteraryWork"("ID") ON UPDATE CASCADE ON DELETE CASCADE,
    "PublicationNomenclatureNumber" int NOT NULL REFERENCES "Publication"("NomenclatureNumber") ON UPDATE CASCADE ON
        DELETE CASCADE,
    PRIMARY KEY ("LiteraryWorkID", "PublicationNomenclatureNumber")
);

CREATE TABLE "ReaderToLibrary" (
    "ReaderLibraryCardNumber" int NOT NULL REFERENCES "Reader"("LibraryCardNumber") ON UPDATE CASCADE ON DELETE CASCADE,
    "LibraryID" int NOT NULL REFERENCES "Library"("ID") ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY ("ReaderLibraryCardNumber", "LibraryID")
);

CREATE TABLE "StudentInformation" (
    "StudentID" serial PRIMARY KEY,
    "LibraryCardNumber" int NOT NULL UNIQUE REFERENCES "Reader"("LibraryCardNumber") ON UPDATE CASCADE ON DELETE CASCADE,
    "EducationalInstitutionName" varchar(150) NOT NULL,
    "Faculty" varchar(60),
    "Course" int CHECK ("Course" > 0 AND "Course" <= 6),
    "GroupNumber" int,
    "StudentCardNumber" int NOT NULL,
    "ExtensionDate" date NOT NULL
);

CREATE TABLE "SchoolchildInformation" (
    "SchoolchildID" serial PRIMARY KEY,
    "LibraryCardNumber" int NOT NULL UNIQUE REFERENCES "Reader"("LibraryCardNumber") ON UPDATE CASCADE ON DELETE CASCADE,
    "EducationalInstitutionName" varchar(60) NOT NULL,
    "Grade" int CHECK ("Grade" >= 1 AND "Grade" <= 11),
    "ExtensionDate" date NOT NULL
);

CREATE TABLE "LecturerInformation" (
    "LecturerID" serial PRIMARY KEY,
    "LibraryCardNumber" int NOT NULL UNIQUE REFERENCES "Reader"("LibraryCardNumber") ON UPDATE CASCADE ON DELETE CASCADE,
    "EducationalInstitutionName" varchar(150) NOT NULL,
    "JobTitle" varchar(100)
);

CREATE TABLE "PensionerInformation" (
    "PensionerID" serial PRIMARY KEY,
    "LibraryCardNumber" int NOT NULL UNIQUE REFERENCES "Reader"("LibraryCardNumber") ON UPDATE CASCADE ON DELETE CASCADE,
    "PensionCertificateNumber" varchar(11) NOT NULL
);

CREATE TABLE "ScientificWorkerInformation" (
    "ScientificWorkerID" serial PRIMARY KEY,
    "LibraryCardNumber" int NOT NULL UNIQUE REFERENCES "Reader"("LibraryCardNumber") ON UPDATE CASCADE ON DELETE CASCADE,
    "OrganizationName" varchar(60) NOT NULL,
    "ScientificTopic" varchar(100) NOT NULL
);

-- Вставка записи в "Сведения о студентах"

CREATE FUNCTION StudentCheckInsert()
RETURNS TRIGGER AS $$
    BEGIN
        IF (SELECT "Reader"."CategoryID" FROM "Reader" WHERE "LibraryCardNumber" = NEW."LibraryCardNumber") IS NOT
            NULL THEN RAISE EXCEPTION 'reader already belongs to one of the categories';
        END IF;
        UPDATE "Reader" SET "CategoryID" = (
            SELECT "ReaderCategory"."ID"
            FROM "ReaderCategory"
            WHERE "ReaderCategory"."Name" = 'Студент')
        WHERE "LibraryCardNumber" = NEW."LibraryCardNumber";
        RETURN NEW;
    END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "StudentCheckInsert"
    BEFORE INSERT ON "StudentInformation"
    FOR EACH ROW EXECUTE FUNCTION StudentCheckInsert();

-- Вставка записи в "Сведения о школьниках"

CREATE FUNCTION SchoolchildCheckInsert()
    RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT "Reader"."CategoryID" FROM "Reader" WHERE "LibraryCardNumber" = NEW."LibraryCardNumber") IS NOT
        NULL THEN RAISE EXCEPTION 'reader already belongs to one of the categories';
    END IF;
    UPDATE "Reader" SET "CategoryID" = (
        SELECT "ReaderCategory"."ID"
        FROM "ReaderCategory"
        WHERE "ReaderCategory"."Name" = 'Школьник')
    WHERE "LibraryCardNumber" = NEW."LibraryCardNumber";
    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "SchoolchildCheckInsert"
    BEFORE INSERT ON "SchoolchildInformation"
    FOR EACH ROW EXECUTE FUNCTION SchoolchildCheckInsert();

-- Вставка записи в "Сведения о преподавателях"

CREATE FUNCTION LecturerCheckInsert()
    RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT "Reader"."CategoryID" FROM "Reader" WHERE "LibraryCardNumber" = NEW."LibraryCardNumber") IS NOT
        NULL THEN RAISE EXCEPTION 'reader already belongs to one of the categories';
    END IF;
    UPDATE "Reader" SET "CategoryID" = (
        SELECT "ReaderCategory"."ID"
        FROM "ReaderCategory"
        WHERE "ReaderCategory"."Name" = 'Преподаватель')
    WHERE "LibraryCardNumber" = NEW."LibraryCardNumber";
    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "LecturerCheckInsert"
    BEFORE INSERT ON "LecturerInformation"
    FOR EACH ROW EXECUTE FUNCTION LecturerCheckInsert();

-- Вставка записи в "Сведения о пенсионерах"

CREATE FUNCTION PensionerCheckInsert()
    RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT "Reader"."CategoryID" FROM "Reader" WHERE "LibraryCardNumber" = NEW."LibraryCardNumber") IS NOT
        NULL THEN RAISE EXCEPTION 'reader already belongs to one of the categories';
    END IF;
    UPDATE "Reader" SET "CategoryID" = (
        SELECT "ReaderCategory"."ID"
        FROM "ReaderCategory"
        WHERE "ReaderCategory"."Name" = 'Пенсионер')
    WHERE "LibraryCardNumber" = NEW."LibraryCardNumber";
    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "PensionerCheckInsert"
    BEFORE INSERT ON "PensionerInformation"
    FOR EACH ROW EXECUTE FUNCTION PensionerCheckInsert();

-- Вставка записи в "Сведения о научных работниках"

CREATE FUNCTION ScientificWorkerCheckInsert()
    RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT "Reader"."CategoryID" FROM "Reader" WHERE "LibraryCardNumber" = NEW."LibraryCardNumber") IS NOT
        NULL THEN RAISE EXCEPTION 'reader already belongs to one of the categories';
    END IF;
    UPDATE "Reader" SET "CategoryID" = (
        SELECT "ReaderCategory"."ID"
        FROM "ReaderCategory"
        WHERE "ReaderCategory"."Name" = 'Научный работник')
    WHERE "LibraryCardNumber" = NEW."LibraryCardNumber";
    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "ScientificWorkerCheckInsert"
    BEFORE INSERT ON "ScientificWorkerInformation"
    FOR EACH ROW EXECUTE FUNCTION ScientificWorkerCheckInsert();

-- Удаление записи из "Сведения о научных работниках"

CREATE FUNCTION ScientificWorkerCategoryDelete()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE "Reader" SET "CategoryID" = NULL WHERE "LibraryCardNumber" = OLD."LibraryCardNumber";
    RETURN OLD;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "ScientificWorkerCategoryDelete"
    AFTER DELETE ON "ScientificWorkerInformation"
    FOR EACH ROW EXECUTE FUNCTION ScientificWorkerCategoryDelete();

-- Удаление записи из "Сведения о пенсионерах"

CREATE FUNCTION PensionerCategoryDelete()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE "Reader" SET "CategoryID" = NULL WHERE "LibraryCardNumber" = OLD."LibraryCardNumber";
    RETURN OLD;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "PensionerCategoryDelete"
    AFTER DELETE ON "PensionerInformation"
    FOR EACH ROW EXECUTE FUNCTION PensionerCategoryDelete();

-- Удаление записи из "Сведения о преподавателях"

CREATE FUNCTION LecturerCategoryDelete()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE "Reader" SET "CategoryID" = NULL WHERE "LibraryCardNumber" = OLD."LibraryCardNumber";
    RETURN OLD;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "LecturerCategoryDelete"
    AFTER DELETE ON "LecturerInformation"
    FOR EACH ROW EXECUTE FUNCTION LecturerCategoryDelete();

-- Удаление записи из "Сведения о школьниках"

CREATE FUNCTION SchoolchildCategoryDelete()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE "Reader" SET "CategoryID" = NULL WHERE "LibraryCardNumber" = OLD."LibraryCardNumber";
    RETURN OLD;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "SchoolchildCategoryDelete"
    AFTER DELETE ON "SchoolchildInformation"
    FOR EACH ROW EXECUTE FUNCTION SchoolchildCategoryDelete();

-- Удаление записи из "Сведения о студентах"

CREATE FUNCTION StudentCategoryDelete()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE "Reader" SET "CategoryID" = NULL WHERE "LibraryCardNumber" = OLD."LibraryCardNumber";
    RETURN OLD;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "StudentCategoryDelete"
    AFTER DELETE ON "StudentInformation"
    FOR EACH ROW EXECUTE FUNCTION StudentCategoryDelete();

-- Вставка записи в "История выдачи изданий"

CREATE FUNCTION HistoryOfIssuePublicationsCheckInsert()
    RETURNS TRIGGER AS $$
DECLARE
    "NEW.ReaderCategoryID" int;
    "NEW.ReaderCategory" varchar;
    "NEW.AgeRestriction" int;
    "NEW.ReaderBirthDay" date;
BEGIN
    IF (SELECT "State" FROM "Publication" WHERE "NomenclatureNumber" = NEW."PublicationNomenclatureNumber") != 'В наличии'
    THEN RAISE EXCEPTION 'selected publication is not available';
    END IF;
    IF (SELECT "PermissionToIssue" FROM "Publication" WHERE "NomenclatureNumber" = NEW."PublicationNomenclatureNumber") IS FALSE
    THEN
        SELECT "CategoryID"
        INTO "NEW.ReaderCategoryID"
        FROM "Reader"
        WHERE "LibraryCardNumber" = NEW."LibraryCardNumber";

        IF "NEW.ReaderCategoryID" IS NULL
        THEN RAISE EXCEPTION 'selected publication is not available for readers without category';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM "PublicationPermissionToIssue"
            WHERE "PublicationNomenclatureNumber" = NEW."PublicationNomenclatureNumber" AND
                "ReaderCategoryID" = "NEW.ReaderCategoryID"
        ) THEN RAISE EXCEPTION 'selected publication is not available for this category of readers';
        END IF;

        SELECT "Name" INTO "NEW.ReaderCategory" FROM "ReaderCategory" WHERE "ID" = "NEW.ReaderCategoryID";

        IF ("NEW.ReaderCategory" = 'Студент') THEN
            IF (SELECT "ExtensionDate" FROM "StudentInformation" WHERE "LibraryCardNumber" = NEW
                ."LibraryCardNumber") <= CURRENT_DATE
            THEN RAISE EXCEPTION 'the student subscription renewal period has expired';
            END IF;
        ELSEIF ("NEW.ReaderCategory" = 'Школьник') THEN
            IF (SELECT "ExtensionDate" FROM "SchoolchildInformation" WHERE "LibraryCardNumber" = NEW
                ."LibraryCardNumber") <= CURRENT_DATE
            THEN RAISE EXCEPTION 'the schoolchild subscription renewal period has expired';
            END IF;
        END IF;
    END IF;

    SELECT "AgeRestriction" INTO "NEW.AgeRestriction"
    FROM "Publication" WHERE "NomenclatureNumber" = NEW."PublicationNomenclatureNumber";

    SELECT "BirthDay" INTO "NEW.ReaderBirthDay"
    FROM "Reader" WHERE "LibraryCardNumber" = NEW."LibraryCardNumber";

    IF "NEW.AgeRestriction" IS NOT NULL AND
       CURRENT_DATE - INTERVAL '1 year' * "NEW.AgeRestriction" < "NEW.ReaderBirthDay"
    THEN RAISE EXCEPTION 'reader does not meet the age restriction';
    END IF;

    IF NEW."ReturnDate" IS NULL THEN
        UPDATE "Publication" SET "State" = 'Выдано'
        WHERE "NomenclatureNumber" = NEW."PublicationNomenclatureNumber";
    END IF;

    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "HistoryOfIssuePublicationsCheckInsert"
    BEFORE INSERT ON "HistoryOfIssueOfPublications"
    FOR EACH ROW EXECUTE FUNCTION HistoryOfIssuePublicationsCheckInsert();

-- Изменение записи в "История выдачи изданий"

CREATE FUNCTION HistoryOfIssuePublicationsCheckUpdate()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE "Publication" SET "State" = 'В наличии'
    WHERE "NomenclatureNumber" = NEW."PublicationNomenclatureNumber";
    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "HistoryOfIssuePublicationsCheckUpdate"
    AFTER UPDATE OF "ReturnDate" ON "HistoryOfIssueOfPublications"
    FOR EACH ROW EXECUTE FUNCTION HistoryOfIssuePublicationsCheckUpdate();

-- Вставка записи в "Списанные издания"

CREATE FUNCTION WrittenOffPublicationsCheckInsert()
    RETURNS TRIGGER AS $$
DECLARE
    "PublicationState" varchar;
BEGIN
    SELECT "State"
    INTO "PublicationState"
    FROM "Publication"
    WHERE "NomenclatureNumber" = NEW."PublicationNomenclatureNumber";

    IF "PublicationState" = 'Списано' THEN RAISE EXCEPTION 'publication has already been written off';
    ELSEIF "PublicationState" = 'Выдано' THEN RAISE EXCEPTION 'publication is out of stock';
    END IF;

    IF NEW."WriteOffDate" > CURRENT_DATE THEN RAISE EXCEPTION 'write off date cannot be in the future';
    END IF;

    UPDATE "Publication" SET "State" = 'Списано', "StorageLocationID" = NULL
    WHERE "NomenclatureNumber" = NEW."PublicationNomenclatureNumber";
    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "WrittenOffPublicationsCheckInsert"
    BEFORE INSERT ON "WrittenOffPublications"
    FOR EACH ROW EXECUTE FUNCTION WrittenOffPublicationsCheckInsert();

-- Обновление записи в "Списанные издания"

CREATE FUNCTION WrittenOffPublicationsCheckUpdate()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW."PublicationNomenclatureNumber" IS DISTINCT FROM OLD."PublicationNomenclatureNumber" THEN
        RAISE EXCEPTION 'field PublicationNomenclatureNumber cannot be changed';
    END IF;

    IF NEW."WriteOffDate" > CURRENT_DATE THEN RAISE EXCEPTION 'write off date cannot be in the future';
    END IF;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "WrittenOffPublicationsCheckUpdate"
    BEFORE UPDATE ON "WrittenOffPublications"
    FOR EACH ROW EXECUTE FUNCTION WrittenOffPublicationsCheckUpdate();

-- Удаление записи из "Списанные издания"

CREATE FUNCTION WrittenOffPublicationsDelete()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE "Publication" SET "State" = 'В наличии'
    WHERE "NomenclatureNumber" = OLD."PublicationNomenclatureNumber";
    RETURN OLD;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "WrittenOffPublicationsDelete"
    AFTER DELETE ON "WrittenOffPublications"
    FOR EACH ROW EXECUTE FUNCTION WrittenOffPublicationsDelete();

-- Изменение записи в "Издания"

CREATE FUNCTION PublicationCheckUpdate()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW."State" = 'Списано' AND NEW."StorageLocationID" IS NOT NULL THEN RAISE EXCEPTION
        'field StorageLocationID cannot be changed while publication is written off';
    END IF;

    IF NEW."ReceiptDate" > CURRENT_DATE THEN RAISE EXCEPTION 'receipt date cannot be in the future';
    END IF;

    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "PublicationCheckUpdate"
    BEFORE UPDATE ON "Publication"
    FOR EACH ROW EXECUTE FUNCTION PublicationCheckUpdate();

-- Вставка записи в "Издания"

CREATE FUNCTION PublicationCheckInsert()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW."ReceiptDate" > CURRENT_DATE THEN RAISE EXCEPTION 'receipt date cannot be in the future';
    END IF;
    RETURN NEW;
END
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER "PublicationCheckInsert"
    BEFORE INSERT ON "Publication"
    FOR EACH ROW EXECUTE FUNCTION PublicationCheckInsert();

-- Заполнение таблиц

INSERT INTO "Library" ("Name", "Address")
VALUES
    ('Государственная публичная научно-техническая библиотека СО РАН', 'ул. Восход, 15'),
    ('Новосибирская областная молодёжная библиотека', 'ул. Красный проспект, 26'),
    ('Новосибирская государственная областная научная библиотека', 'ул. Советская, 6'),
    ('Областная детская библиотека им. А.М. Горького', 'ул. Некрасова, 84'),
    ('Центральная городская библиотека им. К. Маркса', 'ул. Красный Проспект, 163'),
    ('Центральная районная библиотека им. П.П. Бажова Ленинского района', 'ул. Новогодняя, 11');

INSERT INTO "ReaderCategory" ("Name")
VALUES
    ('Студент'),
    ('Преподаватель'),
    ('Школьник'),
    ('Пенсионер'),
    ('Научный сотрудник');

INSERT INTO "Reader" ("Surname", "Name", "Patronymic", "BirthDay", "CategoryID")
VALUES
    ('Тимофеева', 'Ирина', 'Викторовна', TO_DATE('25.05.1983', 'DD.MM.YYYY'), NULL),
    ('Фролов', 'Евгений', 'Дмитриевич', TO_DATE('17.09.1970', 'DD.MM.YYYY'), NULL),
    ('Калинина', 'Дарья', 'Алексеевна', TO_DATE('01.02.2012', 'DD.MM.YYYY'), NULL),
    ('Юдин', 'Андрей', 'Андреевич', TO_DATE('03.03.2009', 'DD.MM.YYYY'), NULL),
    ('Вишняков', 'Виктор', 'Петрович', TO_DATE('05.07.1991', 'DD.MM.YYYY'), NULL),
    ('Лебедев', 'Артем', 'Семенович', TO_DATE('15.02.2013', 'DD.MM.YYYY'), NULL),
    ('Смирнов', 'Алексей', 'Петрович', TO_DATE('12.11.1995', 'DD.MM.YYYY'), NULL),
    ('Васильева', 'Ольга', 'Игоревна', TO_DATE('05.07.1988', 'DD.MM.YYYY'), NULL),
    ('Кузнецов', 'Дмитрий', 'Сергеевич', TO_DATE('30.03.2005', 'DD.MM.YYYY'), NULL),
    ('Морозова', 'Анна', 'Владимировна', TO_DATE('14.08.1976', 'DD.MM.YYYY'), NULL),
    ('Петров', 'Иван', 'Андреевич', TO_DATE('22.12.1999', 'DD.MM.YYYY'), NULL),
    ('Захарова', 'Екатерина', 'Артемовна', TO_DATE('09.06.2010', 'DD.MM.YYYY'), NULL),
    ('Соколов', 'Николай', 'Евгеньевич', TO_DATE('27.04.1982', 'DD.MM.YYYY'), NULL),
    ('Федорова', 'Марина', 'Александровна', TO_DATE('19.01.1993', 'DD.MM.YYYY'), NULL),
    ('Иванов', 'Сергей', 'Михайлович', TO_DATE('07.10.1965', 'DD.MM.YYYY'), NULL),
    ('Попова', 'Елена', 'Юрьевна', TO_DATE('15.03.2000', 'DD.MM.YYYY'), NULL),
    ('Андреев', 'Виктор', 'Иванович', TO_DATE('11.04.1978', 'DD.MM.YYYY'), NULL),
    ('Григорьева', 'Наталья', 'Сергеевна', TO_DATE('23.09.1990', 'DD.MM.YYYY'), NULL),
    ('Михайлов', 'Артем', 'Владиславович', TO_DATE('05.12.1985', 'DD.MM.YYYY'), NULL),
    ('Романова', 'Татьяна', 'Павловна', TO_DATE('30.06.2003', 'DD.MM.YYYY'), NULL),
    ('Беляев', 'Олег', 'Денисович', TO_DATE('17.02.1973', 'DD.MM.YYYY'), NULL),
    ('Киселева', 'Вера', 'Олеговна', TO_DATE('08.08.1998', 'DD.MM.YYYY'), NULL),
    ('Тихонов', 'Игорь', 'Анатольевич', TO_DATE('25.05.2007', 'DD.MM.YYYY'), NULL),
    ('Тимощук', 'Анастасия', 'Алексеевна', TO_DATE('29.09.2003', 'DD.MM.YYYY'), NULL),
    ('Кузнецов', 'Иван', 'Алексеевич', TO_DATE('14.08.1990', 'DD.MM.YYYY'), NULL),
    ('Попова', 'Мария', 'Сергеевна', TO_DATE('22.04.1985', 'DD.MM.YYYY'), NULL),
    ('Морозов', 'Дмитрий', 'Владимирович', TO_DATE('30.12.2000', 'DD.MM.YYYY'), NULL),
    ('Новикова', 'Анна', 'Павловна', TO_DATE('11.06.1964', 'DD.MM.YYYY'), NULL),
    ('Соловьев', 'Егор', 'Михайлович', TO_DATE('09.09.1982', 'DD.MM.YYYY'), NULL),
    ('Петрова', 'Екатерина', 'Олеговна', TO_DATE('27.02.1975', 'DD.MM.YYYY'), NULL),
    ('Федоров', 'Николай', 'Андреевич', TO_DATE('05.01.1959', 'DD.MM.YYYY'), NULL),
    ('Воробьева', 'Юлия', 'Анатольевна', TO_DATE('18.07.2003', 'DD.MM.YYYY'), NULL),
    ('Белов', 'Александр', 'Григорьевич', TO_DATE('21.10.2010', 'DD.MM.YYYY'), NULL),
    ('Гордеева', 'Наталья', 'Викторовна', TO_DATE('06.05.1980', 'DD.MM.YYYY'), NULL),
    ('Макаров', 'Сергей', 'Денисович', TO_DATE('15.09.1978', 'DD.MM.YYYY'), NULL),
    ('Зайцева', 'Оксана', 'Игоревна', TO_DATE('08.03.1989', 'DD.MM.YYYY'), NULL),
    ('Тихонов', 'Роман', 'Сергеевич', TO_DATE('02.11.1959', 'DD.MM.YYYY'), NULL),
    ('Савельева', 'Людмила', 'Александровна', TO_DATE('19.12.1971', 'DD.MM.YYYY'), NULL),
    ('Орлов', 'Владимир', 'Петрович', TO_DATE('25.06.2005', 'DD.MM.YYYY'), NULL),
    ('Андреева', 'Валентина', 'Геннадьевна', TO_DATE('03.04.1957', 'DD.MM.YYYY'), NULL),
    ('Рябов', 'Константин', 'Юрьевич', TO_DATE('10.08.2012', 'DD.MM.YYYY'), NULL),
    ('Киселева', 'Татьяна', 'Борисовна', TO_DATE('07.07.2001', 'DD.MM.YYYY'), NULL),
    ('Гаврилов', 'Игорь', 'Станиславович', TO_DATE('28.02.2009', 'DD.MM.YYYY'), NULL),
    ('Щербакова', 'Елена', 'Михайловна', TO_DATE('12.05.1963', 'DD.MM.YYYY'), NULL);

INSERT INTO "LiteraryWork" ("Title", "Author", "WritingYear", "Category")
VALUES
    ('Сто лет одиночества', 'Габриэль Гарсиа Маркес', 1967, 'Роман'),
    ('451 градус по Фаренгейту', 'Рэй Брэдбери', 1953 , 'Роман'),
    ('Поющие в терновнике', 'Колин Маккалоу', 1977 , 'Роман'),
    ('Мастер и Маргарита', 'Михаил Булгаков', 1940 , 'Роман'),
    ('Гиперфокус. Как я научился делать больше, тратя меньше времени', 'Крис Бэйли', 2013 , 'Литература по саморазвитию'),
    ('Зачем мы спим. Новая наука о сне и сновидениях', 'Мэттью Уолкер', 2017 , 'Литература по саморазвитию'),
    ('Хоббит, или Туда и Обратно', 'Джон Рональд Руэл Толкин', 1937 , 'Сказка'),
    ('Маленькие женщины', 'Луиза Мэй Олкотт', 1868 , 'Роман'),
    ('Приключения Тома Сойера', 'Марк Твен', 1884 , 'Роман'),
    ('Двадцать тысяч лье под водой', 'Жюль Верн', 1870 , 'Роман'),
    ('Дети капитана Гранта', 'Жюль Верн', 1867 , 'Роман'),
    ('Таинственный остров', 'Жюль Верн', 1874 , 'Роман'),
    ('Преступление и наказание', 'Федор Достоевский', 1866, 'Роман'),
    ('Анна Каренина', 'Лев Толстой', 1877, 'Роман'),
    ('Гордость и предубеждение', 'Джейн Остин', 1813, 'Роман'),
    ('Белая гвардия', 'Михаил Булгаков', 1925, 'Роман'),
    ('Алые паруса', 'Александр Грин', 1923, 'Повесть'),
    ('Записки охотника', 'Иван Тургенев', 1852, 'Повесть'),
    ('Путешествие из Петербурга в Москву', 'Александр Радищев', 1790, 'Роман'),
    ('О природе вещей', 'Тит Лукреций Кар', NULL, 'Поэма'),
    ('История государства Российского', 'Николай Карамзин', 1818, 'Учебник'),
    ('Физика. 10 класс', 'А. В. Перышкин', 2005, 'Учебник'),
    ('Евгений Онегин', 'Александр Пушкин', 1833, 'Стихотворение'),
    ('Бородино', 'Михаил Лермонтов', 1837, 'Стихотворение'),
    ('Исследование методов машинного обучения', 'Иван Сидоров', 2020, 'Диссертация'),
    ('Теория игр и ее применение', 'Алексей Смирнов', 2018, 'Диссертация'),
    ('Современные тенденции в программировании', 'Петр Иванов', 2021, 'Реферат'),
    ('Влияние сна на продуктивность', 'Мария Козлова', 2019, 'Тезис доклада'),
    ('Атомные привычки', 'Джеймс Клир', 2018, 'Литература по саморазвитию'),
    ('Как завоевывать друзей и оказывать влияние на людей', 'Дейл Карнеги', 1936, 'Литература по саморазвитию'),
    ('Питер Пэн', 'Джеймс Барри', 1904, 'Сказка'),
    ('Золушка', 'Шарль Перро', 1697, 'Сказка'),
    ('Робин Гуд', 'Говард Пайл', 1883, 'Сказка'),
    ('Мифы Древней Греции', 'Николай Кун', 1914, 'Мифология'),
    ('Алиса в Стране чудес', 'Льюис Кэрролл', 1865, 'Сказка'),
    ('1984', 'Джордж Оруэлл', 1949, 'Роман'),
    ('Метро 2033', 'Дмитрий Глуховский', 2005, 'Роман'),
    ('Основы философии', 'Владимир Степин', 2000, 'Учебник'),
    ('Бог как иллюзия', 'Ричард Докинз', 2006, 'Научно-популярная литература');

INSERT INTO "PublicationStorageLocation" ("LibraryID", "RoomNumber", "ShelvingNumber", "ShelfNumber")
VALUES
    (1, 1, 1, 1),
    (1, 1, 1, 2),
    (1, 1, 1, 3),
    (1, 1, 1, 4),
    (1, 1, 1, 5),
    (1, 1, 2, 1),
    (1, 1, 2, 2),
    (1, 1, 2, 3),
    (1, 1, 2, 4),
    (1, 1, 2, 5),
    (1, 1, 3, 1),
    (1, 1, 3, 2),
    (1, 1, 3, 3),
    (1, 1, 3, 4),
    (1, 1, 3, 5),
    (1, 2, 1, 1),
    (1, 2, 1, 2),
    (1, 2, 1, 3),
    (1, 2, 1, 4),
    (1, 2, 1, 5),
    (1, 2, 2, 1),
    (1, 2, 2, 2),
    (1, 2, 2, 3),
    (1, 2, 2, 4),
    (1, 2, 2, 5),
    (1, 2, 3, 1),
    (1, 2, 3, 2),
    (1, 2, 3, 3),
    (1, 2, 3, 4),
    (1, 2, 3, 5),
    (2, 1, 1, 1),
    (2, 1, 1, 2),
    (2, 1, 1, 3),
    (2, 1, 1, 4),
    (2, 1, 1, 5),
    (2, 1, 2, 1),
    (2, 1, 2, 2),
    (2, 1, 2, 3),
    (2, 1, 2, 4),
    (2, 1, 2, 5),
    (2, 1, 3, 1),
    (2, 1, 3, 2),
    (2, 1, 3, 3),
    (2, 1, 3, 4),
    (2, 1, 3, 5),
    (3, 1, 1, 1),
    (3, 1, 1, 2),
    (3, 1, 1, 3),
    (3, 1, 2, 1),
    (3, 1, 2, 2),
    (3, 1, 2, 3),
    (3, 1, 3, 1),
    (3, 1, 3, 2),
    (3, 1, 3, 3),
    (3, 2, 1, 1),
    (3, 2, 1, 2),
    (3, 2, 1, 3),
    (3, 2, 2, 1),
    (3, 2, 2, 2),
    (3, 2, 2, 3),
    (3, 2, 3, 1),
    (3, 2, 3, 2),
    (3, 2, 3, 3),
    (4, 1, 1, 1),
    (4, 1, 1, 2),
    (4, 1, 1, 3),
    (4, 1, 2, 1),
    (4, 1, 2, 2),
    (4, 1, 2, 3),
    (4, 1, 3, 1),
    (4, 1, 3, 2),
    (4, 1, 3, 3),
    (5, 1, 1, 1),
    (5, 1, 1, 2),
    (5, 1, 1, 3),
    (5, 1, 2, 1),
    (5, 1, 2, 2),
    (5, 1, 2, 3),
    (5, 1, 3, 1),
    (5, 1, 3, 2),
    (5, 1, 3, 3),
    (6, 1, 1, 1),
    (6, 1, 1, 2),
    (6, 1, 1, 3),
    (6, 1, 2, 1),
    (6, 1, 2, 2),
    (6, 1, 2, 3),
    (6, 1, 3, 1),
    (6, 1, 3, 2),
    (6, 1, 3, 3);

INSERT INTO "Publication" ("Title", "Publisher", "ReceiptDate", "YearOfPrinting", "Category", "AgeRestriction", "StorageLocationID", "State", "PermissionToIssue", "DaysForReturn")
VALUES
    ('Всё в одном томе. Двадцать тысяч лье под водой. Дети капитана Гранта. Таинственный остров', 'Издательство АСТ',
     TO_DATE('11.12.2021', 'DD.MM.YYYY'), 2020, 'Книга', 12, 12, 'В наличии', TRUE, 14),
    ('Приключения Тома Сойера', 'ФТМ', TO_DATE('01.05.1994', 'DD.MM.YYYY'), 1994, 'Книга', 6, 12, 'В наличии', TRUE,
     14),
    ('Приключения Тома Сойера', 'Самовар', TO_DATE('03.09.2015', 'DD.MM.YYYY'), 2009, 'Книга', 6, 33, 'В наличии', TRUE,
     14),
    ('Двадцать тысяч лье под водой', 'Издательство АСТ', TO_DATE('13.05.2010', 'DD.MM.YYYY'), 2009, 'Книга', 12, 24,
     'В наличии', TRUE, 14),
    ('Хоббит, или Туда и Обратно', 'Издательство АСТ', TO_DATE('29.03.2020', 'DD.MM.YYYY'), 2017, 'Книга', 6, 53,
     'В наличии', TRUE, 14),
    ('Маленькие женщины', 'Эксмо', TO_DATE('29.09.2022', 'DD.MM.YYYY'), 2022, 'Книга', 16, 17, 'В наличии', TRUE, 14),
    ('«Наука и жизнь» №10 (октябрь 2023)', 'Наука и жизнь', TO_DATE('27.12.2023', 'DD.MM.YYYY'), 2023, 'Журнал', 12, 5,
     'В наличии', TRUE, 14),
    ('ВОКРУГ СВЕТА №2 (март 2025)', 'ООО "Шкулёв Медиа Холдинг"', TO_DATE('07.03.2025', 'DD.MM.YYYY'), 2025, 'Журнал',
     12, 8, 'В наличии', TRUE, 14),
    ('Исследование квантовой механики', 'Университетское издательство', TO_DATE('30.11.2020', 'DD.MM.YYYY'), 2020,
     'Диссертация', 18, 9, 'В наличии', FALSE, 14),
    ('Основы биохимии', 'Издательство Академия', TO_DATE('12.10.2021', 'DD.MM.YYYY'), 2021, 'Реферат', 14, 6,
     'В наличии', FALSE, 7),
    ('Современные технологии в машиностроении', 'Техническое издательство', TO_DATE('22.05.2018', 'DD.MM.YYYY'), 2018,
     'Сборник докладов', 16, 11, 'В наличии', FALSE, 7),
    ('Тезисы научной конференции 2022', 'Издательство Университет', TO_DATE('05.09.2022', 'DD.MM.YYYY'), 2022,
     'Сборник тезисов докладов', 16, 13, 'В наличии', FALSE, 7),
    ('Актуальные вопросы наследственного права: Крашенинников, Миронов, Гонгало', 'Статут',
     TO_DATE('03.06.2020', 'DD.MM.YYYY'), 2016, 'Сборник статей', 16, 67, 'В наличии', FALSE, 7),
    ('Защита гражданских прав. Избранные аспекты', 'Статут',
     TO_DATE('30.06.2020', 'DD.MM.YYYY'), 2017, 'Сборник статей', 16, 33, 'В наличии', FALSE, 7),
    ('Соотношение императивных и диспозитивных начал в корпоративном праве', 'Статут',
     TO_DATE('30.06.2020', 'DD.MM.YYYY'), 2017, 'Сборник статей', 16, 33, 'В наличии', FALSE, 7),
    ('Статуарность и тектоника в образах литературы и искусства. Статьи разных лет', 'Статут',
     TO_DATE('19.02.2022', 'DD.MM.YYYY'), 2016, 'Сборник статей', 16, 62, 'В наличии', FALSE, 7),
    ('Мастер и Маргарита', 'Азбука', TO_DATE('12.08.2016', 'DD.MM.YYYY'), 2014, 'Книга', 16, 42, 'В наличии', TRUE, 14),
    ('Сто лет одиночества', 'Издательство АСТ', TO_DATE('13.02.2015', 'DD.MM.YYYY'), 2007, 'Книга', 16, 10,
     'В наличии', TRUE, 14),
    ('Анна Каренина', 'Издательство АСТ', TO_DATE('05.07.2010', 'DD.MM.YYYY'), 2015, 'Книга', 12, 17,
     'В наличии', TRUE, 14),
    ('Алые паруса', 'Юнацтва', TO_DATE('17.02.2007', 'DD.MM.YYYY'), 1988, 'Книга', NULL, 31, 'В наличии', TRUE, 14);

INSERT INTO "PublicationPermissionToIssue" ("PublicationNomenclatureNumber", "ReaderCategoryID")
VALUES
    (9, 2),
    (9, 5),
    (10, 1),
    (10, 2),
    (10, 5),
    (11, 1),
    (11, 2),
    (11, 5),
    (12, 2),
    (12, 5),
    (13, 1),
    (13, 2),
    (13, 5),
    (14, 1),
    (14, 2),
    (14, 5),
    (15, 1),
    (15, 2),
    (15, 5),
    (16, 1),
    (16, 2),
    (16, 5);

INSERT INTO "Librarian" ("Surname", "Name", "Patronymic", "BirthDay", "PhoneNumber", "LibraryID", "RoomNumber")
VALUES
    ('Иванов', 'Алексей', 'Петрович', TO_DATE('15.06.1985', 'DD.MM.YYYY'), '+79161569883', 1, 1),
    ('Петрова', 'Мария', 'Сергеевна', TO_DATE('22.09.1990', 'DD.MM.YYYY'), '+79166549568', 1, 2),
    ('Сидоров', 'Дмитрий', 'Алексеевич', TO_DATE('03.12.1982', 'DD.MM.YYYY'), '+79516734569', 2, 1),
    ('Кузнецова', 'Елена', 'Владимировна', TO_DATE('18.07.1995', 'DD.MM.YYYY'), '+79178364570', 3, 1),
    ('Васильев', 'Игорь', 'Николаевич', TO_DATE('10.04.1988', 'DD.MM.YYYY'), '+79186792571', 3, 2),
    ('Морозов', 'Павел', 'Григорьевич', TO_DATE('05.02.1979', 'DD.MM.YYYY'), '+79168954572', 4, 1),
    ('Смирнова', 'Ольга', 'Александровна', TO_DATE('12.11.1986', 'DD.MM.YYYY'), '+79101984573', 5, 1),
    ('Егорова', 'Анна', 'Павловна', TO_DATE('30.08.1998', 'DD.MM.YYYY'), '+79167801575', 6, 1);

INSERT INTO "StudentInformation" ("LibraryCardNumber", "EducationalInstitutionName", "Faculty", "Course",
                                  "GroupNumber", "StudentCardNumber", "ExtensionDate")
VALUES
    (24, 'НГУ', 'Факультет информационных технологий', 3, 22201, 54778634, TO_DATE('30.06.2026', 'DD.MM.YYYY')),
    (20, 'НГТУ', 'Факультет гуманитарного образования', 4, 21515, 57383884, TO_DATE('01.02.2025', 'DD.MM.YYYY')),
    (9, 'СИБГУТИ', 'Инженерно-экономический факультет', 1, 24321, 43477467, TO_DATE('30.06.2025', 'DD.MM.YYYY')),
    (16, 'НГУ', 'Факультет иностранных языков', 4, 21302, 38724328, TO_DATE('30.06.2025', 'DD.MM.YYYY')),
    (7, 'НГУ', 'Физический факультет', 4, 21802, 38477891, TO_DATE('30.06.2025', 'DD.MM.YYYY'));

INSERT INTO "LecturerInformation" ("LibraryCardNumber", "EducationalInstitutionName", "JobTitle")
VALUES
    (1, 'Новосибирский Государственный Университет', 'Преподаватель по дисциплине "Математический анализ"'),
    (2, 'Новосибирский Государственный Университет', 'Заведующий кафедрой общей информатики ФИТ'),
    (8, 'Новосибирский Государственный Педагогический Университет', 'Преподаватель по дисциплине "Социология"'),
    (10, 'Высший Колледж Информатики', 'Старший преподаватель кафедры информатики'),
    (17, 'Новосибирский Государственный Университет Архитектуры, Дизайна и Искусств',
     'Старший преподаватель кафедры коммуникационного дизайна'),
    (21, 'Новосибирский Государственный Университет Архитектуры, Дизайна и Искусств',
     'Заведующий кафедрой архитектуры');

INSERT INTO "PensionerInformation" ("LibraryCardNumber", "PensionCertificateNumber")
VALUES
    (44, 87675483291),
    (40, 73845916781),
    (37, 69237865912),
    (31, 83702847910),
    (28, 73829917301);

INSERT INTO "SchoolchildInformation" ("LibraryCardNumber", "EducationalInstitutionName", "Grade", "ExtensionDate")
VALUES
    (43, 'МАОУ Гимназия №15 "Содружество"', 9, TO_DATE('01.06.2025', 'DD.MM.YYYY')),
    (4, 'МБОУ Средняя общеобразовательная школа №54', 8, TO_DATE('01.06.2024', 'DD.MM.YYYY')),
    (12, 'МАОУ Центр образования №82 "Развитие"', 8, TO_DATE('01.06.2025', 'DD.MM.YYYY')),
    (33, 'МБОУ Аэрокосмический лицей им. Ю.В. Кондратюка', 7, TO_DATE('01.06.2024', 'DD.MM.YYYY')),
    (3, 'МБОУ Средняя общеобразовательная школа №72', 6, TO_DATE('01.06.2025', 'DD.MM.YYYY')),
    (41, 'МАОУ Гимназия №1', 6, TO_DATE('01.06.2025', 'DD.MM.YYYY')),
    (6, 'МАОУ Гимназия №15 "Содружество"', 5, TO_DATE('01.06.2025', 'DD.MM.YYYY'));

INSERT INTO "ScientificWorkerInformation" ("LibraryCardNumber", "OrganizationName", "ScientificTopic")
VALUES
    (15, 'ИЯФ СО РАН', 'Физика плазмы'),
    (34, 'ФИЦ ИЦиГ СО РАН', 'Физиология'),
    (18, 'ИМ СО РАН', 'Теория алгоритмов'),
    (11, 'ИЯФ СО РАН', 'Ядерная физика'),
    (5, 'ИМ СО РАН', 'Математика');

INSERT INTO "ReaderToLibrary" ("ReaderLibraryCardNumber", "LibraryID")
VALUES
    (1,1),
    (2, 1),
    (3, 1),
    (4, 2),
    (5, 3),
    (6, 3),
    (7, 2),
    (8, 4),
    (9, 1),
    (10, 5),
    (11, 6),
    (12, 1),
    (14, 2),
    (18, 3),
    (19, 5),
    (19, 6),
    (2, 4),
    (20, 1),
    (21, 2),
    (22, 2),
    (23, 3),
    (24, 4),
    (27, 1),
    (27, 4),
    (27, 6),
    (28, 2),
    (29, 3),
    (30, 5),
    (31, 6),
    (33, 1),
    (34, 3),
    (35, 2),
    (37, 4),
    (37, 2),
    (38, 6),
    (39, 6),
    (40, 5),
    (41, 1),
    (41, 3),
    (42, 2),
    (43, 4),
    (44, 6);

INSERT INTO "PublicationToLiteraryWork" ("LiteraryWorkID", "PublicationNomenclatureNumber")
VALUES
    (10, 1),
    (11, 1),
    (12, 1),
    (9, 2),
    (9, 3),
    (10, 4),
    (7, 5),
    (8, 6),
    (4, 17),
    (1, 18),
    (14, 19),
    (17, 20);

INSERT INTO "HistoryOfIssueOfPublications" ("PublicationNomenclatureNumber", "LibraryCardNumber", "IssueDate", "ReturnDate", "LibrarianID")
VALUES
    (5, 12, TO_DATE('05.12.2024', 'DD.MM.YYYY'), TO_DATE('17.12.2024', 'DD.MM.YYYY'), 4),
    (12, 10, TO_DATE('10.12.2024', 'DD.MM.YYYY'), TO_DATE('17.12.2024', 'DD.MM.YYYY'), 1),
    (13, 9, TO_DATE('10.12.2024', 'DD.MM.YYYY'), TO_DATE('16.12.2024', 'DD.MM.YYYY'), 6),
    (10, 21, TO_DATE('13.12.2024', 'DD.MM.YYYY'), TO_DATE('20.12.2024', 'DD.MM.YYYY'), 1),
    (11, 10, TO_DATE('10.01.2025', 'DD.MM.YYYY'), TO_DATE('17.01.2025', 'DD.MM.YYYY'), 1),
    (2, 20, TO_DATE('12.01.2025', 'DD.MM.YYYY'), TO_DATE('25.01.2025', 'DD.MM.YYYY'), 1),
    (2, 35, TO_DATE('26.01.2025', 'DD.MM.YYYY'), TO_DATE('10.02.2025', 'DD.MM.YYYY'), 1),
    (1, 35, TO_DATE('26.01.2025', 'DD.MM.YYYY'), TO_DATE('10.02.2025', 'DD.MM.YYYY'), 1),
    (5, 23, TO_DATE('15.02.2025', 'DD.MM.YYYY'), TO_DATE('19.02.2025', 'DD.MM.YYYY'), 5),
    (16, 1, TO_DATE('15.02.2025', 'DD.MM.YYYY'), TO_DATE('22.02.2025', 'DD.MM.YYYY'), 5),
    (7, 26, TO_DATE('19.02.2025', 'DD.MM.YYYY'), NULL, 1),
    (5, 41, TO_DATE('09.03.2025', 'DD.MM.YYYY'), NULL, 4),
    (3, 29, TO_DATE('09.03.2025', 'DD.MM.YYYY'), NULL, 3);

INSERT INTO "WrittenOffPublications" ("PublicationNomenclatureNumber", "WriteOffDate")
VALUES
    (17, TO_DATE('03.12.2023', 'DD.MM.YYYY')),
    (18, TO_DATE('15.06.2024', 'DD.MM.YYYY')),
    (19, TO_DATE('12.09.2024', 'DD.MM.YYYY')),
    (20, TO_DATE('25.11.2024', 'DD.MM.YYYY')),
    (2, TO_DATE('01.03.2025', 'DD.MM.YYYY'));

-- Хранимые процедуры и функции

CREATE OR REPLACE FUNCTION "getReadersWithLiteraryWork"(literaryWorkID INT)
    RETURNS TABLE("Title" VARCHAR, "LibraryCardNumber" INT, "Surname" VARCHAR, "Name" VARCHAR,
                  "Patronymic" VARCHAR, "PublicationNomenclatureNumber" INT, "IssueDate" DATE) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "LiteraryWork"."Title",
            "Reader"."LibraryCardNumber",
            "Reader"."Surname",
            "Reader"."Name",
            "Reader"."Patronymic",
            "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber",
            "HistoryOfIssueOfPublications"."IssueDate"
        FROM "Reader"
                 JOIN "HistoryOfIssueOfPublications" ON "Reader"."LibraryCardNumber" =
                                                        "HistoryOfIssueOfPublications"."LibraryCardNumber"
                 JOIN "PublicationToLiteraryWork" ON "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber" =
                                                     "PublicationToLiteraryWork"."PublicationNomenclatureNumber"
                 JOIN "LiteraryWork" ON "PublicationToLiteraryWork"."LiteraryWorkID" = "LiteraryWork"."ID"
        WHERE "LiteraryWork"."ID" = literaryWorkID AND "HistoryOfIssueOfPublications"."ReturnDate" IS NULL;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getReadersWithPublication"(publicationNomenclatureNumber INT)
    RETURNS TABLE("LibraryCardNumber" INT, "Surname" VARCHAR, "Name" VARCHAR, "Patronymic" VARCHAR, "IssueDate" DATE)
    AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Reader"."LibraryCardNumber",
            "Reader"."Surname",
            "Reader"."Name",
            "Reader"."Patronymic",
            "HistoryOfIssueOfPublications"."IssueDate"
        FROM "Reader"
                 JOIN "HistoryOfIssueOfPublications" ON "Reader"."LibraryCardNumber" =
                                                        "HistoryOfIssueOfPublications"."LibraryCardNumber"
        WHERE "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber" = publicationNomenclatureNumber
          AND "HistoryOfIssueOfPublications"."ReturnDate" IS NULL;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getReadersWhoReceivedLiteraryWorkDuringThePeriod"(literaryWorkID INT,
                                                                              startDate DATE, endDate DATE)
    RETURNS TABLE("LiteraryWorkTitle" VARCHAR, "PublicationTitle" VARCHAR, "LibraryCardNumber" INT, "Surname" VARCHAR,
                  "Name" VARCHAR, "Patronymic" VARCHAR, "IssueDate" DATE) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "LiteraryWork"."Title",
            "Publication"."Title",
            "Reader"."LibraryCardNumber",
            "Reader"."Surname",
            "Reader"."Name",
            "Reader"."Patronymic",
            "HistoryOfIssueOfPublications"."IssueDate"
        FROM "Reader"
                 JOIN "HistoryOfIssueOfPublications" ON "Reader"."LibraryCardNumber" =
                                                        "HistoryOfIssueOfPublications"."LibraryCardNumber"
                 JOIN "Publication" ON "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber" =
                                       "Publication"."NomenclatureNumber"
                 JOIN "PublicationToLiteraryWork" ON "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber" =
                                                     "PublicationToLiteraryWork"."PublicationNomenclatureNumber"
                 JOIN "LiteraryWork" ON "PublicationToLiteraryWork"."LiteraryWorkID" = "LiteraryWork"."ID"
        WHERE "LiteraryWork"."ID" = literaryWorkID
          AND "HistoryOfIssueOfPublications"."IssueDate" >= startDate
          AND "HistoryOfIssueOfPublications"."IssueDate" <= endDate;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getTheMostPopularLiteraryWorks"(maxCount INT)
    RETURNS TABLE("LiteraryWorkID" INT, "Title" VARCHAR, "Author" VARCHAR, "ReadersBorrowCount" BIGINT) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "LiteraryWork"."ID",
            "LiteraryWork"."Title",
            "LiteraryWork"."Author",
            COUNT(*) AS "BorrowCount"
        FROM "LiteraryWork"
                 JOIN "PublicationToLiteraryWork" ON "LiteraryWork"."ID" = "PublicationToLiteraryWork"."LiteraryWorkID"
                 JOIN "HistoryOfIssueOfPublications" ON "PublicationToLiteraryWork"."PublicationNomenclatureNumber" =
                                                        "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber"
        GROUP BY "LiteraryWork"."ID", "LiteraryWork"."Title", "LiteraryWork"."Author"
        ORDER BY "BorrowCount" DESC LIMIT maxCount;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getPublicationsWithTheAuthorsWorks"(author VARCHAR)
    RETURNS TABLE("PublicationNomenclatureNumber" INT, "Title" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Publication"."NomenclatureNumber",
            "Publication"."Title"
        FROM "Publication"
                 JOIN "PublicationToLiteraryWork" ON "Publication"."NomenclatureNumber" =
                                                     "PublicationToLiteraryWork"."PublicationNomenclatureNumber"
                 JOIN "LiteraryWork" ON "PublicationToLiteraryWork"."LiteraryWorkID" = "LiteraryWork"."ID"
        WHERE "LiteraryWork"."Author" ILIKE '%' || author || '%'
        GROUP BY "Publication"."NomenclatureNumber", "Publication"."Title";
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getPublicationsWithLiteraryWork"(literaryWorkID INT)
    RETURNS TABLE("PublicationNomenclatureNumber" INT, "Title" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Publication"."NomenclatureNumber",
            "Publication"."Title"
        FROM "Publication"
                 JOIN "PublicationToLiteraryWork" ON "Publication"."NomenclatureNumber" =
                                                     "PublicationToLiteraryWork"."PublicationNomenclatureNumber"
        WHERE "PublicationToLiteraryWork"."LiteraryWorkID" = literaryWorkID;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getReadersWhoHaveNotVisitedTheLibraryDuringThePeriod"(startDate DATE, endDate DATE)
    RETURNS TABLE("LibraryCardNumber" INT, "Surname" VARCHAR, "Name" VARCHAR, "Patronymic" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Reader"."LibraryCardNumber",
            "Reader"."Surname",
            "Reader"."Name",
            "Reader"."Patronymic"
        FROM "Reader"
        WHERE "Reader"."LibraryCardNumber" NOT IN (
            SELECT DISTINCT "HistoryOfIssueOfPublications"."LibraryCardNumber"
            FROM "HistoryOfIssueOfPublications"
            WHERE "IssueDate" BETWEEN startDate AND endDate
               OR "ReturnDate" BETWEEN startDate AND endDate
        );

END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getLibrariansWhoWorksInTheLibraryRoom"(libraryID INT, roomNumber INT)
    RETURNS TABLE("LibrarianID" INT, "Surname" VARCHAR, "Name" VARCHAR, "Patronymic" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Librarian"."LibrarianID",
            "Librarian"."Surname",
            "Librarian"."Name",
            "Librarian"."Patronymic"
        FROM "Librarian"
        WHERE "Librarian"."LibraryID" = libraryID AND "Librarian"."RoomNumber" = roomNumber;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getPublicationsThatWereReceiptOrWrittenOffDuringThePeriod"(startDate DATE, endDate DATE)
    RETURNS TABLE("PublicationNomenclatureNumber" INT, "Title" VARCHAR, "ReceiptDate" DATE, "WriteOffDate" DATE) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Publication"."NomenclatureNumber",
            "Publication"."Title",
            "Publication"."ReceiptDate",
            "WrittenOffPublications"."WriteOffDate"
        FROM "Publication"
                 FULL JOIN "WrittenOffPublications" ON "Publication"."NomenclatureNumber" =
                                                       "WrittenOffPublications"."PublicationNomenclatureNumber"
        WHERE "Publication"."ReceiptDate" BETWEEN startDate AND endDate OR
            "WrittenOffPublications"."WriteOffDate" BETWEEN startDate AND endDate;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getReadersWithExpiredPublications"()
    RETURNS TABLE("LibraryCardNumber" INT, "Surname" VARCHAR, "Name" VARCHAR, "Patronymic" VARCHAR,
                  "PublicationNomenclatureNumber" INT, "DaysOverdue" INT) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Reader"."LibraryCardNumber",
            "Reader"."Surname",
            "Reader"."Name",
            "Reader"."Patronymic",
            "Publication"."NomenclatureNumber",
            EXTRACT(DAY FROM (CURRENT_DATE - ("HistoryOfIssueOfPublications"."IssueDate" + "Publication"
                                                                                               ."DaysForReturn" * INTERVAL '1 day')))::INT AS "DaysOverdue"
        FROM "Reader"
                 JOIN "HistoryOfIssueOfPublications" ON "Reader"."LibraryCardNumber" =
                                                        "HistoryOfIssueOfPublications"."LibraryCardNumber"
                 JOIN "Publication" ON "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber" =
                                       "Publication"."NomenclatureNumber"
        WHERE "HistoryOfIssueOfPublications"."ReturnDate" IS NULL
          AND "HistoryOfIssueOfPublications"."IssueDate" + "Publication"."DaysForReturn" * INTERVAL '1 day' <
              CURRENT_DATE;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getDataOnLibrariansProductivityDuringThePeriod"(startDate DATE, endDate DATE)
    RETURNS TABLE("LibrarianID" INT, "Surname" VARCHAR, "Name" VARCHAR, "Patronymic" VARCHAR,
                  "NumberOfServedReaders" BIGINT) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Librarian"."LibrarianID",
            "Librarian"."Surname",
            "Librarian"."Name",
            "Librarian"."Patronymic",
            COUNT(*) AS "NumberOfServedReaders"

        FROM "Librarian"
                 JOIN "HistoryOfIssueOfPublications" ON "Librarian"."LibrarianID" = "HistoryOfIssueOfPublications"."LibrarianID"
        WHERE "IssueDate" BETWEEN startDate AND endDate
        GROUP BY "Librarian"."LibrarianID", "Librarian"."Surname", "Librarian"."Name", "Librarian"."Patronymic";
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getReadersWhoWereServedByTheLibrarianDuringThePeriod"(librarianID INT, startDate DATE,
                                                                                  endDate DATE)
    RETURNS TABLE("LibraryCardNumber" INT, "Surname" VARCHAR, "Name" VARCHAR, "Patronymic" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Reader"."LibraryCardNumber",
            "Reader"."Surname",
            "Reader"."Name",
            "Reader"."Patronymic"
        FROM "Reader"
                 JOIN "HistoryOfIssueOfPublications" ON "Reader"."LibraryCardNumber" =
                                                        "HistoryOfIssueOfPublications"."LibraryCardNumber"
        WHERE "HistoryOfIssueOfPublications"."LibrarianID" = librarianID
          AND "IssueDate" BETWEEN startDate AND endDate
        GROUP BY "Reader"."LibraryCardNumber";
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getPublicationsThatIssuedFromTheSpecificStorageLocation"(libraryID INT, roomNumber INT,
                                                                                     shelvingNumber INT, shelfNumber INT)
    RETURNS TABLE("PublicationNomenclatureNumber" INT, "Title" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Publication"."NomenclatureNumber",
            "Publication"."Title"
        FROM "Publication"
                 JOIN "PublicationStorageLocation" ON "Publication"."StorageLocationID" =
                                                      "PublicationStorageLocation"."StorageLocationID"
        WHERE "PublicationStorageLocation"."LibraryID" = libraryID
          AND "PublicationStorageLocation"."RoomNumber" = roomNumber
          AND "PublicationStorageLocation"."ShelvingNumber" = shelvingNumber
          AND "PublicationStorageLocation"."ShelfNumber" = shelfNumber
          AND "Publication"."State" = 'Выдано';
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getPublicationsThatIssuedFromLibraryWhereReaderIsNotRegistered"(libraryCardNumber INT)
    RETURNS TABLE("PublicationNomenclatureNumber" INT, "Title" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Publication"."NomenclatureNumber",
            "Publication"."Title"
        FROM "Publication"
                 JOIN "PublicationStorageLocation" ON "Publication"."StorageLocationID" =
                                                      "PublicationStorageLocation"."StorageLocationID"
                 JOIN "HistoryOfIssueOfPublications" ON "Publication"."NomenclatureNumber" =
                                                        "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber"
        WHERE "LibraryCardNumber" = libraryCardNumber
          AND "PublicationStorageLocation"."LibraryID" NOT IN (
            SELECT "ReaderToLibrary"."LibraryID"
            FROM "ReaderToLibrary"
            WHERE "ReaderLibraryCardNumber" = libraryCardNumber);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getPublicationsThatIssuedFromLibraryWhereReaderIsRegistered"(libraryCardNumber INT)
    RETURNS TABLE("PublicationNomenclatureNumber" INT, "Title" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT
            "Publication"."NomenclatureNumber",
            "Publication"."Title"
        FROM "Publication"
                 JOIN "PublicationStorageLocation" ON "Publication"."StorageLocationID" =
                                                      "PublicationStorageLocation"."StorageLocationID"
                 JOIN "HistoryOfIssueOfPublications" ON "Publication"."NomenclatureNumber" =
                                                        "HistoryOfIssueOfPublications"."PublicationNomenclatureNumber"
        WHERE "LibraryCardNumber" = libraryCardNumber
          AND "PublicationStorageLocation"."LibraryID" IN (
            SELECT "ReaderToLibrary"."LibraryID"
            FROM "ReaderToLibrary"
            WHERE "ReaderLibraryCardNumber" = libraryCardNumber);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getStudentsWithCharacteristics"(educationalInstitutionName VARCHAR, faculty VARCHAR,
                                                            course INT)
    RETURNS TABLE("StudentID" INT, "LibraryCardNumber" INT, "EducationalInstitutionName" VARCHAR, "Faculty" VARCHAR,
                  "Course" INT, "GroupNumber" INT, "StudentCardNumber" INT, "ExtensionDate" DATE) AS $$
BEGIN
    RETURN QUERY
        SELECT *
        FROM "StudentInformation"
        WHERE (educationalInstitutionName IS NULL OR "StudentInformation"."EducationalInstitutionName" ILIKE
                                                     CONCAT('%', educationalInstitutionName, '%'))
          AND (faculty IS NULL OR "StudentInformation"."Faculty" ILIKE
                                  CONCAT('%', faculty, '%'))
          AND (course IS NULL OR "StudentInformation"."Course" = course);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getSchoolchildrenWithCharacteristics"(educationalInstitutionName VARCHAR, grade INT)
    RETURNS TABLE("SchoolchildID" INT, "LibraryCardNumber" INT, "EducationalInstitutionName" VARCHAR, "Grade" INT,
                  "ExtensionDate" DATE) AS $$
BEGIN
    RETURN QUERY
        SELECT *
        FROM "SchoolchildInformation"
        WHERE (educationalInstitutionName IS NULL OR "SchoolchildInformation"."EducationalInstitutionName" ILIKE
                                                     CONCAT('%', educationalInstitutionName, '%'))
          AND (grade IS NULL OR "SchoolchildInformation"."Grade" = grade);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getScientificWorkersWithCharacteristics"(organizationName VARCHAR, scientificTopic VARCHAR)
    RETURNS TABLE("ScientificWorkerID" INT, "LibraryCardNumber" INT, "OrganizationName" VARCHAR, "ScientificTopic" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT *
        FROM "ScientificWorkerInformation"
        WHERE (organizationName IS NULL OR "ScientificWorkerInformation"."OrganizationName" ILIKE
                                           CONCAT('%', organizationName, '%'))
          AND (scientificTopic IS NULL OR "ScientificWorkerInformation"."ScientificTopic" ILIKE
                                          CONCAT('%', scientificTopic, '%'));
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getLecturersWithCharacteristics"(educationalInstitutionName VARCHAR, jobTitle VARCHAR)
    RETURNS TABLE("LecturerID" INT, "LibraryCardNumber" INT, "EducationalInstitutionName" VARCHAR, "JobTitle" VARCHAR) AS $$
BEGIN
    RETURN QUERY
        SELECT *
        FROM "LecturerInformation"
        WHERE (educationalInstitutionName IS NULL OR "LecturerInformation"."EducationalInstitutionName" ILIKE
                                                     CONCAT('%', educationalInstitutionName, '%'))
          AND (jobTitle IS NULL OR "LecturerInformation"."JobTitle" ILIKE CONCAT('%', jobTitle, '%'));
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION "getNumberOfDaysOverdue"(publicationNomenclatureNumber INT, returnDate DATE) RETURNS INT
    AS $$
DECLARE
    issueDate DATE;
    daysForReturn INT;
BEGIN
    SELECT "IssueDate"
    INTO issueDate
    FROM "HistoryOfIssueOfPublications"
    WHERE "PublicationNomenclatureNumber" = publicationNomenclatureNumber;

    SELECT "DaysForReturn"
    INTO daysForReturn
    FROM "Publication"
    WHERE "NomenclatureNumber" = publicationNomenclatureNumber;

    RETURN EXTRACT(DAY FROM (returnDate - (issueDate + daysForReturn * INTERVAL '1 day')))::INT;
END
$$ LANGUAGE plpgsql;

CREATE ROLE admin_fond;
CREATE ROLE admin_library;
CREATE ROLE librarian;

GRANT CONNECT ON DATABASE library TO admin_fond, admin_library, librarian;
GRANT USAGE ON SCHEMA public TO admin_fond, admin_library, librarian;

GRANT SELECT ON "Library" TO librarian, admin_library;
GRANT SELECT, INSERT, UPDATE ON "Reader" TO librarian, admin_library;
GRANT SELECT, INSERT, UPDATE, DELETE ON "StudentInformation" TO librarian, admin_library, admin_fond;
GRANT SELECT ON "LiteraryWork" TO librarian, admin_library;
GRANT SELECT, UPDATE ON "Publication" TO librarian;
GRANT SELECT ON "PublicationStorageLocation" TO librarian;
GRANT SELECT, INSERT, UPDATE ON "HistoryOfIssueOfPublications" TO librarian, admin_library;
GRANT SELECT, INSERT ON "WrittenOffPublications" TO librarian;
GRANT SELECT ON "Librarian" TO librarian;
GRANT SELECT, INSERT, UPDATE, DELETE ON "SchoolchildInformation" TO librarian, admin_library, admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "LecturerInformation" TO librarian, admin_library, admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "PensionerInformation" TO librarian, admin_library, admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "ScientificWorkerInformation" TO librarian, admin_library, admin_fond;
GRANT SELECT ON "ReaderCategory" TO librarian, admin_library;
GRANT SELECT ON "PublicationPermissionToIssue" TO librarian;
GRANT SELECT ON "PublicationToLiteraryWork" TO librarian;
GRANT SELECT, INSERT, UPDATE, DELETE ON "ReaderToLibrary" TO librarian, admin_library, admin_fond;

GRANT SELECT, INSERT, UPDATE ON "Publication" TO admin_library;
GRANT SELECT, INSERT, UPDATE, DELETE ON "PublicationStorageLocation" TO admin_library, admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "WrittenOffPublications" TO admin_library, admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "Librarian" TO admin_library, admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "PublicationPermissionToIssue" TO admin_library, admin_fond;
GRANT SELECT, INSERT, UPDATE ON "PublicationToLiteraryWork" TO admin_library;

GRANT SELECT, INSERT, UPDATE, DELETE ON "Library" TO admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "Reader" TO admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "LiteraryWork" TO admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "Publication" TO admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "HistoryOfIssueOfPublications" TO admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "ReaderCategory" TO admin_fond;
GRANT SELECT, INSERT, UPDATE, DELETE ON "PublicationToLiteraryWork" TO admin_fond;

GRANT USAGE, SELECT ON SEQUENCE "HistoryOfIssueOfPublications_IssuedPublicationID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "LecturerInformation_LecturerID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "Librarian_LibrarianID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "Library_ID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "LiteraryWork_ID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "PensionerInformation_PensionerID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "Publication_NomenclatureNumber_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "PublicationStorageLocation_StorageLocationID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "Reader_LibraryCardNumber_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "ReaderCategory_ID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "SchoolchildInformation_SchoolchildID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "ScientificWorkerInformation_ScientificWorkerID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "StudentInformation_StudentID_seq" TO admin_fond, admin_library, librarian;
GRANT USAGE, SELECT ON SEQUENCE "WrittenOffPublications_WriteOffID_seq" TO admin_fond, admin_library, librarian;

CREATE USER a_ivanov WITH PASSWORD '123';
GRANT librarian TO a_ivanov;

CREATE USER fadmin WITH PASSWORD '1';
GRANT admin_fond TO fadmin;

CREATE USER gpntb_admin WITH PASSWORD '000';
GRANT admin_library to gpntb_admin;



















