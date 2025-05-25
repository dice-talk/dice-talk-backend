package com.example.dice_talk.theme.controller;

import com.example.dice_talk.dto.ListResponseDto;
import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.item.dto.ItemDto;
import com.example.dice_talk.item.entity.Item;
import com.example.dice_talk.report.dto.ReportDto;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.theme.dto.ThemeDto;
import com.example.dice_talk.theme.entity.Theme;
import com.example.dice_talk.theme.mapper.ThemeMapper;
import com.example.dice_talk.theme.sevice.ThemeService;
import com.example.dice_talk.utils.AuthorizationUtils;
import com.example.dice_talk.utils.JsonParserUtil;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Tag(name = "Theme", description = "테마 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/themes")
@Validated
@Slf4j
public class ThemeController {
    private final static String THEME_DEFAULT_URL = "/themes";
    private final ThemeService themeService;
    private final ThemeMapper mapper;
    private final JsonParserUtil jsonParserUtil;

    public ThemeController(ThemeService themeService, ThemeMapper mapper, JsonParserUtil jsonParserUtil) {
        this.themeService = themeService;
        this.mapper = mapper;
        this.jsonParserUtil = jsonParserUtil;
    }

    @Operation(summary = "테마 등록", description = "새로운 테마를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),

            @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> postTheme(@Parameter(description = "테마 생성 DTO 문자열(JSON)")
                                          @Valid @RequestParam("themePostDto") String themePostDtoString,
                                          @Parameter(description = "테마 이미지 파일", required = false)
                                          @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        AuthorizationUtils.isAdmin();
        ThemeDto.Post postDto = jsonParserUtil.parse(themePostDtoString, ThemeDto.Post.class);
        Theme createdTheme = themeService.createTheme(mapper.themePostToTheme(postDto), imageFile);
        URI location = UriCreator.createUri(THEME_DEFAULT_URL, createdTheme.getThemeId());
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "테마 수정", description = "기존에 등록된 테마를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ThemeDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),

            @ApiResponse(responseCode = "403", description = "등록 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),

            @ApiResponse(responseCode = "404", description = "존재하지 않는 테마 요청시",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"The requested resource could not be found.\"}")))
    })
    @PatchMapping(value = "/{theme-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleResponseDto<ThemeDto.Response>> patchTheme(
            @Parameter(description = "테마 ID", example = "1")
            @PathVariable("theme-id") @Positive long themeId,
            @Parameter(description = "테마 수정 DTO 문자열(JSON)")
            @Valid @RequestParam("themePatchDto") String themePatchDtoString,
            @Parameter(description = "테마 이미지 파일", required = false)
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        ThemeDto.Patch patchDto = jsonParserUtil.parse(themePatchDtoString, ThemeDto.Patch.class);
        patchDto.setThemeId(themeId);
        Theme theme = themeService.updateTheme(mapper.themePatchToTheme(patchDto), imageFile);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.themeToThemeResponse(theme)), HttpStatus.OK);
    }

    // 활성화 상태의 테마 목록 조회 (회원용)
    @Operation(summary = "운영중인 테마 목록 조회", description = "현재 운영중인 테마의 목록을 조회 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ThemeDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),

            @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),
    })
    @GetMapping
    public ResponseEntity<ListResponseDto<ThemeDto.Response>> getThemes() {
        List<Theme> themes = themeService.findAllThemesOn();
        return new ResponseEntity<>(new ListResponseDto<>(mapper.themesToThemeResponses(themes)), HttpStatus.OK);
    }

    // 비활성화 상태의 테마까지 조회 (관리자용)
    @Operation(summary = "관리자용 전체 테마 목록 조회", description = "전체 테마 목록을 조회합니다.(비활성화 포함)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ThemeDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),

            @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),
    })
    @GetMapping("/admin")
    public ResponseEntity<MultiResponseDto<ThemeDto.Response>> getAllThemes(@Parameter(description = "페이지 번호(1 이상)", example = "1")
                                                                            @Positive @RequestParam int page,
                                                                            @Parameter(description = "페이지 크기(1 이상)", example = "10")
                                                                            @Positive @RequestParam int size) {
        Page<Theme> themePage = themeService.findThemes(page, size);
        List<Theme> themes = themePage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.themesToThemeResponses(themes), themePage), HttpStatus.OK);
    }

    @Operation(summary = "테마 상세 조회", description = "특정 테마를 상세조회 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ThemeDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),

            @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),

            @ApiResponse(responseCode = "404", description = "존재하지 않는 테마 요청시",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"The requested resource could not be found.\"}")))
    })
    @GetMapping("/{theme-id}")
    public ResponseEntity<SingleResponseDto<ThemeDto.Response>> getTheme(
            @Parameter(description = "테마 ID", example = "1")
            @PathVariable("theme-id") @Positive long themeId) {
        Theme theme = themeService.findTheme(themeId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.themeToThemeResponse(theme)), HttpStatus.OK);
    }

    @Operation(summary = "테마 삭제", description = "특정 테마를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),

            @ApiResponse(responseCode = "403", description = "삭제 권한 없음",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),

            @ApiResponse(responseCode = "404", description = "존재하지 않는 테마 요청시",
                    content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                            examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"The requested resource could not be found.\"}")))
    })
    @DeleteMapping("/{theme-id}")
    public ResponseEntity<Void> deleteTheme(@Parameter(description = "테마 식별자", example = "1")
                                            @PathVariable("theme-id") long themeId) {
        themeService.deleteTheme(themeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
