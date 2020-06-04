package io.netty.example.study.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.client.codec.*;
import io.netty.example.study.client.dispatch.OperationResultFuture;
import io.netty.example.study.client.dispatch.RequestPendingCenter;
import io.netty.example.study.client.dispatch.ResponseDispatcherHandler;
import io.netty.example.study.common.Operation;
import io.netty.example.study.common.OperationResult;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.order.OrderOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutionException;

/**
 * @author : chenglong.ma@shuyun.com
 * @date : 2020/4/6
 */
public class ClientV2 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {

        //客户端启动组件
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup());

        RequestPendingCenter requestPendingCenter = new RequestPendingCenter();

        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocolDecoder());
                pipeline.addLast(new OrderProtocolEncoder());

                pipeline.addLast(new ResponseDispatcherHandler(requestPendingCenter));

                pipeline.addLast(new OperationToRequestMessageEncoder());
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));

            }
        });
        //启动客户端
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090).sync();

        RequestMessage requestMessage = new RequestMessage(IdUtil.nextId(), new OrderOperation(1001, "tudou"));
        OperationResultFuture future = new OperationResultFuture();
        requestPendingCenter.add(requestMessage.getMessageHeader().getStreamId(), future);

        channelFuture.channel().writeAndFlush(requestMessage);
        OperationResult operationResult = future.get();
        System.out.println(operationResult.toString());


        channelFuture.channel().closeFuture().get();
    }
}
