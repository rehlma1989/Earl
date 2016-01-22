package com.einmalfel.earl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class RSSContent {
  static final String XML_TAG = "content:encoded";

  @NonNull
  public final String description;
  @Nullable
  public final String image;

  @NonNull
  static RSSContent read(@NonNull XmlPullParser parser)
      throws IOException, XmlPullParserException {

    int token = parser.nextToken();
    while(token!=XmlPullParser.CDSECT){
      token = parser.nextToken();
    }
    String cdata = parser.getText();
    Log.i("Info", cdata);
    String result = cdata.substring(cdata.indexOf("src='")+5, cdata.indexOf("jpg")+3);
    Log.i("Info", result);

    parser.require(XmlPullParser.START_TAG, XmlPullParser.NO_NAMESPACE, XML_TAG);
    return new RSSContent(Utils.nonNullString(parser.nextText()),
                           parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "domain"));
  }

  public RSSContent(@NonNull String description, @Nullable String image) {
    this.description = description;
    this.image = image;
  }
}
