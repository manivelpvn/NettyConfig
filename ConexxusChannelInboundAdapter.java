package com.fdc.ucom.cs.conexxus.socket.endpoint;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fdc.ucom.cs.common.cache.ConnectorSwitchCacheService;
import com.fdc.ucom.cs.common.exception.ConnectorSwitchException;
import com.fdc.ucom.cs.common.util.ResponseState;
import com.fdc.ucom.cs.conexxus.socket.service.IConexxusPosInboundGateway;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConexxusChannelInboundAdapter extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConexxusChannelInboundAdapter.class);

    private IConexxusPosInboundGateway gateway;
    
    private ConnectorSwitchCacheService cacheService;

    public ConexxusChannelInboundAdapter(IConexxusPosInboundGateway gateway, ConnectorSwitchCacheService cacheService) {
        this.gateway = gateway;
        this.cacheService = cacheService;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final Channel channel = ctx.channel();
        final String remoteHostName = channel.remoteAddress().toString();
        final String request = (String) msg;
        try {
            if (StringUtils.isEmpty(request)) {
                throw new ConnectorSwitchException(ResponseState.generate(ResponseState.NameType.INVALID_MESSAGE_FORMAT), "invalid data");
            }

            if (LOGGER.isDebugEnabled()) {
            	LOGGER.debug("### Received message : {}", request.replaceAll("\\n", ""));
            }
            this.gateway.processPosRequest(request, channel);
        } catch (final ConnectorSwitchException ex) {
            LOGGER.error("Error Conexxus netty handler while Procesing Request [" + remoteHostName + "]: " + request, ex);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        LOGGER.error("Processing error while reading from conexxus socket: " + ctx, cause);
        cacheService.removeFromLocalCache(ctx.channel(), "ChannelId Unregistered: " + ctx.channel().id());
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        LOGGER.debug("Channel connected");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
    	LOGGER.info("Channel id: {} Unregistered", ctx.channel().id());
    	super.channelUnregistered(ctx);
    }
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception { 
    	LOGGER.error("Channel becomes InActive : {}",ctx.channel().id());
    	super.channelInactive(ctx);
    }
    
}
