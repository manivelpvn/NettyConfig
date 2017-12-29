package com.fdc.ucom.cs.conexxus.socket.endpoint;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * We do not load this context in our tests, as this will lead to address already in use socket exception.
 * And we do not use the sockets anyhow
 *
 * @author i017jpa
 */
@Component
@Profile("!test")
public class EndpointServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointServer.class);

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    
    private static final int LOCAL_CACHE_INITIAL_DELAY = 1 * 15 * 1000;
    
    private static boolean serverStarted = false;
    
    private ChannelFuture cf;

    @Autowired
    @Qualifier("endpointBootstrap")
    private ServerBootstrap endpointBootstrap;

    @Scheduled(initialDelay = LOCAL_CACHE_INITIAL_DELAY, fixedDelay = Long.MAX_VALUE)
    public void start() throws InterruptedException {
    	if(!serverStarted) {
    		this.endpointBootstrap.group(bossGroup, workerGroup);
            serverStarted = true;
    		ChannelFuture cf = this.endpointBootstrap.bind().sync();
    		LOGGER.info("Conexxus Netty Server boot strap started");
    		//f.channel().closeFuture().sync();
    	}
    }

    @PreDestroy
    public void stop() {
        if(cf != null) {
    		try {
    			cf.channel().closeFuture().sync();
    		} catch (InterruptedException e) {
    			LOGGER.info("", e.toString());
			}
    	}
    	LOGGER.info("Release all the External Resources");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        LOGGER.info("Conexxus Netty Server boot strap stopped");
    }
}
