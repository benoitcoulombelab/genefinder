package ca.qc.ircm.genefinder.rest;

/**
 * Rest's configuration.
 */
public interface RestConfiguration {
  /**
   * Returns timeout for REST requests.
   *
   * @return timeout for REST requests
   */
  public long timeout();
}
