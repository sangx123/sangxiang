package com.example;
import java.text.SimpleDateFormat;
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
        SimpleDateFormat df ;
        df = new SimpleDateFormat("hh:mm a");
//                if(mIsTimeFormat12){
//                    df = new SimpleDateFormat("hh:mm a, dd MMM ,EEEE");
//                }else{
//                    df = new SimpleDateFormat("HH:mm,  dd MMM ,EEEE");
//                }

        //final String date=CommonUtil.getTimeStampFromTimeZone(new Date(),deviceTimezone,df);
        final String date=df.format(new Date());
        System.out.print(date);
    }


}
