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
import java.util.Map;

@Service
public class DataImportServiceImpl implements DataImportService {

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
            int firstDataRow = Math.max(headerRow + 1, 1);
            for (int i = firstDataRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String universityName = getStringCellValue(row, 6, formatter);
                if (universityName.isEmpty()) {
                    continue;
                }
                String province = getStringCellValue(row, 2, formatter);
                String city = getStringCellValue(row, 7, formatter);
                String level = "";
                String type = getStringCellValue(row, 3, formatter);
                String doubleTopFlag = getStringCellValue(row, 5, formatter).toLowerCase();
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

                String majorName = getStringCellValue(row, 11, formatter);
                if (majorName.isEmpty()) {
                    continue;
                }
                String category = getStringCellValue(row, 3, formatter);
                String discipline = getStringCellValue(row, 9, formatter);
                String subjectReq = "";
                String majorLevel = getStringCellValue(row, 10, formatter);
                String majorRemark = getStringCellValue(row, 12, formatter);

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

                String batch = getStringCellValue(row, 4, formatter);
                Integer duration = getIntegerCellValue(row, 13, formatter);
                Integer tuition = getIntegerCellValue(row, 14, formatter);
                Integer year = getIntegerCellValue(row, 1, formatter);
                if (year == null) {
                    continue;
                }
                Integer minScore = getIntegerCellValue(row, 16, formatter);
                Integer minRank = getIntegerCellValue(row, 17, formatter);
                Integer avgScore = getIntegerCellValue(row, 18, formatter);
                Integer avgRank = getIntegerCellValue(row, 19, formatter);
                Integer admitCount = getIntegerCellValue(row, 15, formatter);

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

    private String getStringCellValue(Row row, int index, DataFormatter formatter) {
        if (row == null) {
            return "";
        }
        var cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private Integer getIntegerCellValue(Row row, int index, DataFormatter formatter) {
        if (row == null) {
            return null;
        }
        var cell = row.getCell(index);
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case BLANK -> null;
            case NUMERIC -> (int) Math.round(cell.getNumericCellValue());
            case STRING -> parseIntegerFromString(cell.getStringCellValue().trim(), index);
            case FORMULA -> {
                Integer value = switch (cell.getCachedFormulaResultType()) {
                    case NUMERIC -> (int) Math.round(cell.getNumericCellValue());
                    case STRING -> parseIntegerFromString(cell.getStringCellValue().trim(), index);
                    case BLANK -> null;
                    default -> {
                        String formatted = formatter.formatCellValue(cell).trim();
                        yield formatted.isEmpty() ? null : parseIntegerFromString(formatted, index);
                    }
                };
                yield value;
            }
            default -> {
                String formatted = formatter.formatCellValue(cell).trim();
                yield formatted.isEmpty() ? null : parseIntegerFromString(formatted, index);
            }
        };
    }

    private Integer parseIntegerFromString(String text, int index) {
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
            throw new RuntimeException("无法解析第 " + (index + 1) + " 列的整数值：'" + text + "'", ex);
        }
    }
}
