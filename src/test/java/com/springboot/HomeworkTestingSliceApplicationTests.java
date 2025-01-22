package com.springboot;

import com.google.gson.Gson;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.springboot.member.entity.Member.MemberStatus.MEMBER_ACTIVE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HomeworkTestingSliceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Gson gson;

	@Test
	void patchMemberTest() throws Exception {
		// given
		MemberDto.Post postMember = new MemberDto.Post("asd@gmail.com",
				"ㅁㄴㅇ", "010-1111-2222");

		String content = gson.toJson(postMember);

		// when

		ResultActions postActions = mockMvc.perform(
				post("/v11/members")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(content)
		);

		String location = postActions.andReturn().getResponse().getHeader("Location");

		MemberDto.Patch patchMemberDto = new MemberDto.Patch(1, "권택현",
				"010-6782-8932", MEMBER_ACTIVE);

		URI patchUri = UriComponentsBuilder.newInstance().path(location).build().toUri();

		ResultActions patchActions = mockMvc.perform(
				patch(patchUri)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(gson.toJson(patchMemberDto))
		);

		// then
		patchActions
				.andExpect(status().isOk()) // HTTP 상태 200 확인
				.andExpect(jsonPath("$.data.name").value(patchMemberDto.getName())) // 수정된 값 확인
				.andExpect(jsonPath("$.data.phone").value(patchMemberDto.getPhone()));
	}

	@Test
	void getMembersTest() throws Exception {
		// given
		List<MemberDto.Post> postMembers = new ArrayList<>(List.of(
				new MemberDto.Post("asd@gmail.com", "권택현", "010-1111-2222"),
				new MemberDto.Post("tjsk1999@gmail.com", "ㄱㅌㅎ", "010-6782-8932")
		));

		for (MemberDto.Post post : postMembers) {
			mockMvc.perform(
					post("/v11/members")
							.accept(MediaType.APPLICATION_JSON)
							.contentType(MediaType.APPLICATION_JSON)
							.content(gson.toJson(post)));
		}


		ResultActions getActions = mockMvc.perform(
				get("/v11/members?page=1&size=10")
						.accept(MediaType.APPLICATION_JSON)
		);
		// when

		// then
		getActions
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].name").value(postMembers.get(1).getName()))
				.andExpect(jsonPath("$.data[0].email").value(postMembers.get(1).getEmail()))
				.andExpect(jsonPath("$.data[0].phone").value(postMembers.get(1).getPhone()))
				.andExpect(jsonPath("$.data[1].name").value(postMembers.get(0).getName()))
				.andExpect(jsonPath("$.data[1].email").value(postMembers.get(0).getEmail()))
				.andExpect(jsonPath("$.data[1].phone").value(postMembers.get(0).getPhone()));
	}

	@Test
	void deleteMemberTest() throws Exception {
		// given
		MemberDto.Post postMember = new MemberDto.Post("asd@gmail.com",
				"ㅁㄴㅇ", "010-1111-2222");

		String content = gson.toJson(postMember);

		// when

		ResultActions postActions = mockMvc.perform(
				post("/v11/members")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(content)
		);

		String location = postActions.andReturn().getResponse().getHeader("Location");

		ResultActions deleteActions = mockMvc.perform(
				delete(location)
						.accept(MediaType.APPLICATION_JSON)
		);

		deleteActions
				.andExpect(status().isNoContent());

		mockMvc.perform(
				get(location)
						.accept(MediaType.APPLICATION_JSON)
		).andExpect(status().isNotFound());
	}

}
