package com.springboot.homework;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import com.springboot.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        // PatchDto에 null이 들어올 수 있는가?
        // TODO MemberController의 patchMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given 수정하려면 회원 있어야함
        // 회원 등록 먼저
        MemberDto.Post post = new MemberDto.Post("taekho1225@gmail.com",
                "택호",
                "010-2401-5110");
        String content = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );
        // postActions에서 location 가져와야함
        String location = postActions.andReturn().getResponse().getHeader("Location");
        // 회원 수정할 PatchDto 있어야함
        MemberDto.Patch patchDto = new MemberDto.Patch(
                0, "택호", "010-2401-5118", Member.MemberStatus.MEMBER_SLEEP
        );

        String patchContent = gson.toJson(patchDto);

        // when
        ResultActions patchActions = mockMvc.perform(
                patch(location)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchContent)
        );


        // then
        patchActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(patchDto.getMemberId()))
                .andExpect(jsonPath("$.data.name").value(patchDto.getName()))
                .andExpect(jsonPath("$.data.phone").value(patchDto.getPhone()))
                .andExpect(jsonPath("$.data.memberStatus").value(patchDto.getMemberStatus().getStatus()));
    }

    @Test
    void getMemberTest() throws Exception {
        // given: MemberController의 getMember()를 테스트하기 위해서 postMember()를 이용해 테스트 데이터를 생성 후, DB에 저장
        MemberDto.Post post = new MemberDto.Post("taekho1225@gmail.com","택호","010-2401-1111");
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
        List<MemberDto.Post> posts = new ArrayList<>();
        posts.add(new MemberDto.Post("taekho1@gmail.com",
                "택",
                "010-2401-5110"));
        posts.add(new MemberDto.Post("taekho2@gmail.com",
                "호",
                "010-2401-5111"));
        posts.stream().forEach(
                post -> {
                    String content = gson.toJson(post);
                    try {
                        ResultActions postActions =
                                mockMvc.perform(
                                        post("/v11/members")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(content)
                                );
                    } catch (Exception e) {
                        throw new BusinessLogicException(ExceptionCode.MEMBER_EXISTS);
                    }

                }
        );

        // when
        MvcResult getActions = mockMvc.perform(
                get("/v11/members?page=1&size=2")
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn();
        String jsonResult = getActions.getResponse().getContentAsString();
        JsonObject jObj = gson.fromJson(jsonResult, JsonObject.class);

        JsonArray datas = jObj.getAsJsonArray("data");

        // then
        int count = 0;
        for(int i = 0; i < datas.size(); i++){
            JsonObject jobj = datas.get(datas.size()-i-1).getAsJsonObject();
            if(
                    posts.get(i).getEmail().equals(jobj.get("email").getAsString())
                            &&
                            posts.get(i).getName().equals(jobj.get("name").getAsString())
                            &&
                            posts.get(i).getPhone().equals(jobj.get("phone").getAsString())){
                count++;
            }
        }
        assertEquals(datas.size(), count);



//        getActions.andExpect(status().isOk())
//                // 멤버 개수 확인
//                .andExpect(jsonPath("$.data.size()").value(2))
//                // 1번 멤버 확인
//                .andExpect(jsonPath("$.data[1].email").value(post1.getEmail()))
//                .andExpect(jsonPath("$.data[1].name").value(post1.getName()))
//                .andExpect(jsonPath("$.data[1].phone").value(post1.getPhone()))
//                // 2번 멤버 확인
//                .andExpect(jsonPath("$.data[0].email").value(post2.getEmail()))
//                .andExpect(jsonPath("$.data[0].name").value(post2.getName()))
//                .andExpect(jsonPath("$.data[0].phone").value(post2.getPhone()));
    }

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // when
        // 삭제를 위해 멤버 등록해야함
        MemberDto.Post post = new MemberDto.Post("taekho1@gmail.com",
                "택",
                "010-2401-5110");
        String content = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );
        postActions.andExpect(status().isCreated());
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"

        ResultActions deleteActions =
                mockMvc.perform(
                        delete(location)
                                .accept(MediaType.APPLICATION_JSON)
                );
        deleteActions.andExpect(status().isNoContent());

        mockMvc.perform(
                        get(location)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());


    }
}
