package uk.gov.hmcts.probate.services.submit.clients;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;
import uk.gov.hmcts.probate.services.submit.utils.TestUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MailMessageBuilderTest {

    @Autowired
    private SpringTemplateEngine templateEngine;

    private MailMessageBuilder mailMessageBuilder;
    private Calendar submissonTimestamp;
    private JsonNode registryData;
    
    @Before
    public void setUp() throws Exception {
        mailMessageBuilder = new MailMessageBuilder(templateEngine);
        submissonTimestamp = Calendar.getInstance();
        registryData = TestUtils.getJsonNodeFromFile("registryDataSubmit.json");
    }

    @Test
    public void testBuildMessageWithSuccess() throws IOException, MessagingException {
        Properties messageProperties = new Properties();
        messageProperties.put("subject", "subject");
        messageProperties.put("sender", "sender");
        messageProperties.put("recipient", "recipient");
        JsonNode emailData = TestUtils.getJsonNodeFromFile("formPayload.json");
        JsonNode registry = registryData.get("registry");

        MimeMessage mimeMessage = mailMessageBuilder.buildMessage(emailData, registry, messageProperties, submissonTimestamp);
        String mailContent = mimeMessage.getContent().toString();
        System.out.println(mailContent);
        assertThat(mimeMessage.getSubject(), is("subject"));
        assertThat(mimeMessage.getFrom(), arrayContaining(new InternetAddress("sender")));
        assertThat(mimeMessage.getRecipients(Message.RecipientType.TO), arrayContaining(new InternetAddress("oxford@email.com")));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantFirstName").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantLastName").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantSameWillName").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantAlias").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantAliasReason").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantOtherReason").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantAddress").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantPostcode").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantPhone").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/email").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedFirstname").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedSurname").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedAddress").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedPostcode").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/noOfApplicants").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedDob").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedDod").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/willLeft").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/noOfExecutors").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/executorsNotApplying").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/ihtForm").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/ihtGrossValue").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/ihtNetValue").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/declaration").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/legalStatement").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedAliasAssets").asText()));
        emailData.at("/submitdata/deceasedOtherNames").elements().forEachRemaining(otherName -> {
            assertThat(mailContent, containsString(otherName.at("/firstName").asText()));
            assertThat(mailContent, containsString(otherName.at("/lastName").asText()));
        });
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedMarriedAfterDateOnWill").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/ihtIdentifier").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/applicantIsExecutor").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/deceasedDomicile").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/willOriginal").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/willWithCodicils").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/willOriginal").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/ihtCompleted").asText()));
        assertThat(mailContent, containsString(emailData.at("/submitdata/noOfExecutors").asText()));
        emailData.at("/submitdata/executorsNotApplying").elements().forEachRemaining(notApplying -> {
            assertThat(mailContent, containsString(notApplying.at("/fullName").asText()));
            assertThat(mailContent, containsString(notApplying.at("/optionNotApplyingKey").asText()));
        });
        emailData.at("/submitdata/executorsApplying").elements().forEachRemaining(Applying -> {
            assertThat(mailContent, containsString(Applying.at("/fullName").asText()));
            assertThat(mailContent, containsString(Applying.at("/email").asText()));
            assertThat(mailContent, containsString(Applying.at("/mobile").asText()));
            assertThat(mailContent, containsString(Applying.at("/currentName").asText()));
        });
        assertThat(mailContent, containsString(emailData.at("/submitdata/executorsApplying").asText()));
    }
}