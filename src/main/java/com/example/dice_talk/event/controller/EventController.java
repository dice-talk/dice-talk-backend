package com.example.dice_talk.event.controller;

import com.example.dice_talk.dto.MultiResponseDto;
import com.example.dice_talk.dto.SingleResponseDto;
import com.example.dice_talk.event.dto.EventDto;
import com.example.dice_talk.event.entity.Event;
import com.example.dice_talk.event.mapper.EventMapper;
import com.example.dice_talk.event.service.EventService;
import com.example.dice_talk.response.SwaggerErrorResponse;
import com.example.dice_talk.utils.UriCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@Tag(name = "Event API", description = "이벤트 관련 API")
@SecurityRequirement(name = "JWT")
@RestController
@RequestMapping("/events")
public class EventController {
    private final static String EVENT_DEFAULT_URL = "/events";
    private final EventService eventService;
    private final EventMapper mapper;

    public EventController(EventService eventService, EventMapper mapper) {
        this.eventService = eventService;
        this.mapper = mapper;
    }

    @Operation(summary = "새로운 이벤트 등록", description = "관리자가 새로운 이벤트를 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "이벤트 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 인증번호",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"BAD REQUEST\", \"message\": \"Invalid request format\"}")))}
    )
    @PostMapping
    public ResponseEntity<Void> postEvent(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "이벤트 등록 요청 본문",
                                    required = true, content = @Content(schema = @Schema(implementation = EventDto.Post.class)))
                                        @Valid @RequestBody EventDto.Post postDto){
        Event event = mapper.eventPostToEvent(postDto);
        Event createdEvent = eventService.createEvent(event);
        URI location = UriCreator.createUri(EVENT_DEFAULT_URL, createdEvent.getEventId());
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "이벤트 수정", description = "관리자가 기존에 등록된 이벤트를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이벤트 수정 성공",
                            content = @Content(schema = @Schema(implementation = EventDto.Response.class))),
                    @ApiResponse(responseCode = "403", description = "수정 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}"))),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 이벤트 수정 요청",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"NOT_FOUND\", \"message\": \"The requested resource could not be found.\"}")))}
    )
    @PatchMapping("/{event-id}")
    public ResponseEntity<SingleResponseDto<EventDto.Response>> patchEvent(@Parameter(name = "event-id", description = "수정할 이벤트의 ID", example = "101")
                                                                               @PathVariable("event-id") @Positive long eventId,
                                                                           @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "이벤트 수정 요청 본문",
                                                                                   required = true, content = @Content(schema = @Schema(implementation = EventDto.Patch.class)))
                                                                           @Valid @RequestBody EventDto.Patch patchDto){
        patchDto.setEventId(eventId);
        Event event = eventService.updateEvent(mapper.eventPatchToEvent(patchDto));
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.eventToEventResponse(event)), HttpStatus.OK);
    }

    @Operation(summary = "이벤트 조회", description = "관리자가 전체 이벤트 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이벤트 조회 성공",
                            content = @Content(schema = @Schema(implementation = EventDto.Response.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping
    public ResponseEntity<MultiResponseDto<EventDto.Response>> getEvents(@Parameter(name = "page", description = "조회할 페이지 번호 (1부터 시작)", example = "1")
                                        @Positive @RequestParam int page,
                                    @Parameter(name = "size", description = "한 페이지당 항목 수", example = "10")
                                        @Positive @RequestParam int size){
        Page<Event> eventPage = eventService.findEvents(page, size);
        List<Event> events = eventPage.getContent();
        return new ResponseEntity<>(new MultiResponseDto<>(mapper.eventsToEventResponses(events), eventPage), HttpStatus.OK);
    }

    @Operation(summary = "이벤트 상세 조회", description = "관리자가 전체 이벤트를 상세 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이벤트 조회 성공",
                            content = @Content(schema = @Schema(implementation = EventDto.Response.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "조회 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @GetMapping("/{event-id}")
    public ResponseEntity<SingleResponseDto<EventDto.Response>> getEvent(@Parameter(name = "event-id", description = "상세 조회할 이벤트의 ID", example = "101")
                                       @PathVariable("event-id") @Positive long eventId){
        Event event = eventService.findEvent(eventId);
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.eventToEventResponse(event)), HttpStatus.OK);
    }

    @Operation(summary = "이벤트 비활성화", description = "관리자가 특정 이벤트의 상태를 비활성화로 변경합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "이벤트 상태 변경 성공"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 접근",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"UNAUTHORIZED\", \"message\": \"Authentication is required to access this resource.\"}"))),
                    @ApiResponse(responseCode = "403", description = "상태 변경 권한 없음",
                            content = @Content(schema = @Schema(implementation = SwaggerErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"error\": \"FORBIDDEN\", \"message\": \"Access not allowed\"}")))}
    )
    @DeleteMapping("/{event-id}")
    public ResponseEntity<Void> deleteEvent(@Parameter(name = "event-id", description = "비활성화 이벤트의 ID", example = "101")
                                          @PathVariable("event-id") long eventId){
        eventService.deleteEvent(eventId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
