package vault.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.vault.config.SecurityConfig;
import com.vault.service.CustomUserDetailsService;
import com.vault.service.FileService;


/**
 * Test suite for SecurityConfig class.
 * Tests password encoding and access control for secured endpoints.
 */
@WebMvcTest(controllers = SecurityConfig.class)
@ContextConfiguration(classes = SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomUserDetailsService userDetailsService;
    
    @MockBean
    private FileService fileService;

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests if the PasswordEncoder bean correctly encodes passwords.
     */
    @Test
    void testPasswordEncoderBean() {
        BCryptPasswordEncoder encoder = (BCryptPasswordEncoder) securityConfig.passwordEncoder();
        String rawPassword = "testPassword";
        String encodedPassword = encoder.encode(rawPassword);

     // Verify if the encoder matches the raw password to the encoded password
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    /**
     * Tests access to a protected endpoint without authentication.
     * Expects HTTP status 401 Unauthorized.
     */
    @Test
    void testUnauthorizedAccessToProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/files/1"))
               .andExpect(status().isUnauthorized())
               .andDo(print());
    }

}
