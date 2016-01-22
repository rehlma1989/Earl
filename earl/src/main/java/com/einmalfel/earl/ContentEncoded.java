package com.einmalfel.earl;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentEncoded {
  public static final String XML_TAG = "encoded";

  public static ContentEncoded parseTag(@NonNull XmlPullParser parser) throws IOException, XmlPullParserException {
    String content = parser.nextText();
    return new ContentEncoded(content, getPlainText(content), parseImage(content));
  }

  private static URL parseImage(String encoded) {
    String imgRegex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";

    Pattern p = Pattern.compile(imgRegex);
    Matcher m = p.matcher(encoded == null ? "" : encoded);
    if (m.find()) {
      return Utils.tryParseUrl(m.group(1));
    }
    return null;
  }

  private static String getPlainText(String encoded) {
    try {
      String plainTextBody = encoded.replaceAll("<[^<>]+>([^<>]*)<[^<>]+>", "$1");
      plainTextBody = plainTextBody.replaceAll("<br ?/>", "");
      return plainTextBody;
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Nullable
  public final String text;
  @Nullable
  public final URL image;
  @NonNull
  public final String encoded;

  public ContentEncoded(@NonNull String encoded, @Nullable String text, @Nullable URL image) {
    this.image = image;
    this.text = text;
    this.encoded = encoded;
  }
}
