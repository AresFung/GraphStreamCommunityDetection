package th.main;

import java.io.IOException;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.util.parser.ParseException;
import th.algorithms.louvain.CommunityDetectionLouvain;
import th.algorithms.louvain.CommunityDetectionLouvain2;
import th.algorithms.propinquitydynamics.PropinquityDynamics;
import static th.algorithms.propinquitydynamics.utils.Utils.FractionWithNumberOfEdges;
import static th.algorithms.propinquitydynamics.utils.Utils.FractionWithTotalPropinquity;
import th.utils.ExtractCommunities;
import th.utils.Menu;
import th.utils.UIToolbox;
import th.utils.Utils;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class AppManager {

    public void printUserMenu() throws IOException, GraphParseException, ParseException {
        int methodSelection, datasetSelection;
        String datasetFile = null;
        boolean flag = true;

        while (flag) {
            methodSelection = Menu.printMenu();
            if(methodSelection != 0) {
                datasetSelection = Menu.printDatasetMenu();
                switch (datasetSelection) {
                    case 1:
                        datasetFile = "../data/polbooks.gml";
                        break;
                    case 2:
                        datasetFile = "../data/dolphins.gml";
                        break;
                    case 3:
                        datasetFile = "../data/karate.gml";
                        break;
                    case 4:
                        datasetFile = "../data/erdos02.gml";
                        break;
                    case 5:
                        datasetFile = "../data/erdos02-subset.gml";
                        break;
                    case 0:
                        return;
                }
            }
            switch (methodSelection) {
                case 1:
                    //Execute 1st function
                    ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin(datasetFile);
                    break;
                case 2:
                    //Execute 2st function
                    ErdozSubgraphwithOriginalPDAndTwoDisplays(datasetFile);
                    break;
                case 3:
                    //Execute 3rd function
                    LouvainExample(datasetFile);
                    break;
                case 0:
                    //Exit
                    return;
            }
        }
    }

    private void ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin(String datasetFile) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read(datasetFile);

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 10);

        pd.init(graph);

        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !pd.didAbsoluteConvergence()) {
            pd.compute();
            i++;
        }
        pd.applyFinalTopology();

        // Set the edge weight to fractions
        FractionWithNumberOfEdges(graph);
        //Utils.FractionWithTotalPropinquity(graph);

        // Use our custom extraction algorithm to retrive internal communities
        ExtractCommunities.MaxToMin(graph);
        UIToolbox.ColorCommunities(graph);
    }

    private void ErdozSubgraphwithOriginalPDAndTwoDisplays(String datasetFile) throws IOException, ParseException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read(datasetFile);

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 10);
        pd.init(graph);

        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !pd.didAbsoluteConvergence()) {
            pd.compute();
            i++;
        }

        pd.applyFinalTopology();

        // Erdos02-subgraph specific vertices
        Integer[] fixedCommunity = {10, 11};
        ExtractCommunities.BFS(graph, fixedCommunity);
        UIToolbox.ColorCommunities(graph);
        FractionWithTotalPropinquity(graph);

        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read("../data/erdos02-subset.gml");
        Utils.CopyCommunities(graph, originGraph);

        UIToolbox.ColorCommunities(originGraph);
    }

    private void LouvainExample(String datasetFile) throws IOException, GraphParseException {
        
        Graph graph = new DefaultGraph("Louvain");
        graph.read("../data/smalltest.dgs");
        graph.display();
        
        CommunityDetectionLouvain2 louvain = new CommunityDetectionLouvain2();
        //louvain.debugOn();
        
        louvain.init(graph);
        Modularity modularity = new Modularity("community", "weight");
        modularity.init(graph);
        
        double globalNewQ;
        double globalMaxQ = -0.5;
        louvain.findCommunities(graph);         // First Phase
        globalNewQ = modularity.getMeasure();   // Get new global modularity value
        
        Graph folded = null;
        while (globalNewQ > globalMaxQ) {       // As long as the modularity is not the maximum
            globalMaxQ = globalNewQ;
            folded = louvain.foldingCommunities(graph); // Second Phase (folding)
            louvain.findCommunities(folded);    // and get the new modularity
            globalNewQ = modularity.getMeasure();   // Get new global modularity value
        }
        louvain.printFinalGraph(folded,graph,globalMaxQ,0.0); // After reaching the maximum modularity, 
        // print the graph on the screen.
    }

    public static void WriteToFileExample() throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.read("../data/erdos02-subset.gml");

        graph.removeNode(7);

        Utils.ExportGraphIntoGML(graph, "../data/export");
    }
}
