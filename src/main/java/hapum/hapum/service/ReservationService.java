package hapum.hapum.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import hapum.hapum.domain.BlockedDay;
import hapum.hapum.domain.FixedReservation;
import hapum.hapum.domain.Rental;
import hapum.hapum.domain.RentalWithUser;
import hapum.hapum.mapper.FixedReservationMapper;

@Service
public class ReservationService {

    private final FixedReservationMapper reservationMapper;

    public ReservationService(FixedReservationMapper reservationMapper) {
        this.reservationMapper = reservationMapper;
    }

    public List<FixedReservation> getFixedReservations() {
        return reservationMapper.selectAllFixedReservations();
    }

    public List<BlockedDay> getBlockedDays() {
        return reservationMapper.selectBlockedDays();
    }
    
    public List<Rental> getRentals(){
    	return reservationMapper.selectRentals();
    }
    

    public boolean isBlocked(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue() % 7; // 0(일)~6(토)
        int weekOfMonth = (date.getDayOfMonth() - 1) / 7 + 1;

        return getBlockedDays().stream()
                .anyMatch(b -> b.getWeekOfMonth() == weekOfMonth && b.getDayOfWeek() == dayOfWeek);
    }
    
    //고정 예약 목록------------------------------------------------------------------------
    public void insertFixedReservation(FixedReservation fixedReservation) {
    	reservationMapper.insertFixedReservation(fixedReservation);
    }
    public void updateFixedReservation(FixedReservation fixedReservation) {
    	reservationMapper.updateFixedReservation(fixedReservation);
    }
    public void deleteFixedReservation(Long id) {
    	reservationMapper.deleteFixedReservation(id);
    }
    //------------------------------------------------------------------------
    
    
    //대관 불가 목록------------------------------------------------------------------------
    public void insertBlockedDay(BlockedDay blockedDay) {
    	reservationMapper.insertBlockedDay(blockedDay);
    }
    public void updateBlockedDay(BlockedDay blockedDay) {
    	reservationMapper.updateBlockedDay(blockedDay);
    }
    public void deleteBlockedDay(Long id) {
    	reservationMapper.deleteBlockedDay(id);
    }
    //------------------------------------------------------------------------
    
    
    public void insertRental(Rental rental) {
    	reservationMapper.insertRental(rental);
    }
    
    public List<Rental> selectByUserId (Long userId){
    	return reservationMapper.selectByUserId(userId); 
    }
    
    public void deleteRental(Long rentalId) {
    	reservationMapper.deleteRental(rentalId);
    }
    
    public List<Rental> selectAllRentals(){
    	return reservationMapper.selectAllRentals();
    }
    
    public void approve(Long id) {
    	reservationMapper.approve(id);
    }
    public void delete(Long id) {
    	reservationMapper.delete(id);
    }
    public void disapprove(Long id) {
    	reservationMapper.disapprove(id);
    }
    
    public List<RentalWithUser> getReservationsForWeek(int weekOffset) {
        LocalDate today       = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
        LocalDate endOfWeek   = startOfWeek.plusDays(6);

        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end   = endOfWeek.atTime(LocalTime.MAX);

        return reservationMapper.selectByDateRange(start, end);
    }
    
}