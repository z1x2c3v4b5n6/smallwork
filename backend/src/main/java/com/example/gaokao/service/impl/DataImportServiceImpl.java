package com.example.gaokao.service.impl;

import com.example.gaokao.domain.*;
import com.example.gaokao.service.*;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class DataImportServiceImpl implements DataImportService {

    private static final int HEADER_ROW_INDEX = 0;
    private static final int DATA_START_ROW_INDEX = 1;

    private static final Map<String, List<String>> MAJOR_FIELD_HEADER_ALIASES = new HashMap<>();

    private static final Set<String> MAJOR_REQUIRED_FIELDS = new HashSet<>();

    private static final Map<String, List<String>> STUDENT_FIELD_HEADER_ALIASES = new HashMap<>();

    private static final Set<String> STUDENT_REQUIRED_FIELDS = new HashSet<>();

    static {
        MAJOR_FIELD_HEADER_ALIASES.put("year", Arrays.asList("年份", "year", "年度"));
        MAJOR_FIELD_HEADER_ALIASES.put("province", Arrays.asList("生源地", "生源省份", "省份", "院校省份"));
        MAJOR_FIELD_HEADER_ALIASES.put("city", Arrays.asList("院校所在地", "所在城市", "城市", "地区"));
        MAJOR_FIELD_HEADER_ALIASES.put("type", Arrays.asList("科类", "文理科", "选科要求", "选考科目"));
        MAJOR_FIELD_HEADER_ALIASES.put("batch", Arrays.asList("批次", "录取批次", "招生批次", "录取批次名称"));
        MAJOR_FIELD_HEADER_ALIASES.put("doubleTop", Arrays.asList("是否双一流", "双一流", "双一流标识", "是否985", "是否211"));
        MAJOR_FIELD_HEADER_ALIASES.put("universityName", Arrays.asList(
                "院校名称", "学校名称", "高校名称",
                "院校", "院校名", "学校", "学校名",
                "高校", "招生院校", "招生学校", "报考院校"));
        MAJOR_FIELD_HEADER_ALIASES.put("category", Arrays.asList("专业类别", "专业类", "类别", "科类", "学科门类"));
        MAJOR_FIELD_HEADER_ALIASES.put("discipline", Arrays.asList("专业方向", "方向", "学科门类"));
        MAJOR_FIELD_HEADER_ALIASES.put("majorLevel", Arrays.asList("专业全称", "层次", "专业层次", "培养层次"));
        MAJOR_FIELD_HEADER_ALIASES.put("majorRemark", Arrays.asList("专业备注", "备注", "说明"));
        MAJOR_FIELD_HEADER_ALIASES.put("majorName", Arrays.asList("专业名称", "专业", "招生专业"));
        MAJOR_FIELD_HEADER_ALIASES.put("duration", Arrays.asList("学制", "修业年限", "学制(年)"));
        MAJOR_FIELD_HEADER_ALIASES.put("tuition", Arrays.asList("学费", "学费(元/年)", "学费（元/年）"));
        MAJOR_FIELD_HEADER_ALIASES.put("planCount", Arrays.asList("计划人数", "招生计划", "录取人数", "计划数"));
        MAJOR_FIELD_HEADER_ALIASES.put("minScore", Arrays.asList("最低分", "最低分数", "去年最低分", "当年最低分"));
        MAJOR_FIELD_HEADER_ALIASES.put("minRank", Arrays.asList("最低位次", "最低排名", "去年最低位次", "当年最低位次"));
        MAJOR_FIELD_HEADER_ALIASES.put("avgScore", Arrays.asList("平均分", "平均分数", "去年平均分"));
        MAJOR_FIELD_HEADER_ALIASES.put("avgRank", Arrays.asList("平均位次", "平均排名", "去年平均位次"));

        MAJOR_REQUIRED_FIELDS.addAll(Arrays.asList("year", "universityName", "majorName", "batch"));

        STUDENT_FIELD_HEADER_ALIASES.put("username", Arrays.asList("用户名", "账号", "学生账号"));
        STUDENT_FIELD_HEADER_ALIASES.put("realName", Arrays.asList("姓名", "真实姓名", "学生姓名"));
        STUDENT_FIELD_HEADER_ALIASES.put("nickname", Arrays.asList("昵称", "常用称呼"));
        STUDENT_FIELD_HEADER_ALIASES.put("province", Arrays.asList("省份", "高考省份", "户籍省份"));
        STUDENT_FIELD_HEADER_ALIASES.put("score", Arrays.asList("总分", "成绩", "当前分数"));
        STUDENT_FIELD_HEADER_ALIASES.put("rank", Arrays.asList("位次", "排名", "当前位次"));
        STUDENT_FIELD_HEADER_ALIASES.put("firstMockScore", Arrays.asList("一模分数", "一模总分"));
        STUDENT_FIELD_HEADER_ALIASES.put("firstMockRank", Arrays.asList("一模位次"));
        STUDENT_FIELD_HEADER_ALIASES.put("secondMockScore", Arrays.asList("二模分数", "二模总分"));
        STUDENT_FIELD_HEADER_ALIASES.put("secondMockRank", Arrays.asList("二模位次"));
        STUDENT_FIELD_HEADER_ALIASES.put("subjects", Arrays.asList("选考科目", "科目组合", "选科"));
        STUDENT_FIELD_HEADER_ALIASES.put("targetMajorType", Arrays.asList("目标专业类型", "意向专业"));
        STUDENT_FIELD_HEADER_ALIASES.put("password", Arrays.asList("初始密码", "密码"));

        STUDENT_REQUIRED_FIELDS.add("username");
    }

    private final UniversityService universityService;
    private final MajorService majorService;
    private final UniversityMajorService universityMajorService;
    private final AdmissionStatService admissionStatService;
    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final PasswordEncoder passwordEncoder;

    public DataImportServiceImpl(UniversityService universityService, MajorService majorService,
                                 UniversityMajorService universityMajorService, AdmissionStatService admissionStatService,
                                 UserService userService, StudentProfileService studentProfileService,
                                 PasswordEncoder passwordEncoder) {
        this.universityService = universityService;
        this.majorService = majorService;
        this.universityMajorService = universityMajorService;
        this.admissionStatService = admissionStatService;
        this.userService = userService;
        this.studentProfileService = studentProfileService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public int importExcel(MultipartFile file) {
        int count = 0;
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return 0;
            }
            Map<String, Long> universityCache = new HashMap<>();
            Map<String, Long> majorCache = new HashMap<>();
            Row header = sheet.getRow(HEADER_ROW_INDEX);
            if (header == null) {
                throw new RuntimeException("Excel 缺少表头行");
            }
            Map<String, Integer> columnIndexes = resolveFieldColumnIndexes(sheet, formatter,
                    MAJOR_FIELD_HEADER_ALIASES, MAJOR_REQUIRED_FIELDS);

            for (int i = DATA_START_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String universityName = getStringCellValue(row, columnIndexes.get("universityName"), formatter);
                if (universityName.isEmpty()) {
                    continue;
                }
                String province = getStringCellValue(row, columnIndexes.get("province"), formatter);
                String city = getStringCellValue(row, columnIndexes.get("city"), formatter);
                String level = "";
                String type = getStringCellValue(row, columnIndexes.get("type"), formatter);
                String doubleTopFlag = getStringCellValue(row, columnIndexes.get("doubleTop"), formatter).toLowerCase();
                boolean isDoubleTop = "是".equals(doubleTopFlag) ||
                        "y".equals(doubleTopFlag) ||
                        "true".equals(doubleTopFlag) ||
                        "1".equals(doubleTopFlag);

                Long universityId = universityCache.computeIfAbsent(universityName, name -> {
                    University exist = universityService.lambdaQuery().eq(University::getName, name).one();
                    if (exist == null) {
                        University university = new University();
                        university.setName(name);
                        university.setProvince(province);
                        university.setCity(city);
                        university.setLevel(level);
                        university.setType(type);
                        university.setIsDoubleTop(isDoubleTop);
                        universityService.save(university);
                        return university.getId();
                    }
                    return exist.getId();
                });

                String majorName = getStringCellValue(row, columnIndexes.get("majorName"), formatter);
                if (majorName.isEmpty()) {
                    continue;
                }
                String category = getStringCellValue(row, columnIndexes.get("category"), formatter);
                String discipline = getStringCellValue(row, columnIndexes.get("discipline"), formatter);
                String subjectReq = "";
                String majorLevel = getStringCellValue(row, columnIndexes.get("majorLevel"), formatter);
                String majorRemark = getStringCellValue(row, columnIndexes.get("majorRemark"), formatter);

                Long majorId = majorCache.computeIfAbsent(majorName, name -> {
                    Major exist = majorService.lambdaQuery().eq(Major::getName, name).one();
                    if (exist == null) {
                        Major major = new Major();
                        major.setName(name);
                        major.setCategory(category);
                        major.setDiscipline(discipline);
                        major.setSubjectReq(subjectReq);
                        major.setLevel(majorLevel);
                        major.setRemark(majorRemark);
                        majorService.save(major);
                        return major.getId();
                    }
                    boolean updated = false;
                    if (!category.isEmpty() && !category.equals(exist.getCategory())) {
                        exist.setCategory(category);
                        updated = true;
                    }
                    if (!discipline.isEmpty() && !discipline.equals(exist.getDiscipline())) {
                        exist.setDiscipline(discipline);
                        updated = true;
                    }
                    if (!subjectReq.isEmpty() && !subjectReq.equals(exist.getSubjectReq())) {
                        exist.setSubjectReq(subjectReq);
                        updated = true;
                    }
                    if (!majorLevel.isEmpty() && !majorLevel.equals(exist.getLevel())) {
                        exist.setLevel(majorLevel);
                        updated = true;
                    }
                    if (!majorRemark.isEmpty() && !majorRemark.equals(exist.getRemark())) {
                        exist.setRemark(majorRemark);
                        updated = true;
                    }
                    if (updated) {
                        majorService.updateById(exist);
                    }
                    return exist.getId();
                });

                String batch = getStringCellValue(row, columnIndexes.get("batch"), formatter);
                int rowNumber = row.getRowNum();
                Integer duration = getIntegerCellValue(row, columnIndexes.get("duration"), formatter, rowNumber,
                        getFieldDisplayName("duration", MAJOR_FIELD_HEADER_ALIASES));
                Integer tuition = getIntegerCellValue(row, columnIndexes.get("tuition"), formatter, rowNumber,
                        getFieldDisplayName("tuition", MAJOR_FIELD_HEADER_ALIASES));
                Integer year = getIntegerCellValue(row, columnIndexes.get("year"), formatter, rowNumber,
                        getFieldDisplayName("year", MAJOR_FIELD_HEADER_ALIASES));
                if (year == null) {
                    continue;
                }
                Integer minScore = getIntegerCellValue(row, columnIndexes.get("minScore"), formatter, rowNumber,
                        getFieldDisplayName("minScore", MAJOR_FIELD_HEADER_ALIASES));
                Integer minRank = getIntegerCellValue(row, columnIndexes.get("minRank"), formatter, rowNumber,
                        getFieldDisplayName("minRank", MAJOR_FIELD_HEADER_ALIASES));
                Integer avgScore = getIntegerCellValue(row, columnIndexes.get("avgScore"), formatter, rowNumber,
                        getFieldDisplayName("avgScore", MAJOR_FIELD_HEADER_ALIASES));
                Integer avgRank = getIntegerCellValue(row, columnIndexes.get("avgRank"), formatter, rowNumber,
                        getFieldDisplayName("avgRank", MAJOR_FIELD_HEADER_ALIASES));
                Integer admitCount = getIntegerCellValue(row, columnIndexes.get("planCount"), formatter, rowNumber,
                        getFieldDisplayName("planCount", MAJOR_FIELD_HEADER_ALIASES));

                UniversityMajor universityMajor = universityMajorService.lambdaQuery()
                        .eq(UniversityMajor::getUniversityId, universityId)
                        .eq(UniversityMajor::getMajorId, majorId)
                        .eq(UniversityMajor::getBatch, batch)
                        .one();
                if (universityMajor == null) {
                    universityMajor = new UniversityMajor();
                    universityMajor.setUniversityId(universityId);
                    universityMajor.setMajorId(majorId);
                    universityMajor.setBatch(batch);
                    universityMajor.setDuration(duration);
                    universityMajor.setTuition(tuition);
                    universityMajorService.save(universityMajor);
                } else {
                    universityMajor.setDuration(duration);
                    universityMajor.setTuition(tuition);
                    universityMajorService.updateById(universityMajor);
                }

                AdmissionStat admissionStat = admissionStatService.lambdaQuery()
                        .eq(AdmissionStat::getUniversityId, universityId)
                        .eq(AdmissionStat::getMajorId, majorId)
                        .eq(AdmissionStat::getYear, year)
                        .eq(AdmissionStat::getBatch, batch)
                        .one();
                if (admissionStat == null) {
                    admissionStat = new AdmissionStat();
                    admissionStat.setUniversityId(universityId);
                    admissionStat.setMajorId(majorId);
                    admissionStat.setYear(year);
                    admissionStat.setBatch(batch);
                    admissionStat.setMinScore(minScore);
                    admissionStat.setMinRank(minRank);
                    admissionStat.setAvgScore(avgScore);
                    admissionStat.setAvgRank(avgRank);
                    admissionStat.setAdmitCount(admitCount);
                    admissionStatService.save(admissionStat);
                } else {
                    admissionStat.setMinScore(minScore);
                    admissionStat.setMinRank(minRank);
                    admissionStat.setAvgScore(avgScore);
                    admissionStat.setAvgRank(avgRank);
                    admissionStat.setAdmitCount(admitCount);
                    admissionStatService.updateById(admissionStat);
                }
                count++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import Excel: " + e.getMessage(), e);
        }
        return count;
    }

    @Override
    @Transactional
    public int importStudentProfiles(MultipartFile file, String strategy) {
        StudentImportStrategy importStrategy = StudentImportStrategy.from(strategy);
        DataFormatter formatter = new DataFormatter();
        int count = 0;
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return 0;
            }
            Map<String, Integer> columnIndexes = resolveFieldColumnIndexes(sheet, formatter,
                    STUDENT_FIELD_HEADER_ALIASES, STUDENT_REQUIRED_FIELDS);
            for (int i = DATA_START_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String username = getStringCellValue(row, columnIndexes.get("username"), formatter);
                if (username.isEmpty()) {
                    continue;
                }
                String password = getStringCellValue(row, columnIndexes.get("password"), formatter);
                String realName = getStringCellValue(row, columnIndexes.get("realName"), formatter);
                String nickname = getStringCellValue(row, columnIndexes.get("nickname"), formatter);
                String province = getStringCellValue(row, columnIndexes.get("province"), formatter);
                String subjects = getStringCellValue(row, columnIndexes.get("subjects"), formatter);
                String targetMajorType = getStringCellValue(row, columnIndexes.get("targetMajorType"), formatter);
                Integer score = getIntegerCellValue(row, columnIndexes.get("score"), formatter, i,
                        getFieldDisplayName("score", STUDENT_FIELD_HEADER_ALIASES));
                Integer rank = getIntegerCellValue(row, columnIndexes.get("rank"), formatter, i,
                        getFieldDisplayName("rank", STUDENT_FIELD_HEADER_ALIASES));
                Integer firstMockScore = getIntegerCellValue(row, columnIndexes.get("firstMockScore"), formatter, i,
                        getFieldDisplayName("firstMockScore", STUDENT_FIELD_HEADER_ALIASES));
                Integer firstMockRank = getIntegerCellValue(row, columnIndexes.get("firstMockRank"), formatter, i,
                        getFieldDisplayName("firstMockRank", STUDENT_FIELD_HEADER_ALIASES));
                Integer secondMockScore = getIntegerCellValue(row, columnIndexes.get("secondMockScore"), formatter, i,
                        getFieldDisplayName("secondMockScore", STUDENT_FIELD_HEADER_ALIASES));
                Integer secondMockRank = getIntegerCellValue(row, columnIndexes.get("secondMockRank"), formatter, i,
                        getFieldDisplayName("secondMockRank", STUDENT_FIELD_HEADER_ALIASES));

                User user = userService.lambdaQuery().eq(User::getUsername, username).one();
                if (user == null) {
                    user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(password.isEmpty() ? "123456" : password));
                    user.setRole("ROLE_STUDENT");
                    user.setEnabled(true);
                    userService.save(user);
                    StudentProfile profile = new StudentProfile();
                    profile.setUserId(user.getId());
                    applyProfileValues(profile, realName, nickname, score, rank, firstMockScore, firstMockRank,
                            secondMockScore, secondMockRank, subjects, province, targetMajorType, true, true);
                    studentProfileService.save(profile);
                    count++;
                    continue;
                }

                if (importStrategy == StudentImportStrategy.SKIP) {
                    continue;
                }

                StudentProfile profile = studentProfileService.lambdaQuery().eq(StudentProfile::getUserId, user.getId()).one();
                if (profile == null) {
                    profile = new StudentProfile();
                    profile.setUserId(user.getId());
                }
                boolean updateBasicInfo = importStrategy == StudentImportStrategy.OVERWRITE;
                boolean updateScores = updateBasicInfo || importStrategy == StudentImportStrategy.SCORE_ONLY;
                applyProfileValues(profile, realName, nickname, score, rank, firstMockScore, firstMockRank,
                        secondMockScore, secondMockRank, subjects, province, targetMajorType, updateBasicInfo, updateScores);
                studentProfileService.saveOrUpdate(profile);
                if (!password.isEmpty()) {
                    user.setPassword(passwordEncoder.encode(password));
                    userService.updateById(user);
                }
                count++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to import student Excel: " + e.getMessage(), e);
        }
        return count;
    }

    private String getStringCellValue(Row row, Integer index, DataFormatter formatter) {
        if (row == null || index == null) {
            return "";
        }
        var cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private Integer getIntegerCellValue(Row row, Integer index, DataFormatter formatter, int rowNumber, String fieldDisplayName) {
        if (row == null || index == null) {
            return null;
        }
        var cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case BLANK:
                return null;
            case NUMERIC:
                return (int) Math.round(cell.getNumericCellValue());
            case STRING:
                return parseIntegerFromString(cell.getStringCellValue(), index, rowNumber, fieldDisplayName);
            case FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case NUMERIC:
                        return (int) Math.round(cell.getNumericCellValue());
                    case STRING:
                        return parseIntegerFromString(cell.getStringCellValue(), index, rowNumber, fieldDisplayName);
                    case BLANK:
                        return null;
                    default:
                        String formatted = formatter.formatCellValue(cell).trim();
                        return formatted.isEmpty() ? null : parseIntegerFromString(formatted, index, rowNumber, fieldDisplayName);
                }
            default:
                String formatted = formatter.formatCellValue(cell).trim();
                return formatted.isEmpty() ? null : parseIntegerFromString(formatted, index, rowNumber, fieldDisplayName);
        }
    }

    private Integer parseIntegerFromString(String text, int index, int rowNumber, String fieldDisplayName) {
        if (text.isEmpty()) {
            return null;
        }
        String trimmed = text.trim();
        if (!trimmed.matches("^\\d+(\\.\\d+)?$")) {
            throw new RuntimeException("第 " + (rowNumber + 1) + " 行、第 " + formatColumnName(index) + " 列（" + fieldDisplayName + "）的内容应为数字，但实际为：'" + trimmed + "'");
        }
        try {
            BigDecimal decimal = new BigDecimal(trimmed).stripTrailingZeros();
            if (decimal.scale() > 0) {
                throw new NumberFormatException("Non-integer value");
            }
            return decimal.intValueExact();
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new RuntimeException("无法解析第 " + (rowNumber + 1) + " 行、第 " + formatColumnName(index) + " 列（" + fieldDisplayName + "）的整数值：'" + trimmed + "'", ex);
        }
    }

    private Map<String, Integer> resolveFieldColumnIndexes(Sheet sheet, DataFormatter formatter,
                                                          Map<String, List<String>> aliasMap,
                                                          Set<String> requiredFields) {
        Row headerRow = sheet.getRow(HEADER_ROW_INDEX);
        if (headerRow == null) {
            throw new RuntimeException("Excel 缺少表头行");
        }
        Map<String, Integer> headerIndexMap = new HashMap<>();
        short lastCellNum = headerRow.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            var cell = headerRow.getCell(i);
            String title = cell == null ? "" : formatter.formatCellValue(cell);
            if (!title.isEmpty()) {
                headerIndexMap.put(normalizeHeader(title), i);
            }
        }
        Map<String, Integer> columnIndexes = new HashMap<>();
        aliasMap.forEach((field, aliases) -> {
            Integer index = aliases.stream()
                    .map(alias -> normalizeHeader(alias))
                    .map(headerIndexMap::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (index == null && requiredFields.contains(field)) {
                throw new RuntimeException("Excel 缺少必填列：" + String.join("/", aliases));
            }
            columnIndexes.put(field, index);
        });
        return columnIndexes;
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        String trimmed = header.replace('\u00A0', ' ').trim();
        return trimmed.replaceAll("\\s+", "").toLowerCase();
    }

    private String formatColumnName(int columnIndex) {
        int index = columnIndex;
        StringBuilder columnName = new StringBuilder();
        while (index >= 0) {
            int remainder = index % 26;
            columnName.insert(0, (char) ('A' + remainder));
            index = (index / 26) - 1;
        }
        return columnName.toString();
    }

    private String getFieldDisplayName(String fieldKey, Map<String, List<String>> aliasMap) {
        List<String> aliases = aliasMap.get(fieldKey);
        if (aliases == null || aliases.isEmpty()) {
            return fieldKey;
        }
        return aliases.get(0);
    }

    private void applyProfileValues(StudentProfile profile, String realName, String nickname, Integer score, Integer rank,
                                    Integer firstMockScore, Integer firstMockRank, Integer secondMockScore, Integer secondMockRank,
                                    String subjects, String province, String targetMajorType,
                                    boolean updateBasicInfo, boolean updateScores) {
        if (updateBasicInfo) {
            if (realName != null && !realName.isEmpty()) {
                profile.setRealName(realName);
            }
            if (nickname != null && !nickname.isEmpty()) {
                profile.setNickname(nickname);
            }
            if (targetMajorType != null && !targetMajorType.isEmpty()) {
                profile.setTargetMajorType(targetMajorType);
            }
        }
        if (updateScores) {
            if (score != null) {
                profile.setScore(score);
            }
            if (rank != null) {
                profile.setRank(rank);
            }
            if (firstMockScore != null) {
                profile.setFirstMockScore(firstMockScore);
            }
            if (firstMockRank != null) {
                profile.setFirstMockRank(firstMockRank);
            }
            if (secondMockScore != null) {
                profile.setSecondMockScore(secondMockScore);
            }
            if (secondMockRank != null) {
                profile.setSecondMockRank(secondMockRank);
            }
            if (subjects != null && !subjects.isEmpty()) {
                profile.setSubjects(subjects);
            }
            if (province != null && !province.isEmpty()) {
                profile.setProvince(province);
            }
        }
    }

    private enum StudentImportStrategy {
        SKIP,
        OVERWRITE,
        SCORE_ONLY;

        static StudentImportStrategy from(String text) {
            if (text == null) {
                return OVERWRITE;
            }
            String normalized = text.trim().toLowerCase();
            return switch (normalized) {
                case "skip" -> SKIP;
                case "score", "scores", "score_only", "score-only", "scoreonly" -> SCORE_ONLY;
                default -> OVERWRITE;
            };
        }
    }
}
