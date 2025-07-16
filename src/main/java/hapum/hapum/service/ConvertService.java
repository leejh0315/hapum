package hapum.hapum.service;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import hapum.hapum.domain.ProgramSub;

@Service
public class ConvertService {

	
	public String generateWordFromTemplate(ProgramSub ps, String inputTemplatePath, String outputPath) throws Exception {
        try (FileInputStream fis = new FileInputStream(inputTemplatePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            Map<String, String> replacements = new HashMap<>();
            replacements.put("${userId}", String.valueOf(ps.getUserId()));
            replacements.put("${programId}", String.valueOf(ps.getProgramId()));
            replacements.put("${orgName}", ps.getOrgName());
            replacements.put("${partCount}", Integer.toString(ps.getPartCount())!= null ? String.valueOf(ps.getPartCount()) : "N/A");
            replacements.put("${relation}", ps.getRelation());

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String text = run.getText(0);
                    if (text != null) {
                        for (Map.Entry<String, String> entry : replacements.entrySet()) {
                            if (text.contains(entry.getKey())) {
                                text = text.replace(entry.getKey(), entry.getValue());
                            }
                        }
                        run.setText(text, 0);
                    }
                }
            }

            try (FileOutputStream out = new FileOutputStream(outputPath)) {
                document.write(out);
            }

            return outputPath; // 저장된 파일 경로 반환
        }
    }

}
