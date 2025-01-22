package com.springboot.homework;

import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        //given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );
        MemberDto.Patch patch = new MemberDto.Patch("송호근","010-3055-6379");
        String patchContent = gson.toJson(patch);
        String location = postActions.andReturn().getResponse().getHeader("Location");

        //when
        ResultActions patchActions =
                mockMvc.perform(
                        patch(location)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(patchContent)
                );
        //then
        patchActions.andExpect(status().isOk())
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

        //given
        List<MemberDto.Post> posts = new ArrayList<>();
        posts.add(new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111"));
        posts.add(new MemberDto.Post("hgd@naver.com","김럭키","010-1211-1111"));
        posts.add(new MemberDto.Post("hgd@daum.com","한국","010-1311-1111"));
        posts.add(new MemberDto.Post("hgd@kakao.com","송호근","010-1411-1111"));

        List<String> postContents = posts.stream()
                .map(post -> gson.toJson(post))
                .collect(Collectors.toList());
//        postContents.stream()
//                .map(postContent-> {
//                            try {
//                                return mockMvc.perform(
//                                        post("/v11/members")
//                                                .contentType(MediaType.APPLICATION_JSON)
//                                                .accept(MediaType.APPLICATION_JSON)
//                                                .content(postContent)
//                                );
//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                ).collect(Collectors.toList());
        postContents.stream()
                .forEach(postContent-> {
                            try {
                                mockMvc.perform(
                                        post("/v11/members")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .accept(MediaType.APPLICATION_JSON)
                                                .content(postContent)
                                );
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
        //when, then
        ResultActions getActions = mockMvc.perform(
                get("/v11/members")
                        .param("page", "1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON)
        );

        getActions.andExpect(status().isOk());

        int postsLength = posts.size() - 1;
        for(int i = 0; i < postsLength; i++){
            getActions.andExpect(jsonPath("$.data[" + i + "].name" ).value(posts.get(postsLength-i).getName()));
        }
    }

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );

        String location = postActions.andReturn().getResponse().getHeader("Location");

        ResultActions deleteActions =
                mockMvc.perform(
                        delete(location)
                                .accept(MediaType.APPLICATION_JSON)
                );

        deleteActions.andExpect(status().isNoContent());
    }
}
