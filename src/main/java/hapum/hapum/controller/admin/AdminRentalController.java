package hapum.hapum.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hapum.hapum.domain.BlockedDay;
import hapum.hapum.domain.FixedReservation;
import hapum.hapum.service.ReservationService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/fixedRental")
@RequiredArgsConstructor
public class AdminRentalController {

	private final ReservationService reservationService;
	
	@GetMapping("")
	public String getFixedRental(Model model) {
		List<FixedReservation> fixedReservation = reservationService.getFixedReservations();
		List<BlockedDay> blockedDay = reservationService.getBlockedDays();
		
		model.addAttribute("fixedReservation" , fixedReservation);
		model.addAttribute("blockedDay", blockedDay);
		
		return "admin/fixedRental";
	}
	@PostMapping("/insert")
	public String insertFixedRental(FixedReservation fixedReservation) {
		reservationService.insertFixedReservation(fixedReservation);
		return "redirect:/admin/fixedRental";
	}
	@PostMapping("/update")
	public String updateFixedRental(FixedReservation fixedReservation) {
		reservationService.updateFixedReservation(fixedReservation);
		return "redirect:/admin/fixedRental";
	}
	@PostMapping("/delete")
	public String deleteFixedRental(@RequestParam("id") Long id) {
		reservationService.deleteFixedReservation(id);
		return "redirect:/admin/fixedRental";
	}
	
	
	@PostMapping("/blocked/insert")
	public String insertFixedRentalBlocked(BlockedDay blockedDay) {
		reservationService.insertBlockedDay(blockedDay);
		return "redirect:/admin/fixedRental";
	}
	@PostMapping("/blocked/update")
	public String updateFixedRentalBlocked(BlockedDay blockedDay) {
		reservationService.updateBlockedDay(blockedDay);
		return "redirect:/admin/fixedRental";
	}
	@PostMapping("/blocked/delete")
	public String deleteFixedRentalBlocked(@RequestParam("id") Long id) {
		reservationService.deleteBlockedDay(id);
		return "redirect:/admin/fixedRental";
	}
	
	
}
