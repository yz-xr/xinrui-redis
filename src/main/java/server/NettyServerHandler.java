package server;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.redis.ArrayRedisMessage;
import io.netty.handler.codec.redis.FullBulkStringRedisMessage;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

public class NettyServerHandler extends ChannelDuplexHandler {

    private Map<Object, Object> stringMap = new HashMap<Object, Object>();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // super.exceptionCaught(ctx, cause);
        ctx.close();
        System.out.println("encounter error: " + cause.getMessage());
    }

    /*
     * 收到消息时，返回信息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 收到消息直接打印输出
        System.out.println("服务端接受的消息 : " + msg);
        ArrayRedisMessage message = (ArrayRedisMessage) msg;
        //Object response = printAggregatedRedisResponseRequest(message);
        Object response = invokeAkka(message);
        System.out.println("服务器返回结果: " + response.toString());
        FullBulkStringRedisMessage fullBulkStringRedisMessage =
                new FullBulkStringRedisMessage(ByteBufUtil.writeUtf8(ctx.alloc(), (String) response));
        ctx.writeAndFlush(fullBulkStringRedisMessage);
    }

    /**
     * 将消息传递给 Akka, 由 Akka 返回结果
     */
    private Object invokeAkka(ArrayRedisMessage message) {
        String action = ((FullBulkStringRedisMessage) message.children().get(0)).content().toString(CharsetUtil.UTF_8);
        System.out.println("action: " + action);
        Object reposed = "OK";
        if (!"COMMAND".equals(action)) {
            reposed = "NOT COMMAND";
        }
        return reposed;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    /*
     * 建立连接时，返回消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接的客户端地址:" + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    /**
     * 简易存储实现, 基于map实现
     * 关于 action 类型:
     * 1. 启动和关闭: COMMAND
     * 2. 普通操作: 命令动作
     */
    private Object printAggregatedRedisResponseRequest(ArrayRedisMessage message) {
        String action = ((FullBulkStringRedisMessage) message.children().get(0)).content().toString(CharsetUtil.UTF_8);
        System.out.println("action: " + action);
        if (action.equalsIgnoreCase("set")) {
            stringMap.put(((FullBulkStringRedisMessage) message.children().get(1)).content().toString(CharsetUtil.UTF_8),
                    ((FullBulkStringRedisMessage) message.children().get(2)).content().toString(CharsetUtil.UTF_8));
            return "ok";
        }
        if (action.equalsIgnoreCase("del")) {
            stringMap.remove(((FullBulkStringRedisMessage) message.children().get(1)).content().toString(CharsetUtil.UTF_8));
            return "ok";
        }
        if (action.equalsIgnoreCase("get")) {
            Object o = stringMap.get(((FullBulkStringRedisMessage) message.children().get(1)).content().toString(CharsetUtil.UTF_8));
            if (o == null) {
                return "(null)";
            } else {
                return o;
            }
        }
        return "(null)";
    }
}
