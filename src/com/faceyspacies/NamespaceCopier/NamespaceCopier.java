package com.faceyspacies.NamespaceCopier;

import java.io.IOException;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;

public class NamespaceCopier {
	
	private Wiki rsWiki;
	private Wiki tyWiki;
	private String errorLog;
	
	NamespaceCopier() {
		rsWiki = new Wiki("runescape.wikia.com", "");
		tyWiki = new Wiki("ty.wikia.com", "");
		
		rsWiki.setMarkBot(true);
		tyWiki.setMarkBot(true);
		
		rsWiki.setThrottle(0);
		tyWiki.setThrottle(0);
		
		errorLog = "== ~~~ ==\n";
	}

	public static void main(String[] args) {
		NamespaceCopier nsc = new NamespaceCopier();
		nsc.start();

	}
	
	public void start() {
		try {
			rsWiki.login("TyBot", "");
			tyWiki.login("TyBot", "");
		} catch (FailedLoginException e) {
			System.out.println("Couldn't login");
			return;
		} catch (IOException e) {
			System.out.println("Can't connect to wiki");
			return;
		}
		String[] pages = getPages();
		if(pages == null) {
			System.out.println("Unable to get pages");
			return;
		}
		
		String pageContent;
		String dataPageContent;
		for(String page: pages) {
			try {
				if(page.equals("Exchange:Test")) {
					continue;
				}
				System.out.println(page);
				
				pageContent = rsWiki.getPageText(page);
				dataPageContent = rsWiki.getPageText(page + "/Data");
				
				if(pageContent.equals("")) {
					errorLog += "* " + page + ": is empty\n";
				}
				
				if(dataPageContent.equals("")) {
					errorLog += "* " + page + "/Data: is empty\n";
				}
				
				tyWiki.edit(page, pageContent, "Copying [[runescape:" + page + "]] for testing");
				tyWiki.edit(page + "/Data", dataPageContent, "Copying [[runescape:" + page + "/Data]] for testing");
				
				pageContent = "";
				dataPageContent = "";
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			tyWiki.edit("User:TyBot/log", errorLog, "Updating error log");
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String[] getPages() {
		
		int failures = 0;
		
		while(failures < 3) {
			try {
				String[] pages = rsWiki.getCategoryMembers("Grand Exchange", 112);
				return pages;
			} catch (IOException e1) {
				failures++;
				if(failures == 3) {
					return null;
				}
			}
		}
		
		return null; // shouldn't reach here, but if we do assume we have failed. 
	}

}
