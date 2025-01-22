package com.springboot.homework;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

//        String name;
//        String locationBody = postActions.andReturn().getResponse().getContentAsString();
//        name = String.format(locationBody.substring(locationBody.lastIndexOf()))

        MemberDto.Patch patch = new MemberDto.Patch(memberId, "후쿠오카", "010-9876-5433", Member.MemberStatus.MEMBER_ACTIVE);
        String patchContent = gson.toJson(patch);

        ResultActions patchActions =
                mockMvc.perform(
                        patch("/v11/members/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(patchContent)
                );
        // when

        // then
        mockMvc.perform(
                        get("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberStatus").value(patch.getMemberStatus().getStatus()))
                .andExpect(jsonPath("$.data.name").value(patch.getName()))
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

//            "/v11/members?page=2&size={sizeId}"

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
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
//                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );

        MemberDto.Post post2 = new MemberDto.Post("hihi@gmail.com","응애","010-2222-1111");
        String postContent2 = gson.toJson(post2);

        ResultActions postActions2 =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
//                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent2)
                );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page","1");
        params.add("size","10");

        // when

        // then
        mockMvc.perform(
                        get("/v11/members")
                                .params(params)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value(post2.getEmail()))
                .andExpect(jsonPath("$.data[0].name").value(post2.getName()))
                .andExpect(jsonPath("$.data[0].phone").value(post2.getPhone()))
                .andDo(print());
    }

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
//                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );

        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        MemberDto.Post post2 = new MemberDto.Post("hihi@gmail.com","응애","010-2222-1111");
        String postContent2 = gson.toJson(post2);

        ResultActions postActions2 =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
//                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent2)
                );


        // when


        // then
        ResultActions deleteActions =
                mockMvc.perform(
                        delete("/v11/members/" + memberId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                );
        deleteActions.andExpect(status().isNoContent());

    }
}
