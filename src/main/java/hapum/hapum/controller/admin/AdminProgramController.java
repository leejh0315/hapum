package hapum.hapum.controller.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import hapum.hapum.domain.Program;
import hapum.hapum.domain.ProgramAdd;
import hapum.hapum.domain.ProgramSub;
import hapum.hapum.service.ProgramService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProgramController {

	private final ProgramService programService;

	@GetMapping("/openProgram")
	public String getOpenProgram(Model model) {
		List<ProgramAdd> programAdds = programService.selectAllProgramAdd();
		model.addAttribute("programAdds", programAdds);
		return "admin/openProgram";
	}

	@GetMapping("/manageProgram")
	public String getManageProgram() {
		return "admin/manageProgram";
	}
	
	@PostMapping("/program/add")
	public String postProgramAdd(ProgramAdd programAdd, @RequestParam("photo") MultipartFile photo) throws IOException {
		
		
		
		programService.insertProgramAdd(programAdd, photo);
		return "admin/main";
	}
	
	@PostMapping("/openProgram")
	public String postOpenProgram(Program program, @RequestParam("image") MultipartFile imageFile) throws IOException {
		if(program.getExpense()==null) {
			program.setExpense("0");
		}
		programService.insertProgram(program, imageFile);
		return "admin/main";
	}
	@PostMapping("/program/changePopup/{id}")
	public String changePopup(@PathVariable("id")Long id) {
		Program program = programService.selectProgramById(id);
		String code = "N";
		
		if(program.getIsPopup().equals("N")) {
			code = "Y";
		}
		programService.changePopup(id, code);
		
		return "redirect:/admin/programs";
	}
	

	@GetMapping("/programs")
	public String getPrograms(Model model) {
		
		List<ProgramAdd> programAdds = programService.selectAllProgramAdd();
		Map<ProgramAdd, List<Program>> map = new HashMap();
		for(ProgramAdd pa : programAdds) {
			List<Program> programs = programService.selectProgramByAddId(pa.getId());
			map.put(pa, programs);
		}
		
		model.addAttribute("programs", map);
		return "admin/programs";
	}
	
	@PostMapping("/programAdd/updateCode/{addId}")
	public String updateProgramAddOpenStatus(@PathVariable("addId") Long addId,  @RequestParam("code") String code) {
	    // 상태를 Y <-> N 으로 전환
	    programService.updateProgramAddStatus(addId, code.equals("Y") ? "N" : "Y");
	    return "redirect:/admin/programs";
	}

	@GetMapping("/program/detail/{id}")
	public String getProgramDetail(Model model, @PathVariable("id") Long id) {

		Program program = programService.selectProgramById(id);

		List<ProgramSub> ps = programService.getSubsWithUsers(id);

		List<ProgramSub> psY = ps.stream().filter(p -> "Y".equals(p.getIsApp())).toList();
		List<ProgramSub> psN = ps.stream().filter(p -> "N".equals(p.getIsApp())).toList();

		model.addAttribute("ps", ps);
		model.addAttribute("psY", psY);
		model.addAttribute("psN", psN);
		model.addAttribute("program", program);
		return "admin/programsDetail";
	}

	@PostMapping("/program/approve")
	public String approve(@RequestParam("programSubId") Long programSubId, @RequestParam("programId") Long programId) {
		programService.approve(programSubId); // isApp = "Y" 로 변경
		return "redirect:/admin/program/detail/" + programId; // 필요시 파라미터 유지
	}

	@PostMapping("/program/cancel")
	public String cancel(@RequestParam("programSubId") Long programSubId, @RequestParam("programId") Long programId) {
		programService.cancel(programSubId); // isApp = "Y" 로 변경
		return "redirect:/admin/program/detail/" + programId; // 필요시 파라미터 유지
	}
	
	@PostMapping("/program/updateCode/{programId}")
	public String updateCode(@PathVariable("programId") Long programId, @RequestParam("code") String code) {
		
		if(code.equals("Y")){code="N";} 
		else{code="Y";}
		
		programService.updateCode(programId, code);
		return "redirect:/admin/programs";
	}
	
	@GetMapping("/program/update/{id}")
	public String getUpdateProgam(@PathVariable("id")Long id, Model model) {
		List<ProgramAdd> programAdds = programService.selectAllProgramAdd();
		model.addAttribute("programAdds", programAdds);
		Program program = programService.selectProgramById(id);
		model.addAttribute("program",program);
		return "admin/updateProgram";
	}
	
	@PostMapping("/program/update/{id}")
	public String postUpdateProgram(@PathVariable("id")Long id, Program program, @RequestParam("image") MultipartFile imageFile)  throws IOException{
		programService.updateProgram(program, imageFile);
		return "redirect:/admin/programs";
	}
	
	@GetMapping("/programAdd/update/{addId}")
	public String getUpdatePrgramAdd(@PathVariable("addId")Long addId, Model model) {
		ProgramAdd programAdd = programService.selectProgramAddById(addId);
		model.addAttribute("programAdd",programAdd);
		return "admin/updateProgramAdd";
	}
	
	@PostMapping("/programAdd/update/{id}")
	public String updateProgramAdd(@PathVariable("id") Long id,
	                               ProgramAdd programAdd, 
	                               @RequestParam("photo") MultipartFile photo) throws IOException {
		programService.updateProgramAdd(programAdd, photo);
		return "redirect:/admin/programs";
	}
	
	@PostMapping("/program/delete/{id}")
	public String deleteProgram(@PathVariable("id")Long id) {
		programService.deleteProgram(id);
		return "redirect:/admin/programs";
	}
	@PostMapping("/programAdd/delete/{id}")
	public String deleteProgramAdd(@PathVariable("id")Long id) {
		programService.deleteProgramAdd(id);
		return "redirect:/admin/programs";
	}

}
