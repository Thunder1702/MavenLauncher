import ActionChangeAnalyze.ActionITreeAnalyze;
import ActionChangeAnalyze.ITreeBuilder;
import ActionChangeAnalyze.MavenLauncherCtModelsBuild;
import CallGraph.CallGraphResult;
import CallGraph.CallModel;
import CallGraph.CallNode;
import CallGraph.Invocation;
import SearchChangedMethodInCallGraph.GraphMethodSearcher;
import SearchChangedMethodInCallGraph.ResultTuple;
import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.CtModelImpl;

import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
//        String projectOldPath = "D:\\Dokumente\\1_Studium_0-Bachelorarbeit\\___________Working__________\\EasyCalc";
//        String projectNewPath = "D:\\Dokumente\\1_Studium_0-Bachelorarbeit\\___________Working__________\\EasyCalc_NEU";

        String projectOldPath = "D:\\Dokumente\\1_Studium_0-Bachelorarbeit\\___________Working__________\\test_case_selection\\Test_Projekte\\Calculator_alt";
        String projectNewPath = "D:\\Dokumente\\1_Studium_0-Bachelorarbeit\\___________Working__________\\test_case_selection\\Test_Projekte\\Calculator_neu";

//        String projectOldPath = "D:\\Dokumente\\1_Studium_0-Bachelorarbeit\\Testing_functionalities_FINAL\\Project_1_apache-commons-collection\\1_\\commons-collections_old";
//        String projectNewPath = "D:\\Dokumente\\1_Studium_0-Bachelorarbeit\\Testing_functionalities_FINAL\\Project_1_apache-commons-collection\\1_\\commons-collections_new";


        MavenLauncherCtModelsBuild ctModelsBuild = new MavenLauncherCtModelsBuild(projectOldPath,projectNewPath);
        ctModelsBuild.buildModels();

        ITreeBuilder iTreeBuilder = new ITreeBuilder(ctModelsBuild.getModelOld(),ctModelsBuild.getModelNew(),ctModelsBuild.getModelNewTest());

        CallModel callModel = new CallModel(ctModelsBuild.getModelNew(),ctModelsBuild.getModelNewTest(),iTreeBuilder.getCompleteModelNewITree());
//        callModel.outputModelInformation(modelNewTest, "modelNewTest");
//        callModel.outputModelInformation(modelNew,"modelNew");
//        callModel.outputModelInformation(modelOld,"modelOld");

        CallGraphResult callGraphResult = callModel.analyze();
        System.out.println("\nResult after building Call Graph..");
        System.out.println("Nodes...");
        for(CallNode node: callGraphResult.getAllNodes()){
            System.out.println("Node Classname: "+node.getClassName());
            if(node.getPrevious() != null){
                System.out.println("Previous: "+node.getPrevious().getClassName());
            }else {
                System.out.println("Previous: "+node.getPrevious());
            }
        }
        System.out.println("\nInvocations...");
        for(Invocation i: callGraphResult.getAllInvocations()){
            System.out.println("Method Signature: "+i.getMethodSignature());
            System.out.println("ParentMethodSignature: "+i.getParentMethodSignature());
            System.out.println("NextNode name: "+i.getNextNode().getClassName());
            System.out.println("Parent Node: "+i.getParentNode().getClassName());
            System.out.println("\n");
        }
        System.out.println("__________________________________Call Graph build finished__________________________________________");

        final MappingStore mappingsComp = new MappingStore();
        final Matcher matcher = new CompositeMatchers.ClassicGumtree(iTreeBuilder.getRootSpoonLeft(),iTreeBuilder.getRootSpoonRight(), mappingsComp);
        matcher.match();

        final ActionGenerator actionGenerator = new ActionGenerator(iTreeBuilder.getRootSpoonLeft(), iTreeBuilder.getRootSpoonRight(), matcher.getMappings());
        List<Action> actions = actionGenerator.generate();

        actions.forEach(System.out::println);
        System.out.println("______________________________________________________________");

        ActionITreeAnalyze actionITreeAnalyze  = new ActionITreeAnalyze(iTreeBuilder.getRootSpoonRight(),iTreeBuilder.getRootSpoonLeft(), actions, matcher);
        actionITreeAnalyze.analyzeActions();
        actionITreeAnalyze.printCheckForTestList();
        Set<ITree> testCheckList = actionITreeAnalyze.getCheckForTestList();

        GraphMethodSearcher graphMethodSearcher = new GraphMethodSearcher(testCheckList,callGraphResult);
        List<ResultTuple> resultTupleList = graphMethodSearcher.searchInCallGraph();
        System.out.println("To run again:");
        for(ResultTuple r: resultTupleList){
            System.out.println("Test Method "+r.getMethodName()+" in Class "+r.getClassName());
        }
    }
}