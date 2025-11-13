package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gaokao.domain.*;
import com.example.gaokao.service.*;
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
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                return 0;
            }
            Map<String, Long> universityCache = new HashMap<>();
            Map<String, Long> majorCache = new HashMap<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String universityName = row.getCell(0).getStringCellValue();
                String province = row.getCell(1).getStringCellValue();
                String city = row.getCell(2).getStringCellValue();
                String level = row.getCell(3).getStringCellValue();
                String type = row.getCell(4).getStringCellValue();
                boolean isDoubleTop = "是".equalsIgnoreCase(row.getCell(5).getStringCellValue()) ||
                        "Y".equalsIgnoreCase(row.getCell(5).getStringCellValue()) ||
                        "true".equalsIgnoreCase(row.getCell(5).getStringCellValue());

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

                String majorName = row.getCell(6).getStringCellValue();
                String category = row.getCell(7).getStringCellValue();
                String discipline = row.getCell(8).getStringCellValue();
                String subjectReq = row.getCell(9).getStringCellValue();
                String majorLevel = row.getCell(10).getStringCellValue();

                Long majorId = majorCache.computeIfAbsent(majorName, name -> {
                    Major exist = majorService.lambdaQuery().eq(Major::getName, name).one();
                    if (exist == null) {
                        Major major = new Major();
                        major.setName(name);
                        major.setCategory(category);
                        major.setDiscipline(discipline);
                        major.setSubjectReq(subjectReq);
                        major.setLevel(majorLevel);
                        majorService.save(major);
                        return major.getId();
                    }
                    return exist.getId();
                });

                String batch = row.getCell(11).getStringCellValue();
                int duration = (int) row.getCell(12).getNumericCellValue();
                int tuition = (int) row.getCell(13).getNumericCellValue();
                int year = (int) row.getCell(14).getNumericCellValue();
                int minScore = (int) row.getCell(15).getNumericCellValue();
                int minRank = (int) row.getCell(16).getNumericCellValue();
                int avgScore = (int) row.getCell(17).getNumericCellValue();
                int avgRank = (int) row.getCell(18).getNumericCellValue();
                int admitCount = (int) row.getCell(19).getNumericCellValue();

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
}
