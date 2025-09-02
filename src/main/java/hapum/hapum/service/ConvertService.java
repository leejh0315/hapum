package hapum.hapum.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import hapum.hapum.domain.Program;
import hapum.hapum.domain.ProgramSub;
import hapum.hapum.domain.Rental;
import hapum.hapum.domain.User;

@Service
public class ConvertService {

    @Value("${doc.program.template.path}")
    private String templatePath;

    @Value("${doc.program.output.dir}")
    private String outputDir;

    @Value("${doc.rental.template.path}")
    private String rentalTemplatePath;

    @Value("${doc.rental.output.dir}")
    private String rentalOutputDir;

    
    
    DateTimeFormatter formatter =DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime now = LocalDateTime.now();

    
    public String generateProgramWordFromTemplate(ProgramSub ps, Program program, User user) throws Exception {
        // 1) 출력 폴더가 없으면 생성
        File outFolder = new File(outputDir);
        if (!outFolder.exists()) {
            outFolder.mkdirs();
        }

        // 2) 출력할 파일명 생성
        String fileName = String.format("program_%d.docx", ps.getId());
        File outputFile = new File(outFolder, fileName);

        // 3) 치환 맵 준비
        Map<String, String> replacements = new HashMap<>();
        if (ps.getOrgName() != null) {
            replacements.put("${organization}", ps.getOrgName());
        }

        if (program.getTitle() != null) {
            replacements.put("${title}", program.getTitle());
        }

        if (user.getName() != null) {
            replacements.put("${username}", user.getName());
        }

        if (program.getStartDate() != null) {
            replacements.put("${date}", formatter.format(program.getStartDate()));
        }

        replacements.put("${count}",Integer.toString(ps.getPartCount()));
        

        if (now != null) { // now가 null일 가능성은 거의 없지만 안전하게
            replacements.put("${year}", String.valueOf(now.getYear()));
            replacements.put("${month}", String.valueOf(now.getMonthValue()));
            replacements.put("${day}", String.valueOf(now.getDayOfMonth()));
        }

        if (user.getPhone() != null) {
            replacements.put("${userphone}", user.getPhone());
        }

        if (ps.getOpinion() != null) {
            replacements.put("${opinion}", ps.getOpinion());
        }

        if (ps.getReason() != null) {
            replacements.put("${reason}", ps.getReason());
        }

        
        
        // 4) 템플릿 열기
        try (InputStream is = new FileInputStream(templatePath);
             XWPFDocument document = new XWPFDocument(is)) {

            // 5) 문단 치환
            replaceInParagraphs(document.getParagraphs(), replacements);

            // 6) 표(table) 내부 치환
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        replaceInParagraphs(cell.getParagraphs(), replacements);
                    }
                }
            }

            // 7) 결과 파일로 쓰기
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }
            
           

        }

        // 8) 다운로드용 URL 반환 (WebConfig의 /result/** 매핑과 일치)
        return fileName;
    }

    public String generateRentalWordFromTemplate(Rental rental, User user) throws Exception {
        // 1) 출력 폴더가 없으면 생성
        File outFolder = new File(rentalOutputDir);
        if (!outFolder.exists()) {
            outFolder.mkdirs();
        }

        // 2) 출력할 파일명 생성
        String fileName = String.format("rental_%d.docx", rental.getId());
        File outputFile = new File(outFolder, fileName);

        // 3) 치환 맵 준비
        Map<String, String> replacements = new HashMap<>();
        
        String time = "";
        if(rental.getStartTime()!=null) {
        	time += rental.getStartTime().toString();
        	time += " ~ ";
        }
        if(rental.getEndTime()!=null) {
        	time += rental.getEndTime().toString();	
        }        
        
        
        if (rental.getGroupName() != null) {
            replacements.put("${organization}", rental.getGroupName());
        }

        if (rental.getPurpose() != null) {
            replacements.put("${purpose}", rental.getPurpose());
        }

        if (rental.getReservationDate() != null) {
            replacements.put("${date}", rental.getReservationDate().format(DateTimeFormatter.ISO_DATE));
        }

            replacements.put("${time}", time);

        replacements.put("${count}",Integer.toString(rental.getHeadCount()));


        if (user.getName() != null) {
            replacements.put("${username}", user.getName());
        }

        if (user.getPhone() != null) {
            replacements.put("${userphone}", user.getPhone());
        }

        if (rental.getLocation() != null) {
            replacements.put("${location}", rental.getLocation());
        }

        if (rental.getEquipment() != null) {
            replacements.put("${equip}", rental.getEquipment());
        }

        if (now != null) { // now는 보통 null이 아니지만 안전하게 체크
            replacements.put("${year}", String.valueOf(now.getYear()));
            replacements.put("${month}", String.valueOf(now.getMonthValue()));
            replacements.put("${day}", String.valueOf(now.getDayOfMonth()));
        }


        // 4) 템플릿 열기
        try (InputStream is = new FileInputStream(rentalTemplatePath);
             XWPFDocument document = new XWPFDocument(is)) {

            // 5) 문단 치환
            replaceInParagraphs(document.getParagraphs(), replacements);

            // 6) 표(table) 내부 치환
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        replaceInParagraphs(cell.getParagraphs(), replacements);
                    }
                }
            }

            // 7) 결과 파일로 쓰기
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }

           

        }

        // 8) 다운로드용 URL 반환 (WebConfig의 /result/** 매핑과 일치)
        return fileName;
    }

   
    /**
     * 주어진 문단 리스트에서 플레이스홀더를 치환한 뒤
     * 기존 Run을 모두 제거하고 새 Run으로 전체 텍스트를 삽입합니다.
     */
    private void replaceInParagraphs(List<XWPFParagraph> paragraphs,
                                     Map<String, String> replacements) {
        for (XWPFParagraph paragraph : paragraphs) {
            // 문단 전체 텍스트 가져오기
            String fullText = paragraph.getText();
            if (fullText == null || fullText.isEmpty()) {
                continue;
            }

            // 각 키에 대해 치환
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                fullText = fullText.replace(entry.getKey(), entry.getValue());
            }

            // 기존 Run 전부 제거
            int runCount = paragraph.getRuns().size();
            for (int i = runCount - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }

            // 새 Run으로 교체된 텍스트 삽입
            XWPFRun newRun = paragraph.createRun();
            newRun.setText(fullText, 0);
        }
    }
}