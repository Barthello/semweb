package sem_web_project;

import org.apache.commons.codec.language.DoubleMetaphone;

/**
 * Created by flarestar on 08.04.17.
 */
public class Main {

    public static void main(String[] args) {

        Controller controller = new Controller();
        //controller.serialize(controller.wc.getHashtableIntRatings());
        //controller.generateRDF();
        //controller.createTDBdataset();
        controller.tdbQuery("SELECT ?Anime, ?Fansub-Group, ?Videogame {graph ?graph {?s ?p ?o}}");
        //controller.tdbQuery("SELECT ?Anime ?Fansub-Group ?Videogame where {?Anime swp#wasSubbedBy ?Fansub-Group.}");
    }
}
