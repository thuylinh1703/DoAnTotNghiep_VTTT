package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.ApiResponse;
import com.tuixach.lvt.dto.ProductPerformanceDTO;
import com.tuixach.lvt.dto.ReportDTO;
import com.tuixach.lvt.dto.RevenueBreakdownDTO;
import com.tuixach.lvt.dto.RevenuePointDTO;
import com.tuixach.lvt.dto.RevenueSummaryDTO;
import com.tuixach.lvt.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<java.util.List<RevenuePointDTO>>> getRevenueSeries(
            @RequestParam(defaultValue = "day") String groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getRevenueSeries(from, to, groupBy)));
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<ReportDTO>> getRevenueOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getRevenueReport(start, end)));
    }

    @GetMapping("/revenue/summary")
    public ResponseEntity<ApiResponse<RevenueSummaryDTO>> getRevenueSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getRevenueSummary()));
    }

    @GetMapping("/revenue/by-payment-method")
    public ResponseEntity<ApiResponse<java.util.List<RevenueBreakdownDTO>>> getRevenueByPaymentMethod(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getRevenueByPaymentMethod(from, to)));
    }

    @GetMapping("/revenue/by-category")
    public ResponseEntity<ApiResponse<java.util.List<RevenueBreakdownDTO>>> getRevenueByCategory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getRevenueByCategory(from, to)));
    }

    @GetMapping("/products/top-sellers")
    public ResponseEntity<ApiResponse<java.util.List<ProductPerformanceDTO>>> getTopSellers(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getTopSellers(period, limit)));
    }

    @GetMapping("/products/slow-movers")
    public ResponseEntity<ApiResponse<java.util.List<ProductPerformanceDTO>>> getSlowMovers(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "3") int threshold) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getSlowMovers(limit, threshold)));
    }

    @GetMapping("/products/zero-sales")
    public ResponseEntity<ApiResponse<java.util.List<ProductPerformanceDTO>>> getZeroSales(
            @RequestParam(defaultValue = "60") int days,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getZeroSales(days, limit)));
    }

    @GetMapping("/summary/force-update")
    public ResponseEntity<ApiResponse<Void>> forceUpdateSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        reportService.updateDailySummary(date);
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật dữ liệu cho ngày " + date, null));
    }
}
