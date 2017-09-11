package com.example;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAppender;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TestJava {

    public static void main(String[] args) throws Exception {
        int port=8086;
        bind(port);
    }

    public static void bind(int port) throws Exception{
        //配置服务器和NIO线程组
        //NioEventLoopGroup是个线程组，它包含一组NIO线程，专门用于网络事件的处理，实际上他们就是Reactor线程组。
        //这里创建2个原因是一个用于服务端接收客户端的连接，另一个用于进行SocketChannel的网络读写
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try{
            //ServerBootstrap对象是Netty用于启动NIO服务端的辅助启动类，用于降低服务端的开发复杂度
            ServerBootstrap b=new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    //设置创建的Channel为NioServerSocketChannel，它的功能对应于JDK NIO类库中的ServerSocketChannel类
                    .channel(NioServerSocketChannel.class)
                    //配置TCP参数
                    .option(ChannelOption.SO_BACKLOG,1024)
                    //绑定I/O事件的处理类，它的作用类似于Reactor模式中的handler类，主要处理网络I/O事件，例如记录日志，对消息进行编解码等
                    .childHandler(new ChildChannelHandler());
            //绑定端口，同步等待成功
            ChannelFuture f=b.bind(port).sync();
            //等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        }finally {
            //优雅的退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new TimeServerHandler());
        }
    }

    private static class TimeServerHandler extends ChannelHandlerAppender{
        //服务端返回应答消息时候调用
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //获取到服务端请求的消息
            ByteBuf buf=(ByteBuf)msg;
            byte[] req=new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body=new String(req,"UTF-8");
            System.out.println("the time server receive order:"+body);
            //对消息内容进行判断
            String currentTime="QUERY TIME ORDER".equalsIgnoreCase(body)?new Date(System.currentTimeMillis()).toString():"BAD ORDER";
            //返回消息给客户端
            ByteBuf resp= Unpooled.copiedBuffer(currentTime.getBytes());
            ctx.write(resp);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            //将消息发送队列中的消息写入到SockectChannel中发送给对方，从性能角度考虑，为了防止频繁地唤醒Selector进行消息发送
            //Netty的write方法并不直接将消息写入SocketChannel中，调用write方法只是把待发送的消息放到发送缓冲数组中，在通过调用flush方法，将发送缓冲区中的消息全部写到SocketChannel中
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
