package infinity.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Fredrik Lindgren <lindgren.fredrik@gmail.com>
 * @since  2012-08-20
 */
public class StreamDiscarder implements Runnable
{
  private InputStream in;

  public void setStream(InputStream in)
  {
    this.in = in;
  }

  @Override
  public void run()
  {
    try {
      InputStreamReader r = new InputStreamReader(in);
      BufferedReader b = new BufferedReader(r);
      while (b.readLine() != null) {}
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
