package mn.edu.must.sqat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    private static final String STUDENT_ID = "B252270135";
    private static final String COURSE_ID = "F.CSA313";
    private static final String EMAIL = "student@example.com";
    private static final String SUBJECT = "Бүртгэл баталгаажлаа";

    @Mock
    StudentRepository repository;

    @Mock
    EmailSender emailSender;

    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    // A1: Амжилттай бүртгэл нь үр дүн, хадгалалт, мэдэгдэл гурваар батлагдана.
    @Test
    void register_activeStudentWithPrerequisites_returnsOkAndSavesAndSendsEmail() {
        stubSuccessfulRegistration();

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(EMAIL, SUBJECT,
                COURSE_ID + " хичээлд амжилттай бүртгэгдлээ.");
    }

    // A2: Олдоогүй оюутны бүртгэлийг хадгалах, и-мэйл илгээх ёсгүй.
    @Test
    void register_missingStudent_returnsNoStudentWithoutSideEffects() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3: Идэвхгүй оюутны үед и-мэйл илгээгчтэй огт харьцахгүй.
    @Test
    void register_inactiveStudent_returnsInactiveWithoutEmail() {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(new Student(STUDENT_ID, EMAIL, false, List.of()));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A4: Шаардсан хоёр хичээлээс зөвхөн нэгийг үзсэн бол бүртгэхгүй.
    @Test
    void register_missingPrerequisite_returnsPrerequisitesWithoutSaving() {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(new Student(STUDENT_ID, EMAIL, true, List.of("F.CSA101")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID))
                .thenReturn(List.of("F.CSA101", "F.CSA202"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A5: Мэдэгдлийн алдаа нь амжилттай хадгалсан бүртгэлийг хүчингүй болгохгүй.
    @Test
    void register_emailSenderThrows_returnsOkAndKeepsRegistration() {
        stubSuccessfulRegistration();
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailSender).send(eq(EMAIL), anyString(), anyString());

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(EMAIL, SUBJECT,
                COURSE_ID + " хичээлд амжилттай бүртгэгдлээ.");
    }

    // A6: Хоёр аргументын null нөхцөлийг тус тусад нь болон хамтад нь шалгана.
    @Test
    void register_nullArgument_throwsBeforeUsingDependencies() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(null, COURSE_ID), "null studentId"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(STUDENT_ID, null), "null courseId"),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.register(null, null), "both arguments null")
        );
        verifyNoInteractions(repository, emailSender);
    }

    // A7: Бодитоор дамжуулсан и-мэйлийн биеийг captor-оор авч шалгана.
    @Test
    void register_successfulRegistration_emailBodyContainsCourseCode() {
        stubSuccessfulRegistration();
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(emailSender).send(eq(EMAIL), eq(SUBJECT), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID));
        assertEquals(COURSE_ID + " хичээлд амжилттай бүртгэгдлээ.", bodyCaptor.getValue());
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
    }

    // A8: Гурван мөр нь JUnit-ийн тайланд гурван тусдаа тест болж тоологдоно.
    @ParameterizedTest(name = "[{index}] {0} -> {4}")
    @CsvSource({
            "missingStudent, false, true, true, ERROR_NO_STUDENT",
            "inactiveStudent, true, false, true, ERROR_INACTIVE_STUDENT",
            "missingCourse, true, true, false, ERROR_NO_COURSE"
    })
    void register_invalidScenario_returnsExpectedError(String scenario, boolean studentExists,
            boolean active, boolean courseExists, String expectedError) {
        Student student = studentExists
                ? new Student(STUDENT_ID, EMAIL, active, List.of())
                : null;
        when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        // Эхний хоёр мөр эрт буцдаг тул нийтлэг courseExists stub ашиглагдахгүй.
        lenient().when(repository.courseExists(COURSE_ID)).thenReturn(courseExists);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals(expectedError, result, scenario);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    private void stubSuccessfulRegistration() {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(new Student(STUDENT_ID, EMAIL, true,
                        List.of("F.CSA101", "F.CSA202")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID))
                .thenReturn(List.of("F.CSA101", "F.CSA202"));
    }
}
