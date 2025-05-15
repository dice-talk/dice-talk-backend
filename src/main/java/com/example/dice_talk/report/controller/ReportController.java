package com.example.dice_talk.report.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.Report;
import com.example.dice_talk.report.mapper.ReportMapper;
import com.example.dice_talk.report.service.ReportService;
import com.example.dice_talk.utils.UriCreator;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final static String REPORT_DEFAULT_URL = "/reports";
    private final ReportService reportService;
    private final ReportMapper mapper;

    public ReportController(ReportService reportService, ReportMapper mapper) {
        this.reportService = reportService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postReport(@Valid @RequestBody ReportDto.Post postDto,
                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        postDto.setReporterId(customPrincipal.getMemberId());
        Report report = mapper.reportPostToReport(postDto);
        Report createdReport = reportService.createReport(report, customPrincipal.getMemberId());
        URI location = UriCreator.createUri(REPORT_DEFAULT_URL, createdReport.getReportId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{report-id}")
    public ResponseEntity getReport(@PathVariable("report-id") @Positive long reportId){
        Report report = reportService.findReport(reportId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.reportToReportResponse(report)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getReports(@Positive @RequestParam int page, @Positive @RequestParam int size){
        Page<Report> reportPage = reportService.findReports(page, size);
        List<Report> reports = reportPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(
                mapper.reportsToReportResponses(reports), reportPage
        ), HttpStatus.OK);
    }

    @DeleteMapping("/{report-id}")
    public ResponseEntity deleteReport(@PathVariable("report-id") long reportId){
        Report report = reportService.findReport(reportId);
        report.setReportStatus(Report.ReportStatus.REPORT_DELETED);
        reportService.updateReport(report);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
