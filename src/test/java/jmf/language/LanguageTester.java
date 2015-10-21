package jmf.language;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import jmf.Launcher;
import jmf.data.*;
import jmf.module.IFrameworkController;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @brief Test Class that controls the Java-Part of the interoperability test.
 * @details The LanguageTester-class controls the Java-part of the interoperability test between Java and C++.
 * It contains methods for several mini-tests that check if the information provided by the C++-counterpart was received correctly at Java-side.
 * In the same way it provides information to the network that needs to be recognized by the C++-part. The methods test state changes, message-transfer (pub/sub, req/rep) and dependencies.
 * The class synchronizes with the C++-counterpart through sending and receiving messages because exact timing is important for the success of the test.
 * However the test may fail when it is started with too many module instances so that one of both parts takes too long and a timeout is reached.
 * @author Matthias Blohm
 * @date created on 8/11/15.
 */
public class LanguageTester {
    /// number of the module instances that are started (there will be 4*numberModule_ active at the same time)
    private int numberModules = 1;

    /// Timeout for synchronizing with the Java-part. When a timeout is reached, the test fails.
    int waitingTimeout = 10000;

    /// Time to wait when a check-method fails before trying again
    int waitingInterval = 100;

    /// Arrays that contain the modules and their controllers (one vector for each type of module)
    ArrayList<IFrameworkController> controllers1 = new ArrayList<>();
    ArrayList<JavaModule> modules1 = new ArrayList<>();
    ArrayList<IFrameworkController> controllers2 = new ArrayList<>();
    ArrayList<JavaModule> modules2 = new ArrayList<>();

    /**
     * Arrays for storing and printing the test results at the end of the test
     */
    private boolean[] testResults = new boolean[7];
    private String[] testDescriptions = {
            "found PartnerModule With Correct ID and State == 'Active'",
            "received correct Module-Information of PartnerModule (Name, Version and AdditionalState) ",
            "started Modules with cross-dependencies correctly",
            "received correct Pub-Message of PartnerModules",
            "recognized StateChange of PartnerModules to Inactive",
            "received correct Request-Message of PartnerModules",
            "recognized StateChange of PartnerModules to Dead",
    };

    /** Constructor sets amount of starting instances and waitingTimeout with given values
     * @param numberModules The amount of instances that should be started of each type
     * @param waitingTimeout how long to wait for the C++-counterpart to provide correct results until test fails (in ms)
     */
    public LanguageTester(int numberModules, int waitingTimeout){
        this.numberModules = numberModules;
        this.waitingTimeout = waitingTimeout;
    }



