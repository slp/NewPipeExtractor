package org.schabi.newpipe.extractor.channel;

import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

import java.io.IOException;

/*
 * Created by Christian Schabesberger on 31.07.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * ChannelInfo.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ChannelInfo extends ListInfo<StreamInfoItem> {

    public ChannelInfo(int serviceId, String url, String id, String name) {
        super(serviceId, id, url, name);
    }

    public static ChannelInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static ChannelInfo getInfo(StreamingService service, String url) throws IOException, ExtractionException {
        ChannelExtractor extractor = service.getChannelExtractor(url);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    public static InfoItemsPage<StreamInfoItem> getMoreItems(StreamingService service, String url, String pageUrl) throws IOException, ExtractionException {
        return service.getChannelExtractor(url).getPage(pageUrl);
    }

    public static ChannelInfo getInfo(ChannelExtractor extractor) throws IOException, ExtractionException {

        // important data
        int serviceId = extractor.getServiceId();
        String url = extractor.getCleanUrl();
        String id = extractor.getId();
        String name = extractor.getName();

        ChannelInfo info = new ChannelInfo(serviceId, url, id, name);


        try {
            info.setAvatarUrl(extractor.getAvatarUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setBannerUrl(extractor.getBannerUrl());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setFeedUrl(extractor.getFeedUrl());
        } catch (Exception e) {
            info.addError(e);
        }

        final InfoItemsPage<StreamInfoItem> itemsPage = ExtractorHelper.getItemsPageOrLogError(info, extractor);
        info.setRelatedItems(itemsPage.getItems());
        info.setNextPageUrl(itemsPage.getNextPageUrl());

        try {
            info.setSubscriberCount(extractor.getSubscriberCount());
        } catch (Exception e) {
            info.addError(e);
        }
        try {
            info.setDescription(extractor.getDescription());
        } catch (Exception e) {
            info.addError(e);
        }

        return info;
    }

    private String avatarUrl;
    private String bannerUrl;
    private String feedUrl;
    private long subscriberCount = -1;
    private String description;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public long getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}