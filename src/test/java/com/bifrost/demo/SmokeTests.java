package com.bifrost.demo;

import com.bifrost.demo.controller.*;
import com.bifrost.demo.dto.model.BifrostUser;
import com.bifrost.demo.dto.model.DataEntry;
import com.bifrost.demo.dto.model.ResumeRoastEntry;
import com.bifrost.demo.dto.request.*;
import com.bifrost.demo.dto.response.BaseResponse;
import com.bifrost.demo.dto.response.ResumeRoastResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class SmokeTests {

    @Value("${admin.token}")
    private String adminToken;
    @Autowired
    private DocuController docuController;
    @Autowired
    private EncryptionController encryptionController;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ParameterController parameterController;
    @Autowired
    private RootController rootController;
    @Autowired
    private UserController userController;

    @Test
    void contextLoads() {
        assertThat(rootController).isNotNull();
        assertThat(encryptionController).isNotNull();
    }

    /**
     * Basic sanity testing of root page
     */
    @Test
    void rootPage() {
        assertThat(rootController.root().getStatusCode().value()).isEqualTo(HttpStatusCode.NO_CONTENT);
    }

    /**
     * Basic checking for methods in EncryptionController. <p>
     * Coverage:
     * <li>/encrypt (POST - encryptData)</li>
     * <li>/decrypt (POST - decryptData)</li>
     */
    @Test
    void encryptionControllerTest() {
        String testString = "Test";

        ResponseEntity<BaseResponse> resp;

        // Encrypt
        resp = encryptionController.encryptData(
                new EncryptionRequest(
                        testString
                ),
                testString
        );
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNotNull();

        String encResult = resp.getBody().data().toString();

        // Decrypt
        resp = encryptionController.decryptData(
                new EncryptionRequest(
                        encResult
                ),
                testString
        );
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNotNull();
        assertThat(resp.getBody().data().toString()).isEqualTo(testString);
    }

    /**
     * Basic checking for methods in UserController. <p>
     * Coverage:
     * <li>/register-token (POST - registerToken)</li>
     * <li>/claim-token (POST - claimToken)</li>
     */
    @Test
    void userControllerTest() {
        String role = "LV0_USER";
        String username = adminToken;
        String email = "deeonanugrah@gmail.com";
        String dummyOTP = "123321";

        ResponseEntity<BaseResponse> resp;

        // Register
        resp = userController.registerToken(
                role,
                new TemporaryAdminTokenRequest(username, email)
        );
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNotNull();

        // Verify
        resp = userController.claimToken(
                role,
                new GetTemporaryAdminTokenRequest(username, dummyOTP)
        );
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNotNull();

        BifrostUser user = objectMapper.convertValue(resp.getBody().data(), BifrostUser.class);

        assertThat(user.username()).isEqualTo(username);
        assertThat(user.role().name()).isEqualTo(role);
        assertThat(user.token()).isNotNull();
    }

    /**
     * Basic checking for methods in ParameterController. <p>
     * Coverage:
     * <li>/parameter/{id} (GET - getParameterById)</li>
     * <li>/parameter (POST - createNewParameter)</li>
     * <li>/parameter (PUT - updateParameter)</li>
     * <li>/parameter/{id} (DELETE - deleteParameterById)</li>
     * <li>/parameter (GET - getParameterEntries)</li>
     */
    @Test
    void parameterControllerTest() {
        String key = UUID.randomUUID().toString().replace("-", "");
        String value = "{\"test\": \"123\"}";
        String newValue = "{\"test\": \"1234\"}";
        String desc = "Test!";

        ResponseEntity<BaseResponse> resp;
        String newId;

        // createNewParameter
        resp = parameterController.createNewParameter(new NewParameterRequest(
                desc,
                key,
                value
        ));
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNotNull();
        DataEntry entry = objectMapper.convertValue(resp.getBody().data(), DataEntry.class);
        assertThat(key).isEqualTo(entry.key());
        assertThat(value).isEqualTo(entry.value().toString());
        assertThat(desc).isEqualTo(entry.description());
        newId = entry.id();

        // getParameterById
        resp = parameterController.getParameterById(newId);
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNotNull();
        entry = objectMapper.convertValue(resp.getBody().data(), DataEntry.class);
        assertThat(key).isEqualTo(entry.key());
        assertThat(value).isEqualTo(entry.value().toString());
        assertThat(desc).isEqualTo(entry.description());

        // updateParameter
        resp = parameterController.updateParameter(new ParameterRequest(
                entry.id(),
                desc,
                key,
                newValue
        ));
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNull();

        // getParameterEntries
        resp = parameterController.getParameterEntries(100);
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNotNull();
        List<DataEntry> entries = objectMapper.convertValue(resp.getBody().data(), new TypeReference<>() {
        });
        boolean entryFound = false;
        for (DataEntry e : entries) {
            if (e.id().equals(newId) && e.value().equals(newValue)) {
                entryFound = true;
                break;
            }
        }
        assertThat(entryFound).isEqualTo(true);

        // deleteParameterById
        resp = parameterController.deleteParameterById(newId);
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNull();

        // getParameterById (after deletion)
        resp = parameterController.getParameterById(newId);
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.BAD_REQUEST);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNull();
    }

    @Test
    void docuControllerTest() throws InterruptedException {
        byte[] dummyPDF = null;

        try {
            dummyPDF = new ClassPathResource("templates/dummy.pdf").getInputStream().readAllBytes();
        } catch (Exception ex) {
            ;
        }

        if (dummyPDF == null) {
            assumeTrue(false, "Ensure dummy.pdf exists.");
        }

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "dummy.pdf",
                "application/pdf",
                dummyPDF
        );

        ResponseEntity<BaseResponse> resp;
        ResumeRoastResponse roastResp;

        resp = docuController.roastIt(mockFile);
        assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
        assertThat(resp.getBody().status()).isNotEmpty();
        assertThat(resp.getBody().message()).isNotEmpty();
        assertThat(resp.getBody().data()).isNotNull();
        roastResp = objectMapper.convertValue(resp.getBody().data(), ResumeRoastResponse.class);
        assertThat(roastResp.id()).isNotNull();
        assertThat(roastResp.status()).isEqualTo(ResumeRoastEntry.Status.NEW.name());
        assertThat(roastResp.result()).isNullOrEmpty();
        assertThat(roastResp.lastUpdated()).isNotNull();

        int maxRetry = 5;
        int delayBetweenRetrySeconds = 2000;

        while (maxRetry-- != 0) {
            resp = docuController.roastItStatus(roastResp.id());
            assertThat(resp.getStatusCode().value()).isEqualTo(HttpStatusCode.OK);
            assertThat(resp.getBody().status()).isNotEmpty();
            assertThat(resp.getBody().message()).isNotEmpty();
            assertThat(resp.getBody().data()).isNotNull();
            roastResp = objectMapper.convertValue(resp.getBody().data(), ResumeRoastResponse.class);

            if (!roastResp.status().equals(ResumeRoastEntry.Status.NEW.name()) &&
                    !roastResp.status().equals(ResumeRoastEntry.Status.PROCESSING.name()) &&
                    !roastResp.status().equals(ResumeRoastEntry.Status.DONE.name()) &&
                    !roastResp.status().equals(ResumeRoastEntry.Status.FAILED.name())) {
                fail("Somehow `status` field is modified.");
            }

            if (roastResp.status().equals(ResumeRoastEntry.Status.DONE.name())) {
                assertThat(roastResp.result()).isNotEmpty();
                break;
            }

            Thread.sleep(delayBetweenRetrySeconds);
        }
    }
}
