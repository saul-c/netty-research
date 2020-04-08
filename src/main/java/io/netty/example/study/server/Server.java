package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.codec.handler.MetricHandler;
import io.netty.example.study.server.codec.handler.OrderServerProcessHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutionException;

/**
 * @author : chenglong.ma@shuyun.com
 * @date : 2020/4/6
 */
public class Server {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.channel(NioServerSocketChannel.class);
        NioEventLoopGroup boss = new NioEventLoopGroup(0, new DefaultThreadFactory("boss"));
        NioEventLoopGroup worker = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));

        bootstrap.group(boss, worker);
        //可以发送小报文，不要等
        bootstrap.childOption(NioChannelOption.TCP_NODELAY, true);
        //等待连接数，默认128
        bootstrap.option(NioChannelOption.SO_BACKLOG, 1024);


        MetricHandler metricHandler = new MetricHandler();
        bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("LoggingHandler", new LoggingHandler(LogLevel.ERROR));
                pipeline.addLast("OrderFrameDecoder", new OrderFrameDecoder());
                pipeline.addLast("OrderFrameEncoder", new OrderFrameEncoder());
                pipeline.addLast("OrderProtocolDecoder", new OrderProtocolDecoder());
                pipeline.addLast("OrderProtocolEncoder", new OrderProtocolEncoder());

                pipeline.addLast("MetricHandler", metricHandler);

                pipeline.addLast("OrderServerProcessHandler", new OrderServerProcessHandler());
            }
        });

        ChannelFuture channelFuture = bootstrap.bind(8090).sync();
        channelFuture.channel().closeFuture().get();
    }
}
