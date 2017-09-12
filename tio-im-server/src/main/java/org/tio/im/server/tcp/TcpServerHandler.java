/**
 * 
 */
package org.tio.im.server.tcp;

import java.nio.ByteBuffer;
import org.apache.log4j.Logger;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.exception.AioDecodeException;
import org.tio.core.intf.Packet;
import org.tio.im.common.Const;
import org.tio.im.common.Protocol;
import org.tio.im.common.tcp.TcpPacket;
import org.tio.im.common.tcp.TcpServerDecoder;
import org.tio.im.common.tcp.TcpServerEncoder;
import org.tio.im.common.tcp.TcpSessionContext;
import org.tio.im.common.utils.ImUtils;
import org.tio.im.server.command.CommandManager;
import org.tio.im.server.handler.AbServerHandler;
import org.tio.server.ServerGroupContext;
/**
 * 版本: [1.0]
 * 功能说明: 
 * 作者: WChao 创建时间: 2017年8月3日 下午7:44:48
 */
public class TcpServerHandler extends AbServerHandler{
	
	Logger logger = Logger.getLogger(TcpServerHandler.class);
	
	@Override
	public void init(ServerGroupContext serverGroupContext) {
	}

	@Override
	public boolean isProtocol(ByteBuffer buffer,ChannelContext channelContext){
		Object sessionContext = channelContext.getAttribute();
		if(sessionContext == null){
			if(buffer != null){
				//获取第一个字节协议版本号;
				byte version = buffer.get();
				if(version == Protocol.VERSION){//TCP协议;
					channelContext.setAttribute(new TcpSessionContext());
					ImUtils.setClient(channelContext);
					return true;
				}
			}
		}else if(sessionContext instanceof TcpSessionContext){
			return true;
		}
		return false;
	}

	@Override
	public ByteBuffer encode(Packet packet, GroupContext groupContext,ChannelContext channelContext) {
		TcpPacket tcpPacket = (TcpPacket)packet;
		return TcpServerEncoder.encode(tcpPacket, groupContext, channelContext);
	}

	@Override
	public void handler(Packet packet, ChannelContext channelContext)throws Exception {
		TcpPacket tcpPacket = (TcpPacket)packet;
		String message = new String(tcpPacket.getBody(),Const.CHARSET);
		String onText = new String("服务器收到来自->"+channelContext.getId()+"的消息:"+message);
		logger.info(onText);
		CommandManager.getInstance().getCommand(tcpPacket.getCommand()).handler(tcpPacket, channelContext);
	}

	@Override
	public TcpPacket decode(ByteBuffer buffer, ChannelContext channelContext)throws AioDecodeException {
		return TcpServerDecoder.decode(buffer, channelContext);
	}

	@Override
	public AbServerHandler build() {
		
		return new TcpServerHandler();
	}

	@Override
	public String name() {
		
		return Protocol.TCP;
	}
}
