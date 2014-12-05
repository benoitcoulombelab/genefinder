package ca.qc.ircm.util.javafx;

import java.util.function.Function;

import com.google.inject.Injector;

/**
 * Instance supplier for afterburner.fx.
 */
@SuppressWarnings("rawtypes")
public class AfterburnerGuiceInstanceSupplier implements Function<Class, Object> {
    private Injector injector;

    public AfterburnerGuiceInstanceSupplier(Injector injector) {
        this.injector = injector;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object apply(Class t) {
        return injector.getInstance(t);
    }
}