    /**
     * Starts the test and controls the process flow.
     */
    public void startTest() throws Exception {

        //start JavaModule-Instances of Type 1 (no dependencies)
        for (int i = 0;i<numberModules;i++){
            Collection<ModuleDependency> dependencies = null;
            JavaModule module = new JavaModule(1,i+1,1,"JAVA_1_"+(i+1),dependencies);
            modules1.add(module);
            controllers1.add(Launcher.createInstance(module, Launcher.StartingOption.NO_PEER_DISCOVERY_WAIT));
        }

        // check if Modules started correctly
        int waitingTime = 0;
        boolean allStarted = false;
        while (waitingTime < waitingTimeout && allStarted == false ) {
            allStarted = true;
            for (int i = 0; i < numberModules; i++) {
                if (!modules1.get(i).isEnabled() || !controllers1.get(i).isStarted()){
                    allStarted = false;
                    break;
                }
            }
            Thread.sleep(waitingInterval);
            waitingTime += waitingInterval;
        }

        Thread.sleep(300);


        // when started, set Additional State
        for (int i = 0;i<numberModules;i++){
            String addInfoString = "J1"+(i+1);
            byte[] addI = addInfoString.getBytes();
            modules1.get(i).setAdditionalState(addI);
        }


        // Now check if CModules have been recognized correctly
        checkCModulesFound();

        // Now check if we received the correct Information of the CModules (Name, Version, Additional State)
        checkCModulesInfo();


        //start JavaModule-Instances of Type 2 (with dependencies)
        for (int i = 0;i<numberModules;i++){
            Collection<ModuleDependency> dependencies = new ArrayList<>();
            ModuleDependency dependency = new ModuleDependency(UnsignedInteger.fromIntBits(1),UnsignedInteger.fromIntBits(2));
            dependencies.add(dependency);

            JavaModule module = new JavaModule(2,i+1,1,"JAVA_2_"+(i+1),dependencies);
            modules2.add(module);
            controllers2.add(Launcher.createInstance(module, Launcher.StartingOption.NO_PEER_DISCOVERY_WAIT));
        }


        // check if the new JavaModules also started correctly - they depend on their CModule-counterpart (instance of Type 1)
        checkCrossDependencies();

        // now we send some Pub-Messages to tell the C++-Part, that we are ready to disable some modules
        for (int i = 0;i<numberModules;i++){
                modules1.get(i).doPub();
        }

        // now wait till we received all Pubs from C++ which tells us it is also ready to disable some of its own Modules
        checkReceivedPubs();

        // Now we try to disable our Modules of Type 2 again

        for (int i = 0;i<numberModules;i++) {
            controllers2.get(i).requestDisableModule();
        }
        // check if Modules were disabled correctly
        waitingTime = 0;
        boolean allInactive = false;
        while (waitingTime < waitingTimeout && allInactive == false ) {
            allInactive = true;
            for (int i = 0; i < numberModules; i++) {
                if (modules2.get(i).isEnabled()){
                    allInactive = false;
                    break;
                }
            }
            Thread.sleep(waitingInterval);
            waitingTime += waitingInterval;
        }

        // Now make sure our remaining active modules of Type 1 received the Inactive-State of the CModules of Type 2
        checkCModulesInactive();

        // Now we send some direct Request-Messages to tell C++ we recognized the inactive modules


        // Every JavaModule-Instance of Type 1 sends a request to every CModule-Instance of Type 1
        for (int i = 0;i<numberModules;i++){
            for (int j = 0;j<numberModules;j++) {
                int cModuleInstance = numberModules+j+1;
                ModuleUniqueId cId = new ModuleUniqueId(UnsignedInteger.fromIntBits(1), UnsignedLong.fromLongBits(cModuleInstance));
                modules1.get(i).doReq(cId);
            }
        }

        // Make sure we also received the Requests of C++ that indicates it has also seen our inactive modules and is now ready for the next part of the test
        checkReceivedRequests();

        // We now stop the JavaModlues of Type 2 completely. C++ will do the same
        for (int i = 0;i<numberModules;i++) {
            controllers2.get(i).requestStopInstance();
            controllers2.get(i).joinExecution();
        }

        // Let our remaining JavaModules of Type 1 check if they recognized the dead CModules of Type 2
        checkCModulesDead();




        // The Test ends and we shut down the other modules, too


        for (int i = 0;i<numberModules;i++) {
            controllers1.get(i).requestStopInstance();
            controllers1.get(i).joinExecution();
        }

        controllers1.clear();
        modules1.clear();

        controllers2.clear();
        modules2.clear();

        printTestResults();
    }

    /**
    *Checks if the started java modules are recognized as active
    * @throws InterruptedException if Thread.sleep is interrupted
     */

    private void checkCModulesFound() throws InterruptedException{
        boolean result = false;
        int waitingTime = 0;
        while (waitingTime < waitingTimeout && result == false  ){
            result = true;
            for (int i = 0;i<numberModules;i++) {
                for (int j = 0; j < numberModules; j++) {
                    int cModuleInstance = numberModules + j + 1;
                    ModuleUniqueId cId = new ModuleUniqueId(UnsignedInteger.fromIntBits(1), UnsignedLong.fromLongBits(cModuleInstance));
                    if (!(modules1.get(i).checkPartnerFound(cId))) {
                        result = false;
                        break;
                    }
                }
            }
            Thread.sleep(waitingInterval);
            waitingTime += waitingInterval;
        }
        testResults[0] = result;
    }

    /**
     *Checks if name, version and additional state are recognized correctly
     * @throws InterruptedException if Thread.sleep is interrupted
     */
    private void checkCModulesInfo() throws InterruptedException{
        boolean result = false;
        int waitingTime = 0;
        while (waitingTime < waitingTimeout &&  result == false ){
            result = true;
            for (int i = 0;i<numberModules;i++) {
                for (int j = 0; j < numberModules; j++) {
                    int cModuleInstance = numberModules + j + 1;
                    ModuleUniqueId cId = new ModuleUniqueId(UnsignedInteger.fromIntBits(1), UnsignedLong.fromLongBits(cModuleInstance));
                    String addInfoString = "C1" + cModuleInstance;
                    byte[] addI = addInfoString.getBytes();
                    if (!(modules1.get(i).checkPartnerInfos(cId, "C++_1_" + cModuleInstance, 2, addI))) {
                        result = false;
                        break;
                    }
                }
            }
            Thread.sleep(waitingInterval);
            waitingTime += waitingInterval;
        }
        testResults[1] = result;
    }

