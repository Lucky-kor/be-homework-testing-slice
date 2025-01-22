package com.springboot.homework;

import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
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

import static org.hamcrest.MatcherAssert.assertThat;
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
        MemberDto.Post postDto = new MemberDto.Post("ming@nambet.com", "마", "010-4444-3333");
        String content = gson.toJson(postDto);

        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON) //요청 타입
                                .contentType(MediaType.APPLICATION_JSON) // 타입만 지정
                                .content(content) //body 요청
                );

        //when

       String location = actions.andReturn().getResponse().getHeader("Location");
       MemberDto.Patch patchMemberDto = MemberDto.Patch.builder()
                .name("점심")
                .phone("010-0000-3333")
                .build();

       String patchContent = gson.toJson(patchMemberDto);

       ResultActions patchActions = mockMvc.perform(
                patch(location)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON) // 타입만 지정
                        .content(patchContent) //body 요청
       );

        //then
       patchActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(patchMemberDto.getName()))
                .andExpect(jsonPath("$.data.phone").value(patchMemberDto.getPhone()));


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
       List<MemberDto.Post> members = new ArrayList<>();
       members.add(new MemberDto.Post("em@emboal.co.kr", "백군", "010-3435-4783"));
       members.add(new MemberDto.Post("hu@emboal.co.kr", "피구왕", "010-3489-7773"));
       members.add(new MemberDto.Post("sa@emboal.co.kr", "청군", "010-3065-9983"));
       members.add(new MemberDto.Post("rrkkdl@emboal.co.kr", "다이겨", "010-1235-5583"));

        members.stream().forEach(  //반환을 못해
               post -> {
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

//       String postContent = gson.toJson(members);
//       ResultActions postActions =
//               mockMvc.perform(
//                       post("/v11/members")
//                               .contentType(MediaType.APPLICATION_JSON)
//                               .accept(MediaType.APPLICATION_JSON)
//                               .content(postContent)
//               );
//        String location = postActions.andReturn().getResponse().getHeader("Location");

        //when, then
        mockMvc.perform(
                get("/v11/members?page=1&size=4")
                        .accept(MediaType.APPLICATION_JSON) //응답 받는 타입 설정
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[3].email").value(members.get(0).getEmail()))
                .andExpect(jsonPath("$.data[3].name").value(members.get(0).getName()))
                .andExpect(jsonPath("$.data[3].phone").value(members.get(0).getPhone()))
                .andExpect(jsonPath("$.data[2].email").value(members.get(1).getEmail()))
                .andExpect(jsonPath("$.data[2].name").value(members.get(1).getName()))
                .andExpect(jsonPath("$.data[2].phone").value(members.get(1).getPhone()))
                .andExpect(jsonPath("$.data[1].email").value(members.get(2).getEmail()))
                .andExpect(jsonPath("$.data[1].name").value(members.get(2).getName()))
                .andExpect(jsonPath("$.data[1].phone").value(members.get(2).getPhone()))
                .andExpect(jsonPath("$.data[0].email").value(members.get(3).getEmail()))
                .andExpect(jsonPath("$.data[0].name").value(members.get(3).getName()))
                .andExpect(jsonPath("$.data[0].phone").value(members.get(3).getPhone()));

}

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        //given
        MemberDto.Post post = new MemberDto.Post("wdof@navfd.com", "영타", "010-2038-3321");
        String postContent = gson.toJson(post);
        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );
        //when, then
        String location = postActions.andReturn().getResponse().getHeader("Location");
       mockMvc.perform(
               delete(location)
                       .accept(MediaType.APPLICATION_JSON)
       )
               .andExpect(status().isNoContent());


    }
}
