package org.schabi.newpipe.extractor.services.peertube.extractors;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import com.grack.nanojson.JsonObject;

public class PeertubeStreamInfoItemExtractor implements StreamInfoItemExtractor {
    
    protected final JsonObject item;
    
    public PeertubeStreamInfoItemExtractor(JsonObject item) {
        this.item = item;
    }
    
    @Override
    public String getUrl() throws ParsingException {
        String uuid = JsonUtils.getString(item, "uuid");
        return PeertubeStreamLinkHandlerFactory.getInstance().fromId(uuid).getUrl();
    }
    
    @Override
    public String getThumbnailUrl() throws ParsingException {
        String value = JsonUtils.getString(item, "thumbnailPath");
        return ServiceList.PeerTube.getBaseUrl() + value;
    }
    
    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "name");
    }
    
    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }
    
    @Override
    public long getViewCount() throws ParsingException {
        Number value = JsonUtils.getNumber(item, "views");
        return value.longValue();
    }
    
    @Override
    public String getUploaderUrl() throws ParsingException {
        String name = JsonUtils.getString(item, "account.name");
        String host = JsonUtils.getString(item, "account.host");
        return PeertubeChannelLinkHandlerFactory.getInstance().fromId(name + "@" + host).getUrl();
    }
    
    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(item, "account.displayName");
    }
    
    @Override
    public String getUploadDate() throws ParsingException {
        String date = JsonUtils.getString(item, "publishedAt");
        return PeertubeParsingHelper.toDateString(date);
    }
    
    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }
    
    @Override
    public long getDuration() throws ParsingException {
        Number value = JsonUtils.getNumber(item, "duration");
        return value.longValue();
    }
}