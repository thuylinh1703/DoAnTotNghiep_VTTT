package com.tuixach.lvt.repository;

import com.tuixach.lvt.entity.DailyRevenueSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRevenueSummaryRepository extends JpaRepository<DailyRevenueSummary, Long> {

    Optional<DailyRevenueSummary> findByDate(LocalDate date);

    @Query("SELECT s FROM DailyRevenueSummary s WHERE s.date BETWEEN :start AND :end ORDER BY s.date ASC")
    List<DailyRevenueSummary> findAllBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(s.netRevenue) FROM DailyRevenueSummary s WHERE s.date BETWEEN :start AND :end")
    Optional<java.math.BigDecimal> sumNetRevenueBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
