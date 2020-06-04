package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.codec.handler.OrderServerProcessHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutionException;

/**
 * @author : chenglong.ma@shuyun.com
 * @date : 2020/4/6
 */
public class Server {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        //服务启动组件
        ServerBootstrap bootstrap = new ServerBootstrap();
        //channel服务器端监听套接字通道
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.handler(new LoggingHandler(LogLevel.INFO));
        //Reactor 多线程模型
        //NioEventLoopGroup 实际上就是个线程池，一个 EventLoopGroup 包含一个或者多个 EventLoop
        bootstrap.group(new NioEventLoopGroup());
        //ChannelPipeline 提供了 ChannelHandler 责任链的容器
        //解编码处理
        bootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocolDecoder());
                pipeline.addLast(new OrderProtocolEncoder());
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                //在链上加入自定义的Handler
                pipeline.addLast(new OrderServerProcessHandler());
            }
        });

        ChannelFuture channelFuture = bootstrap.bind(8090).sync();
        channelFuture.channel().closeFuture().get();
    }
}
