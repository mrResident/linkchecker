package ru.resprojects.linkchecker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
public class LinkcheckerApplicationTests {

	private static final Logger LOG = LoggerFactory.getLogger(LinkcheckerApplicationTests.class);

	@Test
	public void contextLoads() {
	}

	@Test
	public void stub() {
		LOG.debug("NOTHING");
		Set<String> stringSet = Stream.of("v1", "v2").collect(Collectors.toSet());
		Set<Integer> intSet = Stream.of(1, 2).collect(Collectors.toSet());
		Set<Double> doubleSet = Stream.of(1.0, 2.0).collect(Collectors.toSet());
		genericCollection(stringSet);
		genericCollection(intSet);
		genericCollection(doubleSet);
	}

	private <T> void genericCollection(final Set<T> coll) {
		if (Objects.nonNull(coll) && !coll.isEmpty()) {
			T col = coll.iterator().next();
			if (col instanceof String || col instanceof Integer) {
				LOG.debug("The collection has a valid type = " + col.getClass().getName());
			} else {
				LOG.debug("The collection do not have a valid type");
			}
		}
	}

}
