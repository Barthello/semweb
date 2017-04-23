package sem_web_project;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDFS;

import java.io.*;
import java.util.*;

/**
 * Created by flarestar on 08.04.17.
 */
public class Controller {

    WebCrawler wc;
    DoubleMetaphone dm;
    public Hashtable<String, List<String>> germanRatings, internationalRatings, fansubdb, fansubde;
    public Hashtable<String, String> ogdbgames;

    public Controller() {
        wc = new WebCrawler();
        dm = new DoubleMetaphone();
        germanRatings = (Hashtable) deserialize("/home/flarestar/IdeaProjects/SemanticWebProject/anisearch.obj");
        internationalRatings = (Hashtable) deserialize("/home/flarestar/IdeaProjects/SemanticWebProject/anidb.obj");
        fansubdb = (Hashtable) deserialize("/home/flarestar/IdeaProjects/SemanticWebProject/fansubdb.obj");
        fansubde = (Hashtable) deserialize("/home/flarestar/IdeaProjects/SemanticWebProject/fansubde.obj");
        ogdbgames = (Hashtable) deserialize("/home/flarestar/IdeaProjects/SemanticWebProject/ogdb.obj");
    }

    public void generateRDF(){
        OntModel m = ModelFactory.createOntologyModel();
        m.read("swp.owl");
        List<String> tempgrouplist = new ArrayList<String>();

        OntClass videogame = m.getOntClass("http://www.semanticweb.org/flarestar/ontologies/2017/3/swp#Videogame");
        OntClass anime = m.getOntClass("http://www.semanticweb.org/flarestar/ontologies/2017/3/swp#Anime");
        OntClass fansubgroup = m.getOntClass("http://www.semanticweb.org/flarestar/ontologies/2017/3/swp#Fansub-Group");
        DatatypeProperty germanRated = m.getDatatypeProperty("http://www.semanticweb.org/flarestar/ontologies/2017/3/swp#gotRatedInGermanyWith");
        DatatypeProperty interRated = m.getDatatypeProperty("http://www.semanticweb.org/flarestar/ontologies/2017/3/swp#gotRatedInternationallyWith");
        DatatypeProperty isLicensed = m.getDatatypeProperty("http://www.semanticweb.org/flarestar/ontologies/2017/3/swp#isLicensedinGermany");
        ObjectProperty wasSubbedBy = m.getObjectProperty("http://www.semanticweb.org/flarestar/ontologies/2017/3/swp#wasSubbedBy");
        ObjectProperty soundsLike = m.getObjectProperty("http://www.semanticweb.org/flarestar/ontologies/2017/3/swp#soundsLike");

        for (String key: germanRatings.keySet()) {
            Individual tempind = anime.createIndividual(germanRatings.get(key).get(1));
            tempind.addLabel(key,"en");
            tempind.addLiteral(germanRated, Double.valueOf(germanRatings.get(key).get(0)));
            if (internationalRatings.keySet().contains(key)) {
                tempind.addLiteral(interRated, Double.valueOf(internationalRatings.get(key).get(0)));
                internationalRatings.remove(key);
            }
            if (fansubdb.keySet().contains(key)) {
                if (fansubdb.get(key).get(0).equals("licensed")) {
                    tempind.addLiteral(isLicensed, true);
                } else {
                    tempind.addLiteral(isLicensed, false);
                }
                for (int i=1; i<=fansubdb.get(key).size()-1; i=i+2) {
                    Individual tempgroup = fansubgroup.createIndividual(fansubdb.get(key).get(i+1));
                    tempgroup.addLabel(fansubdb.get(key).get(i),"en");
                    tempind.addProperty(wasSubbedBy, tempgroup);
                    tempgrouplist.add(fansubdb.get(key).get(i));
                }
            }
            if (fansubde.keySet().contains(key)) {
                for (int i=0; i<=fansubde.get(key).size()-1; i=i+2) {
                    if (!tempgrouplist.contains(fansubde.get(key).get(i))) {
                        Individual tempgroup = fansubgroup.createIndividual(fansubde.get(key).get(i+1));
                        tempgroup.addLabel(fansubde.get(key).get(i),"en");
                        tempind.addProperty(wasSubbedBy, tempgroup);
                    }
                }
            }
            for (String gamekey: ogdbgames.keySet()) {
                if (dm.isDoubleMetaphoneEqual(key, gamekey)) {
                    Individual tempgame = videogame.createIndividual(ogdbgames.get(gamekey));
                    tempgame.addLabel(gamekey,"en");
                    tempind.addProperty(soundsLike, tempgame);
                }
            }
        }

        for (String key: internationalRatings.keySet()) {
            Individual tempind = anime.createIndividual(internationalRatings.get(key).get(1));
            tempind.addLabel(key,"en");
            tempind.addLiteral(interRated, Double.valueOf(internationalRatings.get(key).get(0)));
            if (fansubdb.keySet().contains(key)) {
                if (fansubdb.get(key).get(0).equals("licensed")) {
                    tempind.addLiteral(isLicensed, true);
                } else {
                    tempind.addLiteral(isLicensed, false);
                }
                for (int i=1; i<=fansubdb.get(key).size()-1; i=i+2) {
                    Individual tempgroup = fansubgroup.createIndividual(fansubdb.get(key).get(i+1));
                    tempgroup.addLabel(fansubdb.get(key).get(i),"en");
                    tempind.addProperty(wasSubbedBy, tempgroup);
                    tempgrouplist.add(fansubdb.get(key).get(i));
                }
            }
            if (fansubde.keySet().contains(key)) {
                for (int i=0; i<=fansubde.get(key).size()-1; i=i+2) {
                    if (!tempgrouplist.contains(fansubde.get(key).get(i))) {
                        Individual tempgroup = fansubgroup.createIndividual(fansubde.get(key).get(i+1));
                        tempgroup.addLabel(fansubde.get(key).get(i),"en");
                        tempind.addProperty(wasSubbedBy, tempgroup);
                    }
                }
            }
            for (String gamekey: ogdbgames.keySet()) {
                if (dm.isDoubleMetaphoneEqual(key, gamekey)) {
                    Individual tempgame = videogame.createIndividual(ogdbgames.get(gamekey));
                    tempgame.addLabel(gamekey,"en");
                    tempind.addProperty(soundsLike, tempgame);
                }
            }
        }

        String fileName = "swp.rdf";
        FileWriter out = null;
        try {
            out = new FileWriter( fileName );
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            m.write( out, "Turtle" );
        }
        finally {
            try {
                out.close();
            }
            catch (IOException closeException) {
                // ignore
            }
        }

        //m.write(System.out, "Turtle");

    }

