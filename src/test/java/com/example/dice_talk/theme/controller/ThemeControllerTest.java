//package com.example.dice_talk.theme.controller;
//
//import com.example.dice_talk.theme.dto.ThemeDto;
//import com.example.dice_talk.theme.entity.Theme;
//import com.example.dice_talk.theme.mapper.ThemeMapper;
//import com.example.dice_talk.theme.mapper.ThemeMapperImpl;
//import com.example.dice_talk.theme.sevice.ThemeService;
//import com.example.dice_talk.utils.JsonParserUtil;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.auditing.config.AuditingConfiguration;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.io.IOException;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.willDoNothing;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(controllers = ThemeController.class)
//@AutoConfigureMockMvc
//@Import({ThemeMapperImpl.class})
//@MockBean(JpaMetamodelMappingContext.class)
//class ThemeControllerTest {
//
//    @MockBean(name = "jpaAuditingHandler")
//    Object jpaAuditingHandler;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ThemeMapper themeMapper;
//
//    @MockBean
//    private ThemeService themeService;
//
//    @MockBean
//    private JsonParserUtil jsonParserUtil;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private final Theme theme = Theme.builder()
//            .themeId(1L)
//            .name("테스트테마")
//            .description("설명")
//            .image("image-url")
//            .themeStatus(Theme.ThemeStatus.THEME_ON)
//            .build();
//
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    @DisplayName("테마 등록")
//    void postTheme() throws Exception {
//        ThemeDto.Post postDto = new ThemeDto.Post("테스트테마", "설명");
//
//        given(jsonParserUtil.parse(anyString(), eq(ThemeDto.Post.class)))
//                .willReturn(postDto);
//        given(themeMapper.themePostToTheme(any())).willReturn(theme);
//        given(themeService.createTheme(any(), any())).willReturn(theme);
//
//        MockMultipartFile themePart = new MockMultipartFile("themePostDto", "", "application/json",
//                objectMapper.writeValueAsBytes(postDto));
//
//        mockMvc.perform(multipart("/themes")
//                        .file(themePart)
//                        .with(csrf())
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isCreated());
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    @DisplayName("테마 수정")
//    void patchThemeTest() throws Exception {
//        ThemeDto.Patch patchDto = new ThemeDto.Patch(1L, "수정테마", "수정설명", "image-url", Theme.ThemeStatus.THEME_ON);
//        ThemeDto.Response responseDto = new ThemeDto.Response(1L, "수정테마", "수정설명", "image-url", Theme.ThemeStatus.THEME_ON);
//
//        given(jsonParserUtil.parse(anyString(), eq(ThemeDto.Patch.class))).willReturn(patchDto);
//        given(themeMapper.themePatchToTheme(any())).willReturn(theme);
//        given(themeService.updateTheme(any(), any())).willReturn(theme);
//        given(themeMapper.themeToThemeResponse(any())).willReturn(responseDto);
//
//        MockMultipartFile patchPart = new MockMultipartFile("themePatchDto", "", "application/json",
//                objectMapper.writeValueAsBytes(patchDto));
//
//        mockMvc.perform(multipart("/themes/{theme-id}", 1L)
//                        .file(patchPart)
//                        .with(request -> {
//                            request.setMethod("PATCH");
//                            return request;
//                        })
//                        .with(csrf())
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.themeId").value(1L));
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    @DisplayName("회원용 테마 리스트 조회")
//    void getThemesTest() throws Exception {
//        List<Theme> themeList = List.of(theme);
//        List<ThemeDto.Response> responseList = List.of(new ThemeDto.Response(1L, "테스트테마", "설명", "image-url", Theme.ThemeStatus.THEME_ON));
//
//        given(themeService.findAllThemesNotClose()).willReturn(themeList);
//        given(themeMapper.themesToThemeResponses(themeList)).willReturn(responseList);
//
//        mockMvc.perform(get("/themes"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data[0].themeId").value(1L));
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    @DisplayName("관리자용 전체 조회")
//    void getAllThemesTest() throws Exception {
//        Page<Theme> themePage = new PageImpl<>(List.of(theme));
//        List<ThemeDto.Response> responseList = List.of(new ThemeDto.Response(1L, "테스트테마", "설명", "image-url", Theme.ThemeStatus.THEME_ON));
//
//        given(themeService.findThemes(anyInt(), anyInt(), any())).willReturn(themePage);
//        given(themeMapper.themesToThemeResponses(any())).willReturn(responseList);
//
//        mockMvc.perform(get("/themes/admin")
//                        .param("page", "1")
//                        .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data[0].themeId").value(1L));
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    @DisplayName("테마 상세 조회")
//    void getThemeTest() throws Exception {
//        ThemeDto.Response responseDto = new ThemeDto.Response(1L, "테스트테마", "설명", "image-url", Theme.ThemeStatus.THEME_ON);
//
//        given(themeService.findTheme(1L)).willReturn(theme);
//        given(themeMapper.themeToThemeResponse(theme)).willReturn(responseDto);
//
//        mockMvc.perform(get("/themes/{theme-id}", 1L))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.themeId").value(1L));
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    @DisplayName("테마 삭제")
//    void deleteThemeTest() throws Exception {
//        willDoNothing().given(themeService).deleteTheme(1L);
//
//        mockMvc.perform(delete("/themes/{theme-id}", 1L).with(csrf()))
//                .andExpect(status().isNoContent());
//    }
//}