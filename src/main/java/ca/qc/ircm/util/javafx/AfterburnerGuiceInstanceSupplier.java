package ca.qc.ircm.util.javafx;

import com.google.inject.Injector;

import java.util.function.Function;

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
  public Object apply(Class clazz) {
    return injector.getInstance(clazz);
  }
}