    public static void serialize(Object root)
    {
        ObjectOutputStream out = null;
        FileOutputStream out_file = null;

        try {
            out_file = new FileOutputStream("anidb.obj");
            out = new ObjectOutputStream(out_file);

            out.writeObject(root);
            out.close();
        } catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    public static Object deserialize(String path)
    {
        ObjectInputStream in = null;
        FileInputStream in_file = null;

        Object o = null;

        try {
            in_file = new FileInputStream(path);
            in = new ObjectInputStream(in_file);

            o = in.readObject();
            in.close();
        } catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            System.exit(3);
        } catch (ClassNotFoundException ioe) {
            ioe.printStackTrace();
            System.exit(3);
        }

        return o;
    }
    //Verworfen: Stattdessen Stardog verwendet
    /*
    public void createTDBdataset() {
        Dataset dataset = TDBFactory.createDataset("tdb");
        Model tdb = dataset.getNamedModel("graph");
        try {
            tdb.read(new FileInputStream("swp.rdf"),null,"TTL");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        tdb.close();
        dataset.close();
    }

    public void tdbQuery(String querystring) {
        Dataset dataset = TDBFactory.createDataset("tdb");
        Query query = QueryFactory.create(querystring);
        QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
        ResultSet results = qexec.execSelect();
        ResultSetFormatter.out(results);
    }
    */
}
