package sem_web_project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by flarestar on 08.04.17.
 */
public class WebCrawler {

    private Hashtable<String, List<String>> crawlAnisearch() {
        Elements animenames = new Elements();
        Elements animeratings = new Elements();
        try {
            //296seiten
            for(int i=1; i<=296; i++) {
                Document animeindexpage = Jsoup.connect("https://www.anisearch.de/anime/index/?page="+i).get();
                Elements pagenames = animeindexpage.select("body div div main div table tbody tr th a");
                Elements pageratings = animeindexpage.select("body div div main div table tbody tr td[title^=Klarwert]");
                animenames.addAll(pagenames);
                animeratings.addAll(pageratings);
            }
            return generateAnisearchHashtable(animenames, animeratings);
        } catch (IOException e) {
            return generateAnisearchHashtable(animenames, animeratings);
        }
    }

    private Hashtable<String, List<String>> generateAnisearchHashtable(Elements names, Elements ratings) {
        Hashtable<String, List<String>> anisearchNamesAndRatings = new Hashtable<String, List<String>>();
        for(int i=0; i<=names.size()-1; i++) {
            if (ratings.eq(i).text().contains("(")) {
                String[] ratingbig = ratings.eq(i).text().split("\\s+");
                String commarating = ratingbig[0];
                String[] correctrating = commarating.split(Pattern.quote(","));
                String pointrating = correctrating[0] + "." + correctrating[1];
                List<String> anilist = new ArrayList<String>();
                anilist.add(Double.toString(Double.valueOf(pointrating)*2));
                anilist.add(names.eq(i).attr("abs:href"));
                anisearchNamesAndRatings.put(names.eq(i).text(), anilist);
            }
        }
        return anisearchNamesAndRatings;
    }

    private Hashtable<String, List<String>> crawlAnidb() {
        Elements animenames = new Elements();
        Elements animeratings = new Elements();
        try {
            //183seiten
            for (int i=0; i<=183; i++) {
                Document animeindexpage = Jsoup.connect("http://anidb.net/perl-bin/animedb.pl?show=animelist&page="+i).get();
                Elements pagenames = animeindexpage.select("td[data-label=Title] a");
                Elements pageratings = animeindexpage.select("td[data-label=Rating]");
                animenames.addAll(pagenames);
                animeratings.addAll(pageratings);
            }
            return generateAnidbHashtable(animenames, animeratings);
        } catch (IOException e) {
            return generateAnidbHashtable(animenames, animeratings);
        }
    }

    private Hashtable<String, List<String>> generateAnidbHashtable(Elements names, Elements ratings) {
        Hashtable<String, List<String>> anidbNamesAndRatings = new Hashtable<String, List<String>>();
        for (int i=0; i<=names.size()-1; i++) {
            if (!ratings.eq(i).text().contains("N")) {
                String[] ratingbig = ratings.eq(i).text().split("\\s+");
                String pointrating = ratingbig[0];
                List<String> anilist = new ArrayList<String>();
                anilist.add(pointrating);
                anilist.add(names.eq(i).attr("abs:href"));
                anidbNamesAndRatings.put(names.eq(i).text(), anilist);
            }
        }
        return anidbNamesAndRatings;
    }

    private Hashtable<String, List<String>> crawlFansubdb() {
        Elements animenames = new Elements();
        Elements license = new Elements();
        Elements fansubs = new Elements();
        try {
            //80seiten
            for (int i=1; i<=80; i++) {
                Document animeindexpage = Jsoup.connect("https://fansubdb.net/fansubs/seite/"+i).get();
                Elements pagenames = animeindexpage.select("td[class=name] a");
                Elements pagelicense = animeindexpage.select("td[class^=status]");
                Elements pagefansubs = animeindexpage.select("td[class=group]");
                animenames.addAll(pagenames);
                license.addAll(pagelicense);
                fansubs.addAll(pagefansubs);
            }
            return generateFansubdbHashtable(animenames, license, fansubs);
        } catch (IOException e) {
            return generateFansubdbHashtable(animenames, license, fansubs);
        }
    }

    private Hashtable<String, List<String>> generateFansubdbHashtable(Elements names, Elements license, Elements fansubs) {
        Hashtable<String, List<String>> fansubdbtable = new Hashtable<String, List<String>>();
        String licenseinfo;
        for (int i=0; i<=names.size()-1; i++) {
            licenseinfo = "unlicensed";
            if (license.eq(i).text().equals("lizenziert")) {
                licenseinfo = "licensed";
            }
            List<String> grouplist = new ArrayList<String>();
            grouplist.add(licenseinfo);
            grouplist = updateGrouplist(fansubs.eq(i), grouplist);

            //Handle multiple entries of same anime name, override license and add additional groups to list
            if (!fansubdbtable.containsKey(names.eq(i).text())) {
                fansubdbtable.put(names.eq(i).text(), grouplist);
            } else {
                List<String> templist = fansubdbtable.get(names.eq(i).text());
                if (licenseinfo.equals("licensed")) {
                    templist.set(0, "licensed");
                }
                templist = updateGrouplist(fansubs.eq(i), templist);
                fansubdbtable.put(names.eq(i).text(), templist);
            }
        }
        fansubdbtable = safecheckFansubdbHashtable(fansubdbtable);
        return fansubdbtable;
    }

