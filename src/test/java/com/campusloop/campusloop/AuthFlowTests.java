package com.campusloop.campusloop;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.campusloop.campusloop.entity.User;
import com.campusloop.campusloop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:campusloop-auth-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class AuthFlowTests {

    private static final String EMAIL = "campusloop.auth.test@college.edu";
    private static final String PASSWORD = "CampusLoop#2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrationAuthenticationAndLogoutWorkEndToEnd() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(post("/register")
                        .param("fullName", "Taylor Student")
                        .param("email", EMAIL)
                        .param("password", PASSWORD)
                        .param("confirmPassword", "DifferentPassword#2026")
                        .param("collegeName", "Campus University")
                        .param("department", "Computer Science"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(containsString("Passwords do not match.")))
                .andExpect(content().string(not(containsString(PASSWORD))));

        mockMvc.perform(post("/register")
                        .param("fullName", "Taylor Student")
                        .param("email", EMAIL)
                        .param("password", PASSWORD)
                        .param("confirmPassword", PASSWORD)
                        .param("collegeName", "Campus University")
                        .param("department", "Computer Science"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User user = userRepository.findByEmailIgnoreCase(EMAIL).orElseThrow();
        assertNotEquals(PASSWORD, user.getPasswordHash());
        assertTrue(passwordEncoder.matches(PASSWORD, user.getPasswordHash()));

        mockMvc.perform(post("/register")
                        .param("fullName", "Another Student")
                        .param("email", EMAIL)
                        .param("password", PASSWORD)
                        .param("confirmPassword", PASSWORD)
                        .param("collegeName", "Campus University")
                        .param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("An account with this email already exists.")));

        mockMvc.perform(post("/login")
                        .param("email", EMAIL)
                        .param("password", "incorrect-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("Email or password is incorrect.")))
                .andExpect(content().string(not(containsString("incorrect-password"))));

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("email", EMAIL.toUpperCase())
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Taylor Student")));

        mockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
