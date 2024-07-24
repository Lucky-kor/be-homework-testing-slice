package com.springboot.homework;

import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import com.springboot.member.dto.MemberPatchDto;
import com.springboot.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerHomeworkTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @Test
    void postMemberTest() throws Exception {
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com",
                "홍길동",
                "010-1234-5678");
        String content = gson.toJson(post);


        // when
        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))));
    }

    @Test
    void patchMemberTest() throws Exception {
        // TODO MemberController의 patchMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given
        // 새로운 포스트dto 객체 생성후 데이터 넣어줌
        MemberDto.Post post = new MemberDto.Post("mando123@gmail.com", "만두", "010-4333-2233");
        // dto 객체를 json 타입으로 변환해줌
        String postContent = gson.toJson(post);

        // mockMvc가 post 요청
        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members") // mockMvc로 /v11/members 에 post를 할거임
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );

        // `postActions` 객체를 통해 요청에 대한 응답을 가져옴
        // `andReturn().getResponse()`를 호출하여 응답 객체를 얻음
        // `getHeader("Location")`을 호출하여 응답 헤더에서 "Location" 값을 추출
        String location = postActions.andReturn().getResponse().getHeader("Location");

        // patchDto 객체를 생성후 수정할 데이터를 넣어줌
        MemberDto.Patch patch = MemberDto.Patch.builder()
                .phone("010-1313-4242")
                .build();

        // dto 객체를 json 타입으로 변환
        String patchContent = gson.toJson(patch);

        URI patchUri = UriComponentsBuilder.newInstance().path(location).build().toUri();

        // mockMvc가 patch 요청
        ResultActions patchActions =
                mockMvc.perform(
                        patch(patchUri) // 아까 응답 헤더에서 추출한 Location
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(patchContent)
                );
                        patchActions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.name").value(patch.getName())) // JSON 문서에서 데이터를 추출하기 위한 표준 경로 언어
                        .andExpect(jsonPath("$.data.phone").value(patch.getPhone()));
    }

    @Test
    void getMemberTest() throws Exception {
        // given: MemberController의 getMember()를 테스트하기 위해서 postMember()를 이용해 테스트 데이터를 생성 후, DB에 저장
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );
        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
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
        // given: 여러 회원을 생성하기 위한 DTO 리스트
        List<MemberDto.Post> postList = List.of(
                new MemberDto.Post("mando@gmail.com", "만두", "010-3030-4040"),
                new MemberDto.Post("ari@gmail.com", "아리", "010-2020-1111"),
                new MemberDto.Post("ggugu@gmail.com", "구구", "010-2222-5566")
        );

        // 각 DTO를 JSON 문자열로 변환하여 POST 요청을 보냄
        for (MemberDto.Post post : postList) {
            String postContent = gson.toJson(post);
            mockMvc.perform(
                    post("/v11/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(postContent)
            ).andExpect(status().isCreated());
        }

        // 페이지네이션을 위한 파라미터 설정
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("page", "1");
        multiValueMap.add("size", "5");

        // when: GET 요청을 통해 회원 목록을 가져옴
        ResultActions resultActions = mockMvc.perform(
                get("/v11/members")
                        .params(multiValueMap)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then: 응답 상태와 JSON 응답 내용을 검증
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3))) // 생성된 회원의 수가 3인지 확인
                .andExpect(jsonPath("$.data[0].email").value(postList.get(0).getEmail()))
                .andExpect(jsonPath("$.data[0].name").value(postList.get(0).getName()))
                .andExpect(jsonPath("$.data[0].phone").value(postList.get(0).getPhone()))
                .andExpect(jsonPath("$.data[1].email").value(postList.get(1).getEmail()))
                .andExpect(jsonPath("$.data[1].name").value(postList.get(1).getName()))
                .andExpect(jsonPath("$.data[1].phone").value(postList.get(1).getPhone()))
                .andExpect(jsonPath("$.data[2].email").value(postList.get(2).getEmail()))
                .andExpect(jsonPath("$.data[2].name").value(postList.get(2).getName()))
                .andExpect(jsonPath("$.data[2].phone").value(postList.get(2).getPhone()));
    }


    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
    }
}