    private List<String> updateGrouplist (Elements fansubs, List<String> grouplist) {
        if (!fansubs.text().equals("")) {
            if (fansubs.text().contains(",")) {
                String[] groups = fansubs.text().replaceAll("\\s+","").split(Pattern.quote(","));
                String[] urls = fansubs.html().replaceAll("\\s+","").split(Pattern.quote(","));
                for (int j=0; j<=groups.length-1; j++) {
                    grouplist.add(groups[j]);
                    grouplist.add(urls[j].split(Pattern.quote("\""))[1]);
                }
            } else {
                grouplist.add(fansubs.text());
                grouplist.add(fansubs.html().replaceAll("\\s+","").split(Pattern.quote("\""))[1]);
            }
        }
        return grouplist;
    }

    private Hashtable<String, List<String>> safecheckFansubdbHashtable(Hashtable<String, List<String>> inputtable) {
        Elements animenames = new Elements();
        Elements animelicensetypes = new Elements();
        try {
            //25seiten
            for (int i=1; i<=25; i++) {
                Document licensepage = Jsoup.connect("https://fansubdb.net/lizenzen/seite/"+i).get();
                Elements names = licensepage.select("tbody tr td a[href]");
                Elements licensetypes = licensepage.select("td[class=type]");
                animenames.addAll(names);
                animelicensetypes.addAll(licensetypes);
            }
        } catch (IOException e) {}
        for (int i=0; i<=animenames.size()-1; i++) {
            if (animelicensetypes.eq(i).first().html().split(Pattern.quote("\""))[1].equals("anime")) {
                if(inputtable.containsKey(animenames.eq(i).text())) {
                    List<String> temp = inputtable.get(animenames.eq(i).text());
                    if (temp.get(0).equals("unlicensed")) {
                        System.out.println("license status fixed:   "+animenames.eq(i).text());
                    }
                    temp.set(0, "licensed");
                    inputtable.put(animenames.eq(i).text(), temp);
                }
            }
        }
        return inputtable;
    }

    private Hashtable<String, List<String>> crawlFansubde() {
        Elements animenames = new Elements();
        Elements fansubs = new Elements();
        try {
            //58seiten
            for (int i=1; i<=58; i++) {
                Document animeindexpage = Jsoup.connect("http://fansub.de/fansubs.rhtml?display=5;seite="+i).get();
                Elements pagenames = animeindexpage.select("table[class=gruppe1] tbody tr td a[href^=fansub]");
                Elements pagefansubs = animeindexpage.select("td:has(a[href^=gruppe])");
                animenames.addAll(pagenames);
                fansubs.addAll(pagefansubs);
            }
            return generateGeneralFansubHashtable(animenames, fansubs);
        } catch (IOException e) {
            return generateGeneralFansubHashtable(animenames, fansubs);
        }
    }

    private Hashtable<String, List<String>> generateGeneralFansubHashtable(Elements names, Elements fansubs) {
        Hashtable<String, List<String>> fansubtable = new Hashtable<String, List<String>>();
        for (int i=0; i<=names.size()-1; i++) {
            List<String> grouplist = new ArrayList<String>();
            grouplist = updateGeneralGrouplist(fansubs.eq(i), grouplist);

            //Handle multiple entries of same anime name, override license and add additional groups to list
            if (!fansubtable.containsKey(names.eq(i).text())) {
                fansubtable.put(names.eq(i).text(), grouplist);
            } else {
                List<String> templist = fansubtable.get(names.eq(i).text());
                templist = updateGeneralGrouplist(fansubs.eq(i), templist);
                fansubtable.put(names.eq(i).text(), templist);
            }
        }
        return fansubtable;
    }

    private List<String> updateGeneralGrouplist (Elements fansubs, List<String> grouplist) {
        if (!fansubs.text().equals("")) {
            Elements tempurls = fansubs.select("a[href^=gruppe]");
            if (fansubs.text().contains(",")) {
                String[] groups = fansubs.text().replaceAll("\\s+","").split(Pattern.quote(","));
                for (int j=0; j<=groups.length-1; j++) {
                    grouplist.add(groups[j]);
                    grouplist.add(tempurls.eq(j).attr("abs:href"));
                }
            } else {
                grouplist.add(fansubs.text());
                grouplist.add(tempurls.eq(0).attr("abs:href"));
            }
        }
        return grouplist;
    }

    private Hashtable<String, String> crawlOgdb () {
        Elements gamenames = new Elements();
        String[] categories = {"number","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        try {
            for (int i=0; i<=categories.length-1; i++) {
                Document gameindexpage = Jsoup.connect("http://ogdb.eu/index.php?section=titleslist&show="+categories[i]).get();
                Elements pagenames = gameindexpage.select("tr td span a[href]");
                gamenames.addAll(pagenames);
            }
            return generateOgdbHashtable(gamenames);
        } catch (IOException e) {
            return generateOgdbHashtable(gamenames);
        }
    }

    private Hashtable<String, String> generateOgdbHashtable(Elements names) {
        Hashtable<String, String> gametable = new Hashtable<String, String>();
        for (int i=0; i<=names.size()-1; i++) {
            gametable.put(names.eq(i).text(), names.eq(i).attr("abs:href"));
        }
        return  gametable;
    }


    public Hashtable<String, List<String>> getHashtableGermanRatings() {return crawlAnisearch();}

    public Hashtable<String, List<String>> getHashtableIntRatings() {return crawlAnidb();}

    public Hashtable<String, List<String>> getHashtableFansubdb() {return crawlFansubdb();}

    public Hashtable<String, List<String>> getHashtableFansubde() {return crawlFansubde();}

    public Hashtable<String, String> getHashtableOGDB() {return crawlOgdb();}
}
