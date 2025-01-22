package com.springboot.homework;

import com.jayway.jsonpath.JsonPath;
import com.springboot.member.controller.MemberController;
import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import com.springboot.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
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
        // Post로 Data 생성
        //given (준비)
        MemberDto.Post post = new MemberDto.Post("queuwo@gmail.com", "고길동", "010-1234-0987");
        String postContent = gson.toJson(post);
        // 위 코드 -> post에 email, name, phone이 들어간 새로운 객체를 생성하고 Json Type으로 변경
        // perform -> Controller의 핸들러 메서드를 사용하기 위해 사용
        ResultActions postAction =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );

        String location = postAction.andReturn().getResponse().getHeader("Location");

        MemberDto.Patch patch = new MemberDto.Patch();
        patch.setName("홍성민");
        patch.setPhone("010-1231-1234");
        String patchContent = gson.toJson(patch);

        //when (기능 실행)
        // patch 요청을 보내서 데이터 수정
        ResultActions patchAction =
                mockMvc.perform(
                        patch(location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(patchContent)
                );

        //then (비교)
        patchAction
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("홍성민"))
                .andExpect(jsonPath("$.data.phone").value("010-1231-1234"));
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
        // TODO MemberController의 getMembers() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        //given
        MemberDto.Post post1 = new MemberDto.Post("wqqw@gmail.com", "러키킴", "010-1234-5678");
        String postContent1 = gson.toJson(post1);

        MemberDto.Post post2 = new MemberDto.Post("qpoq12@gmail.com", "luckyKim", "010-3924-5178");
        String postContent2 = gson.toJson(post2);

        mockMvc.perform(
                post("/v11/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(postContent1)).andExpect(status().isCreated());

        mockMvc.perform(
                post("/v11/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(postContent2)).andExpect(status().isCreated());

        ResultActions getActions = mockMvc.perform(
                get("/v11/members?page=1&size=10")
                        .param("size", "2")
                        .param("pageInfo","10")
                        .accept(MediaType.APPLICATION_JSON)
        );


        getActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()").value(2))
                .andExpect(jsonPath("$.data[1].email").value(post1.getEmail()))
                .andExpect(jsonPath("$.data[1].name").value(post1.getName()))
                .andExpect(jsonPath("$.data[1].phone").value(post1.getPhone()))
                .andExpect(jsonPath("$.data[0].email").value(post2.getEmail()))
                .andExpect(jsonPath("$.data[0].name").value(post2.getName()))
                .andExpect(jsonPath("$.data[0].phone").value(post2.getPhone()));
    }

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        MemberDto.Post post = new MemberDto.Post("lucky@cat.house", "김러키", "010-1234-9293");
        String postContent = gson.toJson(post);

        ResultActions postAction = mockMvc.perform(
                post("/v11/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(postContent)
        );

        String location = postAction.andReturn().getResponse().getHeader("Location");
        ResultActions deleteAction = mockMvc.perform(
                delete(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(postContent)
        );

        deleteAction
                .andExpect(status().isNoContent());

    }
}

