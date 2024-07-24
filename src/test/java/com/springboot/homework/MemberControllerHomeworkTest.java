package com.springboot.homework;

import com.jayway.jsonpath.JsonPath;
import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import com.springboot.member.entity.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com",
                "홍길동",
                "010-1234-5678");

        String content = gson.toJson(post);

        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        String location = actions.andReturn().getResponse().getHeader("Location");

        MemberDto.Patch patch = new MemberDto.Patch(1L,"렉돌","010-7777-6666", Member.MemberStatus.MEMBER_ACTIVE);

        String patchContent = gson.toJson(patch);

        // when
        ResultActions patchAction = mockMvc.perform(
                patch(location)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchContent));

        // then
        patchAction
                .andExpect(status().isOk())
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
        List<MemberDto.Post> postMembers = List.of(
                new MemberDto.Post("munchkin@cute.cat","먼치킨","010-1111-1111"),
                new MemberDto.Post("munchkin2@cute.cat","먼치킨2","010-1111-1112")
                );

        for(MemberDto.Post postMember : postMembers){

            String postContent = gson.toJson(postMember);

            mockMvc.perform(
                    post("/v11/members")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(postContent)
            );
        }

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("page", "1");
        queryParams.add("size", "5");

        // when / then
        ResultActions actions = mockMvc.perform(
                        get("/v11/members")
                                //.param("page","1")
                                //.param("size","5")
                                .params(queryParams)
                                .accept(MediaType.APPLICATION_JSON)
                );
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value(postMembers.get(1).getEmail()))
                .andExpect(jsonPath("$.data[0].name").value(postMembers.get(1).getName()))
                .andExpect(jsonPath("$.data[0].phone").value(postMembers.get(1).getPhone()))
                .andExpect(jsonPath("$.data[1].email").value(postMembers.get(0).getEmail()))
                .andExpect(jsonPath("$.data[1].name").value(postMembers.get(0).getName()))
                .andExpect(jsonPath("$.data[1].phone").value(postMembers.get(0).getPhone()))
                .andExpect(jsonPath("$.pageInfo.page").value(queryParams.getFirst("page")))
                .andExpect(jsonPath("$.pageInfo.size").value(queryParams.getFirst("size")));

//        MvcResult result = actions.andExpect(status().isOk())
//                .andExpect(jsonPath("$.data").isArray())
//                .andReturn();
//
//        List list = JsonPath.parse(result.getResponse().getContentAsString()).read("$.data");
//
//        assertEquals(list.size(),postMembers.size());
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
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );

        String location = postActions.andReturn().getResponse().getHeader("Location");

        // when / then
        ResultActions deleteActions =
                mockMvc.perform(delete(location));

        deleteActions
                .andExpect(status().isNoContent());
    }
}
