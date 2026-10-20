package uga.menik.csx370;

import junit.framework.TestCase;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import uga.menik.csx370.controllers.CollectionController;
import uga.menik.csx370.services.PostService;

/** Checks real Spring wiring and transaction proxy creation without opening a port/database. */
public class ApplicationContextTest extends TestCase {
    public void testSpringWiringAndTransactionAdvice() {
        try (var context = new SpringApplicationBuilder(App.class).web(WebApplicationType.NONE)
                .properties("spring.sql.init.mode=never", "spring.main.banner-mode=off", "logging.level.root=ERROR").run()) {
            assertNotNull(context.getBean(CollectionController.class));
            assertTrue("Post writes must use the transaction proxy", AopUtils.isAopProxy(context.getBean(PostService.class)));
        }
    }
}
