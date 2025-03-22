package com.example.dice_talk.report.controller;

import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.Report;
import com.example.dice_talk.report.mapper.ReportMapper;
import com.example.dice_talk.report.service.ReportService;
import com.example.dice_talk.utils.UriCreator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/report")
public class ReportController {
    private final static String REPORT_DEFAULT_URL = "/report";
    private final ReportService reportService;
    private final ReportMapper mapper;

    public ReportController(ReportService reportService, ReportMapper mapper) {
        this.reportService = reportService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postReport(@Valid @RequestBody ReportDto.Post postDto, long memberId){
        Report report = mapper.reportPostToReport(postDto);
        report.setReporterId(memberId);
        Report createdReport = reportService.createReport(report, memberId);
        URI location = UriCreator.createUri(REPORT_DEFAULT_URL, createdReport.getReportId());
        return ResponseEntity.created(location).build();
    }
}
