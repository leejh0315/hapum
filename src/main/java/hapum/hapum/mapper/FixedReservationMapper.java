package hapum.hapum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import hapum.hapum.domain.BlockedDay;
import hapum.hapum.domain.FixedReservation;
import hapum.hapum.domain.Rental;

public interface FixedReservationMapper {
    List<FixedReservation> selectAllFixedReservations();
    List<BlockedDay> selectBlockedDays();
    List<Rental> selectRentals();
    
    void insertFixedReservation(FixedReservation fixedReservation);
    void updateFixedReservation(FixedReservation fixedReservation); 
    void deleteFixedReservation(@Param("id")Long id);
    
    void insertBlockedDay(BlockedDay blockedDay);
    void updateBlockedDay(BlockedDay blockedDay); 
    void deleteBlockedDay(Long id);
    
    void insertRental(Rental rental);
    
    List<Rental> selectByUserId (@Param("userId")Long userId);
    
    void deleteRental(@Param("id")Long rentalId);
    
    List<Rental> selectAllRentals();
    
    void approve(@Param("id")Long id);
    void delete(@Param("id")Long id);
    void disapprove(@Param("id")Long id);
} 