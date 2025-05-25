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
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@Tag(name = "Report", description = "신고 API")
@SecurityRequirement(name = "JWT")
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

    @Operation(summary = "신고 등록", description = "새로운 신고를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @PostMapping
    public ResponseEntity<Void> postReport(@Parameter(description = "신고 요청 DTO", required = true)
                                           @Valid @RequestBody ReportDto.Post postDto,
                                           @Parameter(hidden = true) @AuthenticationPrincipal CustomPrincipal customPrincipal) {
        postDto.setReporterId(customPrincipal.getMemberId());
        List<Report> reports = mapper.reportPostToReport(postDto);
        reportService.createReports(reports);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 신고 상세 조회 시, 신고한 채팅이 있다면 함께 조회
    @Operation(summary = "신고 상세 조회", description = "특정 신고의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReportDto.Response.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @GetMapping("/{report-id}")
    public ResponseEntity<SingleResponseDto<ReportDto.Response>> getReport(@Parameter(description = "신고 ID", example = "1")
                                                                           @PathVariable("report-id") @Positive long reportId) {
        Report report = reportService.findVerifiedReport(reportId);
        List<Chat> reportedChats = reportService.findReportDetails(reportId);
        ReportDto.Response response = mapper.reportToReportResponse(report);
        if (!reportedChats.isEmpty()) {
            response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
        }
        return new ResponseEntity<>(new SingleResponseDto<>(response), HttpStatus.OK);
    }

    @Operation(summary = "전체 신고 목록 조회", description = "전체 신고 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReportDto.Response.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 번호",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Bad Request\"}")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<MultiResponseDto<ReportDto.Response>> getReports(@Parameter(description = "페이지 번호(1 이상)", example = "1")
                                                                           @Positive @RequestParam int page,
                                                                           @Parameter(description = "페이지 크기(1 이상)", example = "10")
                                                                           @Positive @RequestParam int size) {
        Page<Report> reportPage = reportService.findReports(page, size);
        List<Report> reports = reportPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(
                mapper.reportsToReportResponses(reports), reportPage
        ), HttpStatus.OK);
    }

    // 신고 처리완료
    @Operation(summary = "신고 처리 완료", description = "관리자가 신고 건을 처리 완료 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 완료",
                    content = @Content(schema = @Schema(implementation = ReportDto.Response.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @PatchMapping("/{report-id}/complete")
    public ResponseEntity<ReportDto.Response> confirmReport(@Parameter(description = "신고 ID", example = "1")
                                                            @Positive @PathVariable("report-id") long reportId) {
        AuthorizationUtils.isAdmin();
        Report report = reportService.completeReport(reportId);
        List<Chat> reportedChats = reportService.findReportDetails(reportId);
        ReportDto.Response response = mapper.reportToReportResponse(report);
        if (!reportedChats.isEmpty()) {
            response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 신고 반려
    @Operation(summary = "신고 반려", description = "관리자가 신고 건을 반려 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "반려 완료",
                    content = @Content(schema = @Schema(implementation = ReportDto.Response.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @PatchMapping("/{report-id}/reject")
    public ResponseEntity<ReportDto.Response> rejectReport(@Parameter(description = "신고 ID", example = "1")
                                                           @Positive @PathVariable("report-id") long reportId) {
        AuthorizationUtils.isAdmin();
        Report report = reportService.rejectReport(reportId);
        List<Chat> reportedChats = reportService.findReportDetails(reportId);
        ReportDto.Response response = mapper.reportToReportResponse(report);
        if (!reportedChats.isEmpty()) {
            response.setReportedChats(chatMapper.chatsToChatResponses(reportedChats));
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "신고 삭제", description = "관리자가 신고 건을 삭제 상태로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":401,\"message\":\"Authentication is required\"}")
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":403,\"message\":\"Access not allowed\"}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":404,\"message\":\"Not Found\"}")
                    )
            )
    })
    @DeleteMapping("/{report-id}")
    public ResponseEntity<Void> deleteReport(@Parameter(description = "신고 ID", example = "1")
                                             @PathVariable("report-id") long reportId) {
        AuthorizationUtils.isAdmin();
        Report report = reportService.findVerifiedReport(reportId);
        report.setReportStatus(Report.ReportStatus.REPORT_DELETED);
        reportService.deleteReport(reportId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
