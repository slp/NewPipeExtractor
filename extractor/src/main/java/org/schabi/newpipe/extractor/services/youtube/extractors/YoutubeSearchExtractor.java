package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.SearchQueryUrlHandler;
import org.schabi.newpipe.extractor.services.youtube.urlIdHandlers.YoutubeSearchQueryUrlHandler;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class YoutubeSearchExtractor extends SearchExtractor {

    private Document doc;

    public YoutubeSearchExtractor(StreamingService service,
                                  SearchQueryUrlHandler urlIdHandler,
                                  String contentCountry) {
        super(service, urlIdHandler, contentCountry);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String site;
        final String url = getUrl();
        final String contentCountry = getContentCountry();
        //String url = builder.build().toString();
        //if we've been passed a valid language code, append it to the URL
        if (!contentCountry.isEmpty()) {
            //assert Pattern.matches("[a-z]{2}(-([A-Z]{2}|[0-9]{1,3}))?", languageCode);
            site = downloader.download(url, contentCountry);
        } else {
            site = downloader.download(url);
        }

        doc = Jsoup.parse(site, url);
    }

    @Override
    public String getSearchSuggestion() throws ParsingException {
        final Element el = doc.select("div[class*=\"spell-correction\"]").first();
        if (el != null) {
            return el.select("a").first().text();
        } else {
            return "";
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return new InfoItemsPage<>(collectItems(doc), getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return getUrl() + "&page=" + Integer.toString( 1);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        String site = getDownloader().download(pageUrl);
        doc = Jsoup.parse(site, pageUrl);

        return new InfoItemsPage<>(collectItems(doc), getNextPageUrlFromCurrentUrl(pageUrl));
    }

    private String getNextPageUrlFromCurrentUrl(String currentUrl)
            throws MalformedURLException, UnsupportedEncodingException {
        int nextPageNr = Integer.parseInt(
                Parser.compatParseMap(
                        new URL(currentUrl)
                                .getQuery())
                        .get("page")) + 1;

        return currentUrl.replace("&page=" + Integer.toString( nextPageNr-1),
                "&page=" + Integer.toString(nextPageNr));
    }

    private InfoItemsSearchCollector collectItems(Document doc) throws NothingFoundException  {
        InfoItemsSearchCollector collector = getInfoItemSearchCollector();

        Element list = doc.select("ol[class=\"item-section\"]").first();

        for (Element item : list.children()) {
            /* First we need to determine which kind of item we are working with.
               Youtube depicts five different kinds of items on its search result page. These are
               regular videos, playlists, channels, two types of video suggestions, and a "no video
               found" item. Since we only want videos, we need to filter out all the others.
               An example for this can be seen here:
               https://www.youtube.com/results?search_query=asdf&page=1

               We already applied a filter to the url, so we don't need to care about channels and
               playlists now.
            */

            Element el;

            if ((el = item.select("div[class*=\"search-message\"]").first()) != null) {
                throw new NothingFoundException(el.text());

                // video item type
            } else if ((el = item.select("div[class*=\"yt-lockup-video\"]").first()) != null) {
                collector.commit(new YoutubeStreamInfoItemExtractor(el));
            } else if ((el = item.select("div[class*=\"yt-lockup-channel\"]").first()) != null) {
                collector.commit(new YoutubeChannelInfoItemExtractor(el));
            } else if ((el = item.select("div[class*=\"yt-lockup-playlist\"]").first()) != null &&
                    item.select(".yt-pl-icon-mix").isEmpty()) {
                collector.commit(new YoutubePlaylistInfoItemExtractor(el));
            }
        }

        return collector;
    }
}