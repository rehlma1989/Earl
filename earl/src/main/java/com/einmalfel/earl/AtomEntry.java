package com.einmalfel.earl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class AtomEntry extends AtomCommonAttributes implements Item {
  static final String XML_TAG = "entry";
  private static final String TAG = "Earl.AtomEntry";

  @NonNull
  public final URI id;
  @NonNull
  public final AtomText title;
  @NonNull
  public final AtomDate updated;
  @NonNull
  public final List<AtomPerson> authors;
  @Nullable
  public final AtomContent content;
  @NonNull
  public final List<AtomLink> links;
  @Nullable
  public final AtomText summary;
  @NonNull
  public final List<AtomCategory> categories;
  @NonNull
  public final List<AtomPerson> contributors;
  @Nullable
  public final AtomDate published;
  @Nullable
  public final AtomFeed source;
  @Nullable
  public final AtomText rights;

  @NonNull
  static AtomEntry read(XmlPullParser parser)
      throws XmlPullParserException, IOException {
    parser.require(XmlPullParser.START_TAG, null, XML_TAG);

    List<AtomPerson> authors = new LinkedList<>();
    List<AtomLink> links = new LinkedList<>();
    List<AtomCategory> categories = new LinkedList<>();
    List<AtomPerson> contributors = new LinkedList<>();
    AtomText title = null;
    AtomText summary = null;
    AtomText rights = null;
    AtomContent content = null;
    AtomFeed source = null;
    String id = null;
    AtomDate updated = null;
    AtomDate published = null;

    AtomCommonAttributes atomCommonAttributes = new AtomCommonAttributes(parser);
    while (parser.nextTag() == XmlPullParser.START_TAG) {
      if (Utils.ATOM_NAMESPACE.equalsIgnoreCase(parser.getNamespace())) {
        switch (parser.getName()) {
          case AtomLink.XML_TAG:
            links.add(AtomLink.read(parser));
            break;
          case AtomCategory.XML_TAG:
            categories.add(AtomCategory.read(parser));
            break;
          case "contributor":
            contributors.add(AtomPerson.read(parser));
            break;
          case "author":
            authors.add(AtomPerson.read(parser));
            break;
          case "title":
            title = AtomText.read(parser);
            break;
          case "summary":
            summary = AtomText.read(parser);
            break;
          case "rights":
            rights = AtomText.read(parser);
            break;
          case "id":
            id = parser.nextText();
            break;
          case "published":
            published = AtomDate.read(parser);
            break;
          case "updated":
            updated = AtomDate.read(parser);
            break;
          case AtomContent.XML_TAG:
            content = AtomContent.read(parser);
            break;
          case AtomFeed.XML_TAG:
            source = AtomFeed.read(parser, 0);
            break;
          default:
            Log.w(TAG, "Unknown tag in Atom entry " + parser.getName());
            Utils.skipTag(parser);
        }
      } else {
        Log.w(TAG, "Unknown namespace in Atom item " + parser.getNamespace());
        Utils.skipTag(parser);
      }
      Utils.finishTag(parser);
    }

    if (title == null) {
      Log.w(TAG, "No title found for atom entry", new NullPointerException());
      title = new AtomText(null, null, "");
    }
    if (updated == null) {
      Log.w(TAG, "No updated found for atom entry, replaced with zero", new NullPointerException());
      updated = new AtomDate(null, new Date(0));
    }

    return new AtomEntry(
        atomCommonAttributes, Utils.nonNullUri(id), title, updated, authors, content, links,
        summary, categories, contributors, published, source, rights);
  }

  public AtomEntry(@Nullable AtomCommonAttributes atomCommonAttributes, @NonNull URI id,
                   @NonNull AtomText title, @NonNull AtomDate updated,
                   @NonNull List<AtomPerson> authors, @Nullable AtomContent content,
                   @NonNull List<AtomLink> links, @Nullable AtomText summary,
                   @NonNull List<AtomCategory> categories, @NonNull List<AtomPerson> contributors,
                   @Nullable AtomDate published, @Nullable AtomFeed source,
                   @Nullable AtomText rights) {
    super(atomCommonAttributes);
    this.id = id;
    this.title = title;
    this.updated = updated;
    this.authors = Collections.unmodifiableList(authors);
    this.content = content;
    this.links = Collections.unmodifiableList(links);
    this.summary = summary;
    this.categories = Collections.unmodifiableList(categories);
    this.contributors = Collections.unmodifiableList(contributors);
    this.published = published;
    this.source = source;
    this.rights = rights;
  }

  @Nullable
  @Override
  public String getLink() {
    if (links.isEmpty()) {
      return null;
    }
    for (AtomLink link : links)
      if (link.type != null && "alternate".equals(link.type)) {
        return link.href.toString();
      }
    for (AtomLink link : links)
      if (link.type != null && "via".equals(link.type)) {
        return link.href.toString();
      }
    for (AtomLink link : links)
      if (link.type != null && "related".equals(link.type)) {
        return link.href.toString();
      }
    for (AtomLink link : links)
      if (link.type == null) {
        return link.href.toString();
      }
    for (AtomLink link : links)
      if (link.type != null && !"enclosure".equals(link.type) && !"self".equals(link.type)) {
        return link.href.toString();
      }
    return links.get(0).href.toString();
  }

  @NonNull
  @Override
  public Date getPublicationDate() {
    return published == null ? updated.date : published.date;
  }

  @NonNull
  @Override
  public String getTitle() {
    return title.value;
  }

  @Nullable
  @Override
  public String getDescription() {
    return summary == null ? (content == null ? null : content.value) : summary.value;
  }

  @Nullable
  @Override
  public String getImageLink() {
    for (AtomLink link : links) {
      if (link.getType() != null && link.getType().equals("image/jpeg")) return link.getLink();
    }
    return null;
  }

  @Nullable
  @Override
  public String getAuthor() {
    if (authors.isEmpty()) {
      return contributors.isEmpty() ? null : contributors.get(0).name;
    } else {
      return authors.get(0).name;
    }
  }

  @NonNull
  @Override
  public List<? extends Enclosure> getEnclosures() {
    List<Enclosure> result = new LinkedList<>();
    for (AtomLink link : links) {
      if (link.type != null && "enclosure".equals(link.type)) {
        result.add(link);
      }
    }
    return result;
  }

  @NonNull
  @Override
  public String getId() {
    return id.toString();
  }
}
