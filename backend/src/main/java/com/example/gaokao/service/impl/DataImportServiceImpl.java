package com.example.gaokao.service.impl;

import com.example.gaokao.domain.*;
import com.example.gaokao.service.*;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class DataImportServiceImpl implements DataImportService {

    private static final Map<String, List<String>> FIELD_HEADER_ALIASES = Map.ofEntries(
            Map.entry("year", List.of("年份", "year", "年度")),
            Map.entry("province", List.of("生源地", "生源省份", "省份", "院校省份")),
            Map.entry("city", List.of("院校所在地", "所在城市", "城市", "地区")),
            Map.entry("type", List.of("科类", "文理科", "选科要求", "选考科目")),
            Map.entry("batch", List.of("批次", "录取批次", "招生批次")),
            Map.entry("doubleTop", List.of("是否双一流", "双一流", "双一流标识", "是否985", "是否211")),
            Map.entry("universityName", List.of("院校名称", "学校名称", "高校名称")),
            Map.entry("category", List.of("专业类别", "专业类", "类别", "科类", "学科门类")),
            Map.entry("discipline", List.of("专业方向", "方向", "学科门类")),
            Map.entry("majorLevel", List.of("专业全称", "层次", "专业层次", "培养层次")),
            Map.entry("majorRemark", List.of("专业备注", "备注", "说明")),
            Map.entry("majorName", List.of("专业名称", "专业", "招生专业")),
            Map.entry("duration", List.of("学制", "修业年限", "学制(年)")),
            Map.entry("tuition", List.of("学费", "学费(元/年)", "学费（元/年）")),
            Map.entry("planCount", List.of("计划人数", "招生计划", "录取人数", "计划数")),
            Map.entry("minScore", List.of("最低分", "最低分数", "去年最低分", "当年最低分")),
            Map.entry("minRank", List.of("最低位次", "最低排名", "去年最低位次", "当年最低位次")),
            Map.entry("avgScore", List.of("平均分", "平均分数", "去年平均分")),
            Map.entry("avgRank", List.of("平均位次", "平均排名", "去年平均位次"))
    );

    private static final Set<String> REQUIRED_FIELDS = Set.of("year", "universityName", "majorName", "batch");

    private final UniversityService universityService;
    private final MajorService majorService;
    private final UniversityMajorService universityMajorService;
    private final AdmissionStatService admissionStatService;

    public DataImportServiceImpl(UniversityService universityService, MajorService majorService,
                                 UniversityMajorService universityMajorService, AdmissionStatService admissionStatService) {
        this.universityService = universityService;
        this.majorService = majorService;
        this.universityMajorService = universityMajorService;
        this.admissionStatService = admissionStatService;
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
            int headerRow = sheet.getFirstRowNum();
            Row header = sheet.getRow(headerRow);
            if (header == null) {
                throw new RuntimeException("Excel 缺少表头行");
            }
            Map<String, Integer> headerIndexMap = buildHeaderIndexMap(header, formatter);
            Map<String, Integer> columnIndexes = resolveFieldColumnIndexes(headerIndexMap);

            int firstDataRow = Math.max(headerRow + 1, 1);
            for (int i = firstDataRow; i <= sheet.getLastRowNum(); i++) {
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
                Integer duration = getIntegerCellValue(row, columnIndexes.get("duration"), formatter, rowNumber, getFieldDisplayName("duration"));
                Integer tuition = getIntegerCellValue(row, columnIndexes.get("tuition"), formatter, rowNumber, getFieldDisplayName("tuition"));
                Integer year = getIntegerCellValue(row, columnIndexes.get("year"), formatter, rowNumber, getFieldDisplayName("year"));
                if (year == null) {
                    continue;
                }
                Integer minScore = getIntegerCellValue(row, columnIndexes.get("minScore"), formatter, rowNumber, getFieldDisplayName("minScore"));
                Integer minRank = getIntegerCellValue(row, columnIndexes.get("minRank"), formatter, rowNumber, getFieldDisplayName("minRank"));
                Integer avgScore = getIntegerCellValue(row, columnIndexes.get("avgScore"), formatter, rowNumber, getFieldDisplayName("avgScore"));
                Integer avgRank = getIntegerCellValue(row, columnIndexes.get("avgRank"), formatter, rowNumber, getFieldDisplayName("avgRank"));
                Integer admitCount = getIntegerCellValue(row, columnIndexes.get("planCount"), formatter, rowNumber, getFieldDisplayName("planCount"));

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

        return switch (cell.getCellType()) {
            case BLANK -> null;
            case NUMERIC -> (int) Math.round(cell.getNumericCellValue());
            case STRING -> parseIntegerFromString(cell.getStringCellValue().trim(), index, rowNumber, fieldDisplayName);
            case FORMULA -> {
                Integer value = switch (cell.getCachedFormulaResultType()) {
                    case NUMERIC -> (int) Math.round(cell.getNumericCellValue());
                    case STRING -> parseIntegerFromString(cell.getStringCellValue().trim(), index, rowNumber, fieldDisplayName);
                    case BLANK -> null;
                    default -> {
                        String formatted = formatter.formatCellValue(cell).trim();
                        yield formatted.isEmpty() ? null : parseIntegerFromString(formatted, index, rowNumber, fieldDisplayName);
                    }
                };
                yield value;
            }
            default -> {
                String formatted = formatter.formatCellValue(cell).trim();
                yield formatted.isEmpty() ? null : parseIntegerFromString(formatted, index, rowNumber, fieldDisplayName);
            }
        };
    }

    private Integer parseIntegerFromString(String text, int index, int rowNumber, String fieldDisplayName) {
        if (text.isEmpty()) {
            return null;
        }
        try {
            BigDecimal decimal = new BigDecimal(text).stripTrailingZeros();
            if (decimal.scale() > 0) {
                throw new NumberFormatException("Non-integer value");
            }
            return decimal.intValueExact();
        } catch (NumberFormatException | ArithmeticException ex) {
            throw new RuntimeException("无法解析第 " + (rowNumber + 1) + " 行、第 " + (index + 1) + " 列（" + fieldDisplayName + "）的整数值：'" + text + "'", ex);
        }
    }

    private Map<String, Integer> buildHeaderIndexMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        short lastCellNum = headerRow.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            var cell = headerRow.getCell(i);
            String title = cell == null ? "" : formatter.formatCellValue(cell).trim();
            if (!title.isEmpty()) {
                headerIndexMap.put(normalizeHeader(title), i);
            }
        }
        return headerIndexMap;
    }

    private Map<String, Integer> resolveFieldColumnIndexes(Map<String, Integer> headerIndexMap) {
        Map<String, Integer> columnIndexes = new HashMap<>();
        FIELD_HEADER_ALIASES.forEach((field, aliases) -> {
            Integer index = aliases.stream()
                    .map(this::normalizeHeader)
                    .map(headerIndexMap::get)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (index == null && REQUIRED_FIELDS.contains(field)) {
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

    private String getFieldDisplayName(String fieldKey) {
        List<String> aliases = FIELD_HEADER_ALIASES.get(fieldKey);
        if (aliases == null || aliases.isEmpty()) {
            return fieldKey;
        }
        return aliases.get(0);
    }
}
