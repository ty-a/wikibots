# mostly just saving this as an example of using mwclient and mwparserfromhell for template changes 
import mwclient
import mwparserfromhell

ua = "Ty doing things"

mw = mwclient.Site('runescape.wiki', path='/', clients_useragent=ua)
mw.login("TyBot", "")

template = mw.Pages['Template:Gifresize']

for page in template.embeddedin():
	wikicode = mwparserfromhell.parse(page.text())
	madeChanges = False 
	
	for temp in wikicode.filter_templates():
		if temp.name.matches("Gifresize"):
			madeChanges = True 
			replacement = "[[File:"
				
			replacement += unicode(temp.get(1))
			
			if(temp.has("height")):
				replacement += unicode(temp.get("height")).replace("height=", "") + "x"
				
			replacement += "|" + unicode(temp.get(2))
			replacement += "|thumb"
			
			if(temp.has("align")): 
				replacement += "|" + unicode(temp.get("align")).replace("align=", "")
			
			if(temp.has("link")):
				replacement += "|" + unicode(temp.get("link"))
				
			if(temp.has("caption")):
				replacement += "|" + unicode(temp.get("caption")).replace("caption=", "")
				
			replacement += "]]"
			
			print temp 
			print replacement 
			
			wikicode.replace(temp, replacement)
			
			
	if madeChanges:
		page.save(unicode(wikicode), "Replacing {{Gifresize}} with standard file syntax")
			
			
