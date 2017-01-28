package com.faceyspacies.ConvertToExternal;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;

public class ConvertToExternal {

	public static void main(String[] args) {
		ConvertToExternal c = new ConvertToExternal();
		try {
			c.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void start() throws IOException, LoginException {
		// We want to get all the pages that transclude {{2007 page}}, {{WP also}}, and {{RSC page}}
		
		
		// Then we will want to combine the list and remove duplicates
		
		// Then for each page, determine which templates it has and if each template has an parameter
		
		// Remove the old templates
		
		// Add {{External}} template
		
		// For safety, make a list of pages that already have the {{External}} template and depending how many, just do manually before
		// worrying about programming that edge case
		
		Wiki wiki = new Wiki("2007.runescape.wikia.com", "");
		wiki.setMarkBot(true);
		wiki.setThrottle(0);
		wiki.setUserAgent("TyBot converting templates to {{External}}");
		
		wiki.login("TyBot", "");
		
		String[] pages2007 = wiki.whatTranscludesHere("Template:RSW", 0);
		//String[] pagesRSC = wiki.whatTranscludesHere("Template:RSC page", 0);
		String[] pagesWP = wiki.whatTranscludesHere("Template:WP also", 0);
				
		Set<String> set = new HashSet<String>();
		set.addAll(Arrays.asList(pages2007));
		//set.addAll(Arrays.asList(pagesRSC));
		set.addAll(Arrays.asList(pagesWP));
		
		String[] pages = set.toArray(new String[0]);
		//System.out.println(pages.length);
		//System.exit(0);
		int i = 0;
		for(String page: pages) {
			i++;
			processPage(page, wiki);
			System.out.printf("Updating page %d of %d: Current title %s", i, pages.length, page);
		}
	}

	private void processPage(String page, Wiki wiki) throws IOException, LoginException {
		String text = wiki.getPageText(page);
		
		if(text.contains("{{External")) {
			return;
		}
		
		String template = "{{External";
		
		boolean hasOS = false;
		boolean hasRSC = false;
		boolean hasWP = false;
		
		//Pattern OSPattern = Pattern.compile("\\{\\{2007 page(|(.*))\\}\\}"); 
		Pattern OSPattern = Pattern.compile("\\{\\{RSW(|(.*))\\}\\}"); 
		//Pattern RSCPattern = Pattern.compile("\\{\\{RSC page(|(.*))\\}\\}");
		Pattern WPPattern = Pattern.compile("\\{\\{WP also(|(.*))\\}\\}");
		
		Matcher OSMatcher = OSPattern.matcher(text);
		//Matcher RSCMatcher = RSCPattern.matcher(text);
		Matcher WPMatcher = WPPattern.matcher(text);
		
		if(OSMatcher.find()) {
			hasOS = true;
			if(!OSMatcher.group(1).equals("")) {
				//template += "|os=" + OSMatcher.group(1).substring(1);
				template += "|rs=" + OSMatcher.group(1).substring(1);
			} else {
				//template += "|os";
				template += "|rs";
			}
		}
		/*
		if(RSCMatcher.find()) {
			hasRSC = true;
			if(!RSCMatcher.group(1).equals("")) {
				template += "|rsc=" + RSCMatcher.group(1).substring(1);
			} else {
				template += "|rsc";
			}
		}
		*/
		if(WPMatcher.find()) {
			hasWP = true;
			if(!WPMatcher.group(1).equals("")) {
				template += "|wp=" + WPMatcher.group(1).substring(1);
			} else {
				template += "|wp";
			}
		}
		
		template += "}}";
		
		// now remove all the templates we have
		
		if(hasOS) {
			text = text.replaceAll("\\{\\{RSW(|(.*))\\}\\}\n", "");
			text = text.replaceAll("\\{\\{RSW(|(.*))\\}\\}", "");
		}
		
		//if(hasRSC) {
		//	text = text.replaceAll("\\{\\{RSC page(|(.*))\\}\\}\n", "");
		//	text = text.replaceAll("\\{\\{RSC page(|(.*))\\}\\}", "");
		//}
		
		if(hasWP) {
			text = text.replaceAll("\\{\\{WP also(|(.*))\\}\\}\n", "");
			text = text.replaceAll("\\{\\{WP also(|(.*))\\}\\}", "");
		}
		
		// add new {{External}} template we have built
		
		text = template + "\n" + text;
		
		//System.out.println(page);
		//System.out.println(text);
		//System.out.println();
		
		wiki.edit(page, text, "Replacing {{RSW}} and {{WP also}}");
	}
	
	
}
