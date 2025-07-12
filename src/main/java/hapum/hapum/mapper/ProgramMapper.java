package hapum.hapum.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import hapum.hapum.domain.Program;
import hapum.hapum.domain.ProgramAdd;
import hapum.hapum.domain.ProgramSub;
import hapum.hapum.domain.ProgramWithSub;

@Mapper
public interface ProgramMapper {

	public void insertProgram(Program program); 
	List<Program> selectAllPrograms();
	Program selectProgramById(Long id);
	public Integer getApplyCount(@Param("programId") Long programId);
	
	public int programSub(ProgramSub ps);
	List<ProgramSub> selectPrograSubmById(@Param("programId") Long programId);
	 List<ProgramSub> selectByProgramIdWithUser(Long programId);
	 
	 void approve(@Param("psId") Long psId);
	 void cancel(@Param("psId") Long psId);
	 
	 List<ProgramWithSub> selectProgramsWithSubByUserId(@Param("userId")Long userId);
	 void deleteByProgramSubsId(@Param("id") Long subsId);
	 
	 void updateCode(@Param("code") String code, @Param("programId") Long programId);
	 
	 void insertProgramAdd(ProgramAdd programAdd);
	 List<ProgramAdd> selectAllProgramAdd();
	 List<Program> selectProgramByAddId(@Param("id")Long id);
	 
	 List<Program> selectThisMonthProgram();
	 List<Program> selectProgramsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
	 
	 ProgramAdd selectProgramAddById(@Param("addId") Long addId);
	 void updateProgramAddStatus(@Param("addId") Long addId, @Param("code") String code);
	 void updateProgram(Program program);
	 void updateProgramAdd(ProgramAdd programAdd); 
	 
	 void deleteProgramSubs(@Param("id")Long id);
	 void deleteProgram(@Param("id") Long id);
	 void deleteProgramAdd(@Param("id") Long id);
}