    /**
     *Checks if starting Java-modules with dependencies on C++-modules was successful
     * @throws InterruptedException if Thread.sleep is interrupted
     */
    private void checkCrossDependencies() throws InterruptedException{
        int waitingTime = 0;
        boolean result = false;
        while (waitingTime < waitingTimeout && result == false ){
            result = true;
            for (int i = 0;i<numberModules;i++){
                if (!controllers2.get(i).isStarted() || !modules2.get(i).isEnabled()){
                    result = false;
                }
            }
            Thread.sleep(waitingInterval);
            waitingTime+= waitingInterval;
        }

        testResults[2] = result;

    }

    /**
     *Checks if the correct number of C++-pubs was received
     * @throws InterruptedException if Thread.sleep is interrupted
     */
    private void checkReceivedPubs() throws InterruptedException{
        boolean result = false;
        int waitingTime = 0;

        while (waitingTime < waitingTimeout && result == false) {
            result = true;
            for (int i = 0; i < numberModules; i++) {
                if (!(modules1.get(i).receivedPub >= numberModules)) {
                    result = false;
                }
            }
            Thread.sleep(waitingInterval);
            waitingTime+= waitingInterval;
        }
        testResults[3] = result;

    }

    /**
     *Checks if the state change of C++-modules to 'Inactive' was detected
     * @throws InterruptedException if Thread.sleep is interrupted
     */
    private void checkCModulesInactive() throws InterruptedException{

        boolean result = false;
        int waitingTime = 0;

        while (waitingTime < waitingTimeout && result == false) {
            result = true;
            for (int i = 0; i < numberModules; i++) {
                for (int j = 0; j < numberModules; j++) {
                    int cModuleInstance = numberModules + j + 1;
                    ModuleUniqueId cId = new ModuleUniqueId(UnsignedInteger.fromIntBits(2), UnsignedLong.fromLongBits(cModuleInstance));
                    if (!(modules1.get(i).checkPartnerState(cId, ModuleLifecycleState.Inactive))) {
                        result = false;
                    }

                }
            }
            Thread.sleep(waitingInterval);
            waitingTime += waitingInterval;

        }
        testResults[4] = result;

    }

    /**
     *Checks if the correct number of C++-requests was received
     * @throws InterruptedException if Thread.sleep is interrupted
     */
    private void checkReceivedRequests() throws InterruptedException{
        int waitingTime = 0;
        boolean result = false;
        while (waitingTime < waitingTimeout && result == false) {
            result = true;
            for (int i = 0; i < numberModules; i++) {
                if (!(modules1.get(i).receivedReq >= numberModules)) {
                    result = false;
                }
            }
            Thread.sleep(waitingInterval);
            waitingTime += waitingInterval;
        }
        testResults[5] = result;
    }

    /**
     *Checks if the state change of C++-modules to 'Dead' was detected
     * @throws InterruptedException if Thread.sleep is interrupted
     */
    private void checkCModulesDead() throws InterruptedException{
        int waitingTime = 0;
        boolean result = false;
        while (waitingTime < waitingTimeout && result == false) {
            result = true;
            for (int i = 0; i < numberModules; i++) {
                for (int j = 0; j < numberModules; j++) {
                    int cModuleInstance = numberModules + j + 1;
                    ModuleUniqueId cId = new ModuleUniqueId(UnsignedInteger.fromIntBits(2), UnsignedLong.fromLongBits(cModuleInstance));
                    if (!(modules1.get(i).checkPartnerState(cId, ModuleLifecycleState.Dead))) {
                        result = false;
                    }
                }
            }
            Thread.sleep(waitingInterval);
            waitingTime += waitingInterval;
        }
        testResults[6] = result;
    }



    /**
     *Checks if the started java modules are recognized as active
     */
    private void printTestResults(){
        boolean success = true;
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("######### JAVA TEST RESULTS ############");
        for(int i = 0; i<testResults.length;i++){
            if (!testResults[i]){
                success = false;
                System.out.println("JAVA TEST NR. "+(i+1)+" failed:");
                System.out.println(testDescriptions[i]+ " returned FALSE");
                System.out.println();
            }
        }
        System.out.println();
        if (success){
            System.out.println("JAVA TEST RESULT: OK");
            System.out.println(testResults.length+" JAVA TESTS PASSED SUCCESSFULLY!");
        } else {
            System.out.println("JAVA TEST RESULT: FAILURE");
        }

        System.out.println("######### END OF JAVA TEST RESULTS ############");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        Assert.assertTrue(success);
    }


}
