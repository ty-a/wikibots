package com.faceyspacies.fixtypobot;

import java.util.ArrayList;
//import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import jwiki.core.NS;
import jwiki.core.Wiki;

public class FixTypoBot {
	
	private static String errorLog;
//	private static Scanner input;

	/**
	 * Our main method. Loads up all the typos from the project page, extracts the pages from it,
	 * and then call our fixTypo method.
	 * 
	 * @param args Not used
	 */
	public static void main(String[] args) {
		
		try {
			Wiki wiki = new Wiki("TyAbot", "", "en.wikipedia.org");
			errorLog = "";
//			input = new Scanner(System.in);
			String allTypos = wiki.getPageText("Wikipedia:Database reports/Linked misspellings");
			
			Matcher getPagesMatcher = Pattern.compile("1=(.*)\\}\\}").matcher(allTypos);
			String page;
			while(getPagesMatcher.find()) {
				page = getPagesMatcher.group(1);
				System.out.println(page);
				fixTypo(page, wiki);
			}
			
			updateLog(wiki);
		} catch (LoginException e) {
			System.out.println("Failed to login; Check username and password!");
			System.exit(1);
		}

	}
	
	/**
	 * Gets the list of pages that link to page. Then goes through all of them and updates them with the correct page name. 
	 * We only update pages in the mainspace.
	 * 
	 * @param page The misspelled page we want to correct links from
	 * @param wiki Our wiki object
	 */
	public static void fixTypo(String page, Wiki wiki) {
		ArrayList<String> pages = wiki.whatLinksHere(page);
		
		try {
			System.out.println("Sleeping for 5 seconds");
			Thread.sleep(5000);
		} catch (InterruptedException e) {}
		
		pages = wiki.filterByNS(pages, NS.MAIN); // only interested in pages in the mainspace
		String correctPageName = getRedirectTarget(page, wiki);
		
		if(correctPageName == null) {
			errorLog += "# [[" + page + "]] - Unable to figure out redirect target.\n";
			return;
		}
		
		String currPageContent;
		for(String currPage: pages) {
			try {
				System.out.println("Sleeping for 5 seconds");
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
			
			// If the page links to itself, just keep going
			if(currPage.equalsIgnoreCase(page.replaceAll("_", " "))) {
				continue;
			}
			
			// Makes sure page allows for bots
			if(!allowsBots(currPage)) {
				errorLog += "# [[" + currPage + "]] - Links to page, but uses nobots\n";
				continue;
			}
			
			currPageContent = wiki.getPageText(currPage);
			
			// If we failed to get content, length will be zero. 
			// Error out and log it as we don't want to be blanking pages
			if(currPageContent.length() == 0) {
				errorLog += "#[[" + currPage + "]] - failed to get page content\n";
				continue;
			}
			
			// Do our actual replacements. Check for both spaces and underscores
			currPageContent = currPageContent.replaceAll(page, correctPageName);
			currPageContent = currPageContent.replaceAll(page.replaceAll("_", " "), correctPageName);
			
//			Debug bits
//			System.out.println(currPageContent);
//			System.out.println("Continue?");
//			if(input.nextLine() == "y") {
//				continue;
//			}
			
			wiki.edit(currPage, currPageContent, "Fixing linked misspelling - " + page + " to " + correctPageName + "! [[User talk:TyA|Report issues]]");
		}
	}
	
	/**
	 * Updates the bot's log page. The contents of the log is stored in the errorLog global variable
	 * 
	 * @param wiki Our wiki object
	 */
	public static void updateLog(Wiki wiki) {
		if(errorLog.length() == 0) {
			errorLog = "No issues today; yay!";
		}
		
		System.out.print(errorLog);
		wiki.addText("User:TyAbot/log", errorLog, "~~~~~", false);
	}
	
	/**
	 * Gets the target of a redirect page
	 * 
	 * @param page The redirect page
	 * @param wiki Wiki object
	 * @return A string if we can figure out redirect target, null otherwise
	 */
	public static String getRedirectTarget(String page, Wiki wiki) {
		Matcher getRedirectTarget = Pattern.compile("#\\s*[Rr][Ee][Dd][Ii][Rr][Ee][Cc][Tt]\\s*\\[\\[(.*)\\]\\]").matcher(wiki.getPageText(page));
		if(getRedirectTarget.find()) {
			return getRedirectTarget.group(1);
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if bots are allowed to edit this page. This code is copied from https://en.wikipedia.org/wiki/Template:Bots#Java
	 * 
	 * @param page The page we're checking
	 * @return boolean based on if we can or can't
	 */
	public static boolean allowsBots(String page) {
		return !page.matches("(?si).*\\{\\{(nobots|bots\\|(allow=none|deny=(.*?TyAbot.*?|all)|optout=all))\\}\\}.*");
	}
}
