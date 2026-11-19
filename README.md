# Лаборатори 8 Mockito mock объектууд

**Оюутны нэр:** Э.Ундрах  
**Оюутны код:** B252270135  
**Хичээл:** F.CSA313 Программ хангамжийн чанарын баталгаа ба тест

`CourseRegistrationService.register()` методыг өгөгдлийн сан, SMTP серверээс тусгаарлан JUnit 5 болон Mockito 5-аар тестэлсэн. `StudentRepository`, `EmailSender` хоёр хамаарлыг mock-оор орлуулж, буцаах утга болон хадгалалт, мэдэгдлийн үйлдлийг шалгасан. Тест болон тайланг Codex-ийн тусламжтай бэлтгэж, тест ба мутацийн командуудыг бодитоор ажиллуулсан.

## Ажиллуулсан орчин

`java -version` командын гаралт:

```text
java version "25.0.2" 2026-01-20 LTS
Java(TM) SE Runtime Environment (build 25.0.2+10-LTS-69)
Java HotSpot(TM) 64-Bit Server VM (build 25.0.2+10-LTS-69, mixed mode, sharing)
```

`mvn -version` командын гаралт:

```text
Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)
Maven home: /opt/homebrew/Cellar/maven/3.10.0/libexec
Java version: 27.0.1.0.3, vendor: Homebrew, runtime: /opt/homebrew/Cellar/openjdk/27.0.1.0.3/libexec/openjdk.jdk/Contents/Home
Default locale: en_MN, platform encoding: UTF-8, time zone: Asia/Ulaanbaatar
OS name: "mac os x", version: "26.2", arch: "aarch64", family: "mac"
```

Төсөл Java 17-ийн `release` тохиргоотой. `pom.xml` дотор `maven-compiler-plugin`-ийг `3.13.0` хувилбараар тогтоосон тул Maven-ийн анхдагч compiler plugin-ий хувилбараас үл хамааран энэ тохиргоог ашиглана. Starter-ийн `src/main` дахь дөрвөн Java файлыг өөрчлөөгүй.

## Нэгжийн тестүүд

Тестийн класс: [`CourseRegistrationServiceTest.java`](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java).

| Хэсэг | Тестийн метод | Шалгасан нөхцөл ба баталгаа |
| --- | --- | --- |
| A1 | `register_activeStudentWithPrerequisites_returnsOkAndSavesAndSendsEmail` | Идэвхтэй, өмнөх холбоо хичээлээ үзсэн оюутанд `OK`; бүртгэлийг хадгалж, зөв хаяг, гарчиг, биетэй и-мэйл илгээнэ. |
| A2 | `register_missingStudent_returnsNoStudentWithoutSideEffects` | Оюутан олдохгүй бол `ERROR_NO_STUDENT`; `never()`-ээр хадгалалт, и-мэйл дуудлага хийгдээгүйг шалгана. |
| A3 | `register_inactiveStudent_returnsInactiveWithoutEmail` | Идэвхгүй оюутанд `ERROR_INACTIVE_STUDENT`; хадгалахгүй, `verifyNoInteractions(emailSender)` биелнэ. |
| A4 | `register_missingPrerequisite_returnsPrerequisitesWithoutSaving` | Шаардлагатай хоёр хичээлээс нэгийг нь үзсэн бол `ERROR_PREREQUISITES`; хадгалахгүй, мэдэгдэл илгээхгүй. |
| A5 | `register_emailSenderThrows_returnsOkAndKeepsRegistration` | `doThrow()`-оор и-мэйл илгээгчийн алдааг дуурайлгахад `OK` хэвээр, хадгалалт хийгдсэн байна. |
| A6 | `register_nullArgument_throwsBeforeUsingDependencies` | `studentId`, `courseId` тус бүр `null`, мөн хоёулаа `null` үед `IllegalArgumentException`; хамаарлуудтай харьцахгүй. |
| A7 | `register_successfulRegistration_emailBodyContainsCourseCode` | `ArgumentCaptor`-оор дамжуулсан и-мэйлийн биеийг авч, хичээлийн код болон бүтэн агуулгыг шалгана. |
| A8 | `register_invalidScenario_returnsExpectedError` | `@CsvSource`-ийн гурван мөрөөр байхгүй оюутан, идэвхгүй оюутан, байхгүй хичээл гэсэн алдаануудыг шалгана. |

Долоон энгийн тест болон A8-ийн гурван параметр нийлж **10 тест** ажиллана. Эцсийн ногоон ажиллуулалтын нотолгоо [`results/mvn-test.txt`](results/mvn-test.txt)-д хадгалагдсан:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

A8-д `courseExists()`-ийн нийтлэг stub нь оюутан олдоогүй болон идэвхгүй гэсэн эхний хоёр мөрийн эрт буцах замд ашиглагдахгүй. Зөвхөн тэр stub-д `lenient()` хэрэглэснээр `UnnecessaryStubbingException`-ээс сэргийлж, бусад stub-ийн strict шалгалтыг хэвээр үлдээсэн.

## Мутацийн шалгалт

Мутаци бүрийг эх хувилбарт тусад нь хийж тестийг ажиллуулаад, дараагийн мутациас өмнө кодыг сэргээсэн.

