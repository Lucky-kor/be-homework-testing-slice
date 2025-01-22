package com.springboot.homework;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
//        given
//        1. 멤버를 등록하자.
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
        long memberId;
        String location = actions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
//        when
//        2 멤버를 수정하자
        MemberDto.Patch patch = new MemberDto.Patch(memberId,"바니", null, Member.MemberStatus.MEMBER_QUIT);
        String patchContent = gson.toJson(patch);

        ResultActions patchActions =
                mockMvc.perform(
                        patch("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(patchContent));
//        then
        String name = post.getName();
        String phone = post.getPhone();
        Member.MemberStatus memberStatus = Member.MemberStatus.MEMBER_ACTIVE;

//        if(patch.getName() == null) {patch.setName(name);}
//        if(patch.getPhone() == null) {patch.setPhone(phone);}
//        if(patch.getMemberStatus() == null) {patch.setMemberStatus(memberStatus);}

        patchActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(patch.getMemberId()))
                .andExpect(jsonPath("$.data.name").value(patch.getName()))
                .andExpect(jsonPath("$.data.phone").value(post.getPhone()))
                .andExpect(jsonPath("$.data.memberStatus").value(patch.getMemberStatus().getStatus()));
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
//         given
        List<MemberDto.Post> members = new ArrayList<>();
        members.add(new MemberDto.Post("sa@dvfvgb.com","qND", "010-4444-5555"));
        members.add(new MemberDto.Post("dkfi@dn.com", "마나", "010-4654-5656"));
        members.add(new MemberDto.Post("emjf@fdjo.com", "뱌", "010-5265-1522"));

        members.stream().forEach(post -> {
                    String postContent = gson.toJson(post);
                    try {
                        mockMvc.perform(
                                post("/v11/members")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .content(postContent));
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }
                }
        );

//        String getMembers = gson.toJson(members);
//        멤버 1 등록
//        MemberDto.Post post1 = new MemberDto.Post("hgd@gmail.com",
//                "홍길동",
//                "010-1234-5678");
//        String postContent1 = gson.toJson(post1);
//
//        ResultActions postActions1 =
//                mockMvc.perform(
//                        post("/v11/members")
//                                .accept(MediaType.APPLICATION_JSON)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(postContent1)
//                );
//        long memberId1;
//        String location1 = postActions1.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
//        memberId1 = Long.parseLong(location1.substring(location1.lastIndexOf("/") + 1));
//
////        멤버 2 등록
//        MemberDto.Post post2 = new MemberDto.Post("gildong@gmail.com",
//                "길동",
//                "010-1234-5679");
//        String postContent2 = gson.toJson(post2);
//
//        ResultActions postActions2 =
//                mockMvc.perform(
//                        post("/v11/members")
//                                .accept(MediaType.APPLICATION_JSON)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(postContent2)
//                );
//        long memberId2;
//        String location2 = postActions1.andReturn().getResponse().getHeader("Location"); // "/v11/members/2"
//        memberId2 = Long.parseLong(location2.substring(location2.lastIndexOf("/") + 1));
//
////        member List로 만들기
//        List<MemberDto.Post> memberDtos = List.of(post1, post2);
//        for(MemberDto.Post member : members) {
//            gson.toJson(member);
//            return;
//        }

//        // when / then
//        ResultActions postActions =
//                mockMvc.perform(
//                        post("/v11/members")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .accept(MediaType.APPLICATION_JSON)
//                                .content(members.stream()
//                                        .map(e-> gson.toJson(e).toString()))
//                                .andExpect(status().isOk());

//        when/then

        mockMvc.perform(
                        get("/v11/members")
                                .param("page", "1")
                                .param("size", "3")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[2].email").value(members.get(0).getEmail()))
                .andExpect(jsonPath("$.data[2].name").value(members.get(0).getName()))
                .andExpect(jsonPath("$.data[2].phone").value(members.get(0).getPhone()))
                .andExpect(jsonPath("$.data[1].email").value(members.get(1).getEmail()))
                .andExpect(jsonPath("$.data[1].name").value(members.get(1).getName()))
                .andExpect(jsonPath("$.data[1].phone").value(members.get(1).getPhone()))
                .andExpect(jsonPath("$.data[0].email").value(members.get(2).getEmail()))
                .andExpect(jsonPath("$.data[0].name").value(members.get(2).getName()))
                .andExpect(jsonPath("$.data[0].phone").value(members.get(2).getPhone()));
    }

    @Test
    void deleteMemberTest() throws Exception {
        //         given
//        멤버 등록
        MemberDto.Post post = new MemberDto.Post("bana@rabbit.com","바나","010-1234-5678");
        String content = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );
//        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
//        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
//        when
        mockMvc.perform(
                        delete(location)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }
    }
