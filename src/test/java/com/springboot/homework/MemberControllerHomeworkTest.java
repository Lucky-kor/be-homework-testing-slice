package com.springboot.homework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.internal.Utils;
import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.DefaultRequestExpectation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static com.springboot.member.entity.Member.MemberStatus.MEMBER_ACTIVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerHomeworkTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    private ResultActions actions;
    private MemberDto.Post post;
    private String content;

    @BeforeEach
    public void beforeData() throws Exception {
        //given
        post = new MemberDto.Post("test@gmail.com",
                "홍길동",
                "010-1234-5678");
        content = gson.toJson(post);

        // when
        actions = mockMvc.perform(
                post("/v11/members")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
        );
    }

    @Test
    void postMemberTest() throws Exception {
        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))));
    }


    @Test
    void patchMemberTest() throws Exception {
        // TODO MemberController의 patchMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given
        long memberId;
        String location = actions.andReturn().getResponse().getHeader("Location");
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        MemberDto.Patch patch = new MemberDto.Patch(memberId, "test", "010-1111-2222", MEMBER_ACTIVE);

        String patchContent = gson.toJson(patch);

        mockMvc.perform(
                        patch(location)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(patchContent)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(patchContent)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(patch.getName()))
                .andExpect(jsonPath("$.data.phone").value(patch.getPhone()));
    }

    @Test
    void getMemberTest() throws Exception {
        long memberId;
        String location = actions.andReturn().getResponse().getHeader("Location");
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when / then
        mockMvc.perform(
                        get("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(post.getEmail()))
                .andExpect(jsonPath("$.data.name").value(post.getName()))
                .andExpect(jsonPath("$.data.phone").value(post.getPhone()));
    }

    @Test
    void getMembersTest() throws Exception {
        // Given
        List<MemberDto.Post> additionalMembers = Arrays.asList(
                new MemberDto.Post("test2@gmail.com", "김철수", "010-9876-5432"),
                new MemberDto.Post("test3@gmail.com", "이영희", "010-5555-6666"),
                new MemberDto.Post("test4@gmail.com", "박지성", "010-7777-8888")
        );

        //각 추가 회원에 대해 POST 요청을 수행하여 데이터베이스에 저장
        // MockMvc를 사용하여 "/v11/members" 엔드포인트로 POST 요청
        // size , page 파라미터 지정
        for (MemberDto.Post member : additionalMembers) {
            mockMvc.perform(
                    post("/v11/members")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(gson.toJson(member))
            );
        }

        //"/v11/members" 엔드포인트로 GET 요청을 수행
        // 페이지 번호(1)와 페이지 크기(2)를 파라미터로 전달
        // When
        ResultActions getActions = mockMvc.perform(
                get("/v11/members")
                        .param("page", "1")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON)
        );

        //응답을 검증
        // 상태 코드가 200(OK)인지, 데이터가 배열인지, 데이터 크기가 2인지, 페이지 정보가 올바른지 등을 확인
        // Then
        MvcResult result = getActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.pageInfo.page").value(1))
                .andExpect(jsonPath("$.pageInfo.size").value(2))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(greaterThanOrEqualTo(4)))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(greaterThanOrEqualTo(2)))
                .andReturn();

        //응답 본문을 문자열로
        String responseBody = result.getResponse().getContentAsString();

        //응답 본문을 JsonNode 객체로 파싱하고, "data" 필드를 추출
        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        JsonNode dataNode = jsonNode.get("data");

        //모든 회원 데이터를 포함하는 리스트를 생성
        List<MemberDto.Post> allMembers = new ArrayList<>(additionalMembers);
        allMembers.add(0, post);

        //응답 데이터의 각 회원 객체에 대해 필요한 필드(email, name, phone)가 존재하는지 확인
        for (int i = 0; i < dataNode.size(); i++) {
            JsonNode memberNode = dataNode.get(i);
            assertThat(memberNode.has("email")).isTrue();
            assertThat(memberNode.has("name")).isTrue();
            assertThat(memberNode.has("phone")).isTrue();
        }
    }

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        String location = actions.andReturn().getResponse().getHeader("Location");

        ResultActions deleteAction = mockMvc.perform(
                delete(location)
                        .accept(MediaType.APPLICATION_JSON)
        );

        deleteAction.andExpect(status().isNoContent());

        mockMvc.perform(
                get(location)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }
}
