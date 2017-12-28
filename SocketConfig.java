package com.fdc.ucom.cs.conexxus.socket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.fdc.ucom.cs.common.cache.ConnectorSwitchCacheService;
import com.fdc.ucom.cs.conexxus.socket.endpoint.ConexxusChannelInboundAdapter;
import com.fdc.ucom.cs.conexxus.socket.service.IConexxusPosInboundGateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

@Configuration
@Profile("!test")
public class SocketConfig {

	@Value("${com.fdc.ucom.cs.socket.conexxusPort}")
	private int conexxusPort;

	@Autowired
	private IConexxusPosInboundGateway gateway;

	@Autowired
	private ConnectorSwitchCacheService cacheService;

	@Bean(name = "cnxEndpointBootstrap")
	public ServerBootstrap cnxNettyBootstrap() {
		final ServerBootstrap serverBootstrap = new ServerBootstrap();

		serverBootstrap.localAddress(this.conexxusPort);
		serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.childHandler(this.channelInitializer());

		return serverBootstrap;
	}
	
	@Bean
	public ChannelInitializer<NioServerSocketChannel> channelInitializer() {
		return new ChannelInitializer<NioServerSocketChannel>() {
			@Override
			public void initChannel(final NioServerSocketChannel ch) throws Exception {
				ch.pipeline().addLast("lengthFieldBasedFrameDecoder",
						new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
				ch.pipeline().addLast("stringDecoder", new StringDecoder());
				ch.pipeline().addLast("stringEncoder", new StringEncoder());
				ch.pipeline().addLast("lengthFieldPrepender", new LengthFieldPrepender(4));
				ch.pipeline().addLast("conexxusMessageHandler",
						new ConexxusChannelInboundAdapter(ConnectorSwitchConexxusSocketConfig.this.gateway,
								ConnectorSwitchConexxusSocketConfig.this.cacheService));
			}
		};
	}

}
