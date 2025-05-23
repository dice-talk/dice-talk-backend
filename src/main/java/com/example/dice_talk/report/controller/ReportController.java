package com.example.dice_talk.report.controller;

import com.example.dice_talk.auth.CustomPrincipal;
import com.example.dice_talk.chat.dto.ChatDto;
import com.example.dice_talk.chat.entity.Chat;
import com.example.dice_talk.chat.mapper.ChatMapper;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.report.entity.Report;
import com.example.dice_talk.report.mapper.ReportMapper;
import com.example.dice_talk.report.service.ReportService;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final ChatMapper chatMapper;

    public ReportController(ReportService reportService, ReportMapper mapper, ChatMapper chatMapper) {
        this.reportService = reportService;
        this.mapper = mapper;
        this.chatMapper = chatMapper;
    }

    @PostMapping
    public ResponseEntity postReport(@Valid @RequestBody ReportDto.Post postDto,
                                     @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal){
        postDto.setReporterId(customPrincipal.getMemberId());
        List<Report> reports = mapper.reportPostToReport(postDto);
        reportService.createReports(reports);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 신고 상세 조회 시, 신고한 채팅이 있다면 함께 조회
    @GetMapping("/{report-id}")
    public ResponseEntity getReport(@PathVariable("report-id") @Positive long reportId){
        Report report = reportService.findVerifiedReport(reportId);
        List<Chat> reportedChats = reportService.findReportDetails(reportId);
        ReportDto.Response response = mapper.reportToReportResponse(report);
        if(!reportedChats.isEmpty()){
            response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
        }
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getReports(@Positive @RequestParam int page, @Positive @RequestParam int size){
        Page<Report> reportPage = reportService.findReports(page, size);
        List<Report> reports = reportPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(
                mapper.reportsToReportResponses(reports), reportPage
        ), HttpStatus.OK);
    }

    // 신고 처리완료
    @PatchMapping("/{report-id}/complete")
    public ResponseEntity confirmReport(@Positive @PathVariable("report-id") long reportId){
        AuthorizationUtils.isAdmin();
        Report report = reportService.completeReport(reportId);
        return new ResponseEntity<>(mapper.reportToReportResponse(report), HttpStatus.OK);
    }

    // 신고 반려
    @PatchMapping("/{report-id}/reject")
    public ResponseEntity rejectReport(@Positive @PathVariable("report-id") long reportId){
        AuthorizationUtils.isAdmin();
        Report report = reportService.rejectReport(reportId);
        return new ResponseEntity<>(mapper.reportToReportResponse(report), HttpStatus.OK);
    }

    @DeleteMapping("/{report-id}")
    public ResponseEntity deleteReport(@PathVariable("report-id") long reportId){
        AuthorizationUtils.isAdmin();
        Report report = reportService.findVerifiedReport(reportId);
        report.setReportStatus(Report.ReportStatus.REPORT_DELETED);
        reportService.deleteReport(reportId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
