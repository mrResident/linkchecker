package ru.resprojects.linkchecker.model;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.resprojects.linkchecker.LinkcheckerApplication;
import ru.resprojects.linkchecker.util.ValidationUtil;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LinkcheckerApplication.class)
@ActiveProfiles(profiles = "moc_test")
public class ValidationModelTests {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationModelTests.class);
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeClass
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterClass
    public static void close() {
        validatorFactory.close();
    }

    @Test
    public void tryCreateNodeWithBlankName() {
        Node node = new Node("");
        Set<ConstraintViolation<Node>> violations = validator.validate(node);
        Assert.assertEquals(2, violations.size());
        ConstraintViolation<Node> violation = violations.stream()
            .filter(v -> ValidationUtil.VALIDATOR_NODE_NOT_BLANK_NAME_MESSAGE.equals(v.getMessage()))
            .findFirst()
            .orElse(null);
        Assert.assertNotNull(violation);
        violation = violations.stream()
            .filter(v -> ValidationUtil.VALIDATOR_NODE_NAME_RANGE_MESSAGE.equals(v.getMessage()))
            .findFirst()
            .orElse(null);
        Assert.assertNotNull(violation);
        printViolationMessage(violations);
    }

    @Test
    public void tryCreateNodeWithTooLongNodeName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            sb.append('a');
        }
        Node node = new Node(sb.toString());
        Set<ConstraintViolation<Node>> violations = validator.validate(node);
        Assert.assertEquals(1, violations.size());
        ConstraintViolation<Node> violation = violations.stream()
            .filter(v -> ValidationUtil.VALIDATOR_NODE_NAME_RANGE_MESSAGE.equals(v.getMessage()))
            .findFirst()
            .orElse(null);
        Assert.assertNotNull(violation);
        printViolationMessage(violations);
    }

    private static void printViolationMessage(Set<ConstraintViolation<Node>> violations) {
        violations.forEach(v -> {
            LOG.info("VIOLATION INFO");
            LOG.info("--> MESSAGE: " + v.getMessage());
            LOG.info("--> PROPERTY PATH: " + v.getPropertyPath().toString());
            LOG.info("--> INVALID VALUE: " + v.getInvalidValue());
        });
    }

}
