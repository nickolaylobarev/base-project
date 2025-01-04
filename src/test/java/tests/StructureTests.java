package tests;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.Location;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Class enforces architectural rules using ArchUnit.
 * The tests are performed on Java classes located in specific test directories, excluding certain predefined classes
 */

public class StructureTests {

    private static final List<Location> LOCATIONS = List.of(
            Location.of(Paths.get("api/build/classes/java/test").toUri()),
            Location.of(Paths.get("ui/build/classes/java/test").toUri())
    );

    private static final Set<String> EXCLUDED_CLASSES = Set.of(
            "tests.performance.DraftPerformanceTests"
    );

    private final JavaClasses importedClasses = new ClassFileImporter()
            .importLocations(LOCATIONS);

    private final JavaClasses filteredClasses = importedClasses.that(
            new DescribedPredicate<>("not excluded") {
                @Override
                public boolean test(com.tngtech.archunit.core.domain.JavaClass javaClass) {
                    return !EXCLUDED_CLASSES.contains(javaClass.getName());
                }
            });

    @Test
    void testMethodsShouldHaveRequiredAnnotations() {

        methods()
                .that().areAnnotatedWith(org.junit.jupiter.api.Test.class)
                .or().areAnnotatedWith(org.junit.jupiter.params.ParameterizedTest.class)
                .should().beAnnotatedWith(org.junit.jupiter.api.DisplayName.class)
                .andShould().beAnnotatedWith(io.qameta.allure.Description.class)
                .check(filteredClasses);
    }

    @Test
    void testMethodsShouldReturnVoid() {

        methods()
                .that().areAnnotatedWith(org.junit.jupiter.api.Test.class)
                .or().areAnnotatedWith(org.junit.jupiter.params.ParameterizedTest.class)
                .should().haveRawReturnType(void.class)
                .check(filteredClasses);
    }
}
