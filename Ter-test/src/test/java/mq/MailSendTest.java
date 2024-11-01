package mq;

import com.cabin.ter.TerApplication;
import com.cabin.ter.adapter.MQMessageBuilderAdapter;
import com.cabin.ter.constants.dto.EmailMessageDTO;
import com.cabin.ter.constants.enums.EmailTypeEnum;
import com.cabin.ter.constants.enums.MessagePushMethodEnum;
import com.cabin.ter.constants.enums.SourceEnum;
import com.cabin.ter.strategy.MessageStrategyFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

@SpringBootTest(classes = TerApplication.class)
@RunWith(SpringRunner.class)
public class MailSendTest {
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private ApplicationContext context;
    @Test
    public void sendMail() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(context);
        templateResolver.setCacheable(false);
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");

        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("project", "Spring Boot Demo");
        context.setVariable("author", "Yangkai.Shen");
        context.setVariable("url", "https://github.com/xkcoding/spring-boot-demo");

        String emailTemplate = templateEngine.process("test", context);
        EmailMessageDTO message = MQMessageBuilderAdapter.buildEmailMessageDTO("测试", "zouye0113@gmail.com", emailTemplate, EmailTypeEnum.SYSTEM_VERIFICATION_CODE ,SourceEnum.TEST_SOURCE);

        MessageStrategyFactory.getInstance().getAwardResult(message, MessagePushMethodEnum.EMAIL_MESSAGE);
    }


}
