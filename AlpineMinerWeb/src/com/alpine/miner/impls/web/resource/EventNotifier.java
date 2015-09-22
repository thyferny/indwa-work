/**
 * COPYRIGHT   2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * GroupInfo.java
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Nov 07, 2011
 */
 
package com.alpine.miner.impls.web.resource;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.alpine.miner.impls.mail.MailInfo;
import com.alpine.miner.impls.mail.SendMailException;
import com.alpine.miner.impls.resource.ResourceInfo.ResourceType;
import com.alpine.miner.interfaces.MailSender;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.impl.ProviderFactory;
import org.apache.log4j.Logger;

/**
 * @author sam_zang
 *
 */
public class EventNotifier {
    private static Logger itsLogger = Logger.getLogger(EventNotifier.class);

    public enum EventType {
		FlowCreate,
		FlowUpdate,
		FlowDelete
	}

	static class Event {
		EventType type;
		FlowInfo info;
	}
	
	private static MailSender sender = MailSender.instance;
	private static List<Event> eventQ = Collections.synchronizedList(new LinkedList<Event>());
	
	static class WorkerThread extends Thread {

		WorkerThread() {
			super("process thread"); 
			start();
		}

		public void run() {
			while (true) {
				if (eventQ == null) {
					eventQ = Collections.synchronizedList(new LinkedList<Event>());
				}
				if (eventQ.size() > 0) {
					// System.out.println("Process " + new Date());
					Event evt = eventQ.remove(0);
					processEvent(evt.info, evt.type);
				}
				else {
					// System.out.println("Wait    " + new Date());
					synchronized (eventQ) {
						try {
							eventQ.wait(5000);
						} catch (InterruptedException e) {
							// ignore interrupt.
						}
					}					
				}
			}
		}
	}
	
	static {		 	
		new WorkerThread();
	}
	
	public static void sendEvent(FlowInfo info, EventType type) {
		Event evt = new Event();
		evt.type = type;
		evt.info = info;
		synchronized (eventQ) {
			eventQ.add(evt);
			eventQ.notify();
		}
	}
	
	private static void processEvent(FlowInfo info, EventType type) {

		switch (info.getResourceType()) {
		case Public:
			notifyPublic(info, type);
			break;
		case Group:
			notifyGroup(info, type);
			break;
		case Personal:
			break;
		}
	}

	/**
	 * @param info
	 * @param type
	 */
	private static void notifyPublic(FlowInfo info, EventType type) {
		List<UserInfo> infoList = loadUsersOfSystem();
		MailInfo mail = generateMailContent(info, type);

		for (UserInfo uinfo : infoList) {
			if (uinfo.getNotification()) {
				mail.addReceiver(uinfo.getEmail());
			}
		}

		doSend(mail);
	}
	
	/**
	 * @param info
	 * @param type
	 */
	private static void notifyGroup(FlowInfo info, EventType type) {
		List<UserInfo> infoList = loadUsersOfSystem();
		MailInfo mail = generateMailContent(info, type);
		
		for (UserInfo uinfo : infoList) {
			if (inGroup(uinfo, info.getGroupName()) &&
					uinfo.getNotification()) {
				mail.addReceiver(uinfo.getEmail());
			}
		}		
		
		doSend(mail);
	}

	private static void doSend(MailInfo mail) {
		try {
			sender.send(mail);
		} catch (SendMailException e) {
			e.printStackTrace();
			itsLogger.error(e.getMessage(),e);
		} 
	}
	
	/**
	 * @param info
	 * @param type
	 * @return
	 */
	private static MailInfo generateMailContent(FlowInfo info, EventType type) {
		MailInfo mail = new MailInfo();
		
		String action = null;
		switch (type) {
		case FlowCreate:
			action = "created";
			break;
		case FlowUpdate:
			action = "updated";
			break;
		case FlowDelete:
			action = "deleted";
			break;
		}
		
		String subject = info.getResourceType().name() + " flow: ";
		if (info.getResourceType() == ResourceType.Group) {
			subject += info.getGroupName() + "/";
		}
		subject += info.getId() + " has been " + action;
		
		String firstName = "";
		String lastName = "";
		if(info.getModifiedUser().equals("admin")){
			firstName = ProviderFactory.getAuthenticator("admin").getUserInfoByName(info.getModifiedUser()).getLastName();
			lastName =   ProviderFactory.getAuthenticator("admin").getUserInfoByName(info.getModifiedUser()).getFirstName();
		}else{
			firstName = ProviderFactory.getAuthenticator("").getUserInfoByName(info.getModifiedUser()).getLastName();
			lastName =   ProviderFactory.getAuthenticator("").getUserInfoByName(info.getModifiedUser()).getFirstName();
		}
		
		
		String content = "The flow is "
				+ action
				+ " by "
				+ lastName
				+ " "
				+ firstName
		        +"\r\n"
		        +"Comment:"+info.getComments();
		
		mail.setSubject(subject);
		mail.setContent(content);
		mail.setSendTime(new Date());
		
		return mail;
	}

	/**
	 * @param uinfo
	 * @param groupName
	 * @return
	 */
	private static boolean inGroup(UserInfo uinfo, String groupName) {
		if (uinfo == null || uinfo.getGroups() == null || uinfo.getGroups().length == 0) {
			return false;
		}
		for (String g : uinfo.getGroups()) {
			if (g != null && g.equals(groupName)) {
				return true;
			}
		}
		return false;
	}

	
	private static List<UserInfo> loadUsersOfSystem(){
		return ProviderFactory.getAuthenticator("").getUserInfoList();
	}
}