| Мутант | Түр хийсэн өөрчлөлт | Унасан тестүүд | Бодит үр дүн | Нотолгоо |
| --- | --- | --- | --- | --- |
| 1 | Идэвхгүй оюутны шалгалтыг хассан | A3 `register_inactiveStudent_returnsInactiveWithoutEmail`; A8 `register_invalidScenario_returnsExpectedError`-ийн 2 дахь мөр (`inactiveStudent`) | Tests run: 10, Failures: 2, Errors: 0, Skipped: 0; **BUILD FAILURE** | [Лог 1](results/mvn-test-mutant-1.txt) |
| 2 | `saveRegistration()` дуудалтыг хассан | A1 `register_activeStudentWithPrerequisites_returnsOkAndSavesAndSendsEmail`; A5 `register_emailSenderThrows_returnsOkAndKeepsRegistration`; A7 `register_successfulRegistration_emailBodyContainsCourseCode` | Tests run: 10, Failures: 3, Errors: 0, Skipped: 0; **BUILD FAILURE** | [Лог 2](results/mvn-test-mutant-2.txt) |
| 3 | Эхний `if`-ээс `courseId == null` шалгалтыг хассан | A6 `register_nullArgument_throwsBeforeUsingDependencies`-ийн `null courseId` баталгаа | Tests run: 10, Failures: 1, Errors: 0, Skipped: 0; **BUILD FAILURE** | [Лог 3](results/mvn-test-mutant-3.txt) |

Гурван мутаци бүгд assertion болон `verify()`-ийн зөрчлөөр илэрсэн; `UnnecessaryStubbingException` зэрэг орчны алдаагаар унаагүй. Идэвхгүй оюутны хамгаалалт, хадгалалтын дуудлага, `courseId`-ийн шалгалтыг устгахад тус бүр өөр тест унасан нь эдгээр шаардлагыг тестүүд бодитоор шалгаж байгааг харуулсан.

A6 нь анхнаасаа хоёр аргументын `null` нөхцөлийг тус тусад нь шалгасан тул гурав дахь мутацийг барихын тулд тестийг дараа нь нэмэх шаардлага гараагүй. Мутацийн дараа `src/main`-ийг бүрэн сэргээж дахин `mvn test` ажиллуулсан; [`results/mvn-test.txt`](results/mvn-test.txt) нь сэргээсэн кодын эцсийн үр дүн юм.

## Ажиллуулах

Java 17 буюу түүнээс дээш JDK болон Maven суусан орчинд төслийн үндсэн хавтсаас:

```sh
mvn test
```

Анхны ажиллуулалтад Maven хамаарлуудыг татах интернэт холболт шаардлагатай. `target/`, `.idea/`, `.DS_Store` нь `.gitignore`-д орсон; багшийн зааврын Word файл репозиторид ороогүй.

## GitHub Actions

[`.github/workflows/test.yml`](.github/workflows/test.yml) нь GitHub-д push хийх, pull request үүсэх болон гараар эхлүүлэхэд Java 17 ба 21 дээр Maven тест ажиллуулахаар тохируулагдсан. Энэ тайланд GitHub дээрх ажиллуулалтын амжилтыг батлаагүй бөгөөд алсын репозиторийн ногоон badge ашиглаагүй.

## Дүгнэлт

Mock нь хамаарлын хариуг тухайн тестэд тохируулж, түүнд ямар дуудлага очсоныг Mockito-оор шалгах боломж олгодог. Гараар бичсэн fake нь санах ойд бүртгэл хадгалах зэрэг хялбарчилсан бодит зан төлөвтэй хэрэгжүүлэлт учраас олон төлөвтэй сценарид тохиромжтой боловч өөрийн кодын арчилгаа шаарддаг. Энэ лабораторид өгөгдлийн сан болон и-мэйл зэрэг гадаад нөлөөтэй хамаарлуудыг mock болгосноор бизнес логикийг гаднын сервергүйгээр шалгасан. `Student` record болон энгийн `List` утгыг mock хийх шаардлагагүй, учир нь тэдгээрийн бодит объектыг үүсгэх нь хялбар бөгөөд тестийн өгөгдлийг тодорхой болгодог. Тестлэж буй `CourseRegistrationService`-ийг mock болговол жинхэнэ бүртгэлийн логикийг шалгахгүй тул түүнийг бодитоор үүсгэсэн. `verify()`-г дотоод хэрэгжүүлэлтийн бүх дуудлагад хэт хэрэглэвэл зан төлөв өөрчлөгдөөгүй refactor ч тестийг унагаах эрсдэлтэй учраас хадгалалт, мэдэгдэл зэрэг шаардлагатай нөлөөнд төвлөрүүлсэн. Ногоон тестийн үр дүнг гурван мутацийн алдаатай ажиллуулалттай хамт үнэлэх нь буцаах утга, хадгалалт болон оролтын шалгалтын эвдрэлийг тестүүд илрүүлж байгааг баталсан.

## Ашигласан баримт бичиг

- [Apache Maven — Java release тохиргоо](https://maven.apache.org/guides/mini/guide-java-target.html)
- [GitHub Actions checkout](https://github.com/actions/checkout)
- [GitHub Actions setup-java](https://github.com/actions/setup-java)
