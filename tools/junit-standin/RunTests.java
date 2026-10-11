import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Runs every @Test method of the given classes on a fresh instance, prints PASS/FAIL, exits with the failure count. */
public final class RunTests {

    public static void main(String[] classNames) throws Exception {
        int runs = 0;
        int failures = 0;
        for (String name : classNames) {
            Class<?> type = Class.forName(name);
            for (Method method : type.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                    continue;
                }
                Constructor<?> constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
                method.setAccessible(true);
                runs++;
                try {
                    method.invoke(constructor.newInstance());
                    System.out.println("PASS " + type.getSimpleName() + "." + method.getName());
                } catch (InvocationTargetException e) {
                    failures++;
                    System.out.println("FAIL " + type.getSimpleName() + "." + method.getName() + ": " + e.getCause());
                }
            }
        }
        System.out.println(runs + " run, " + failures + " failed");
        System.exit(failures);
    }
}
