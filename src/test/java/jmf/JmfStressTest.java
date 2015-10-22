package jmf;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import jmf.data.ModuleDependency;
import jmf.data.ModuleUniqueId;
import jmf.module.AbstractModule;
import jmf.module.IFrameworkController;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the ZMF core with complicated scenarios
 * Created on 8/5/15
 * @author Jonas Grunert
 */
public class JmfStressTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(JmfStressTest.class);


    /**
     * Detailed test of state changes (enable, disable, stop)
     * @throws InterruptedException
     */
    @Test
    public void detailedTest() throws InterruptedException {
        System.out.println("----------------------- STARTING detailedTest -----------------------");

        ModuleUniqueId id_10 = new ModuleUniqueId(UnsignedInteger.valueOf(1), UnsignedLong.valueOf(0));
        /*ModuleUniqueId id_11 = new ModuleUniqueId(UnsignedInteger.valueOf(1), UnsignedLong.valueOf(1));
        ModuleUniqueId id_12 = new ModuleUniqueId(UnsignedInteger.valueOf(1), UnsignedLong.valueOf(2));
        ModuleUniqueId id_20 = new ModuleUniqueId(UnsignedInteger.valueOf(2), UnsignedLong.valueOf(0));
        ModuleUniqueId id_21 = new ModuleUniqueId(UnsignedInteger.valueOf(2), UnsignedLong.valueOf(1));
        ModuleUniqueId id_30 = new ModuleUniqueId(UnsignedInteger.valueOf(3), UnsignedLong.valueOf(0));
        ModuleUniqueId id_31 = new ModuleUniqueId(UnsignedInteger.valueOf(3), UnsignedLong.valueOf(1));*/

        TestModule module_10_0 = new TestModule(id_10, UnsignedInteger.valueOf(0), "module_10_0",
                new ArrayList<>());
        /*TestModule module_11_0 = new TestModule(id_11, UnsignedInteger.valueOf(0), "module_11_0",
                new ArrayList<>());
        TestModule module_12_1 = new TestModule(id_12, UnsignedInteger.valueOf(1), "module_12_1",
                new ArrayList<>());

        TestModule module_20_0 = new TestModule(id_20, UnsignedInteger.valueOf(0), "module_20_0",
                new ArrayList<>());
        TestModule module_21_1 = new TestModule(id_21, UnsignedInteger.valueOf(1), "module_21_1",
                new ArrayList<>());

        TestModule module_30_0 = new TestModule(id_30, UnsignedInteger.valueOf(0), "module_30_0",
                new ArrayList<>());
        TestModule module_31_1 = new TestModule(id_31, UnsignedInteger.valueOf(1), "module_31_1",
                new ArrayList<>());*/



        ////// Auto enable, disable, enable, requestStop
        // Test start with auto-enable of module_10_0
        IFrameworkController handle_10_0 = Launcher.createInstance(module_10_0, true, true, true, false);
        for (int i = 0; i < 10; i++) {
            if (module_10_0.isEnabled()) {
                break;
            }
            Thread.sleep(200);
        }
        Assert.assertTrue(module_10_0.isEnabled());

        // Test requestStopInstance of module_10_0
        handle_10_0.requestDisableModule();
        for (int i = 0; i < 10; i++) {
            if (!module_10_0.isEnabled()) {
                break;
            }
            Thread.sleep(200);
        }
        Assert.assertTrue(!module_10_0.isEnabled());

        // Test requestEnableModule of module_10_0
        handle_10_0.requestEnableModule();
        for (int i = 0; i < 10; i++) {
            if (module_10_0.isEnabled()) {
                break;
            }//
            Thread.sleep(200);
        }
        Assert.assertTrue(module_10_0.isEnabled());

        // Test requestStopInstance of module_10_0
        handle_10_0.requestStopInstance();
        handle_10_0.joinExecution();
        Assert.assertTrue(!module_10_0.isEnabled());



        ////// No Auto enable, enable, stop
        // Test start with auto-enable of module_10_0
        handle_10_0 = Launcher.createInstance(module_10_0, true, false, true, false);
        Thread.sleep(2000);
        Assert.assertTrue(!module_10_0.isEnabled());

        // Test requestEnableModule of module_10_0
        handle_10_0.requestEnableModule();
        for (int i = 0; i < 10; i++) {
            if (module_10_0.isEnabled()) {
                break;
            }
            Thread.sleep(200);
        }
        Assert.assertTrue(module_10_0.isEnabled());

        // Test stopInstance of module_10_0
        handle_10_0.stopInstance();
        Assert.assertTrue(!module_10_0.isEnabled());

        System.out.println("----------------------- FINISHED detailedTest -----------------------");
    }


    /**
     * Tests fast state changes and dependencies. Can be configured how stressful, by default simpler test.
     * @throws InterruptedException
     */
    @Test
    public void stressTestEnableDisableDependencies() throws InterruptedException {

        System.out.println("----------------------- STARTING stressTest -----------------------");

        int NUM_TEST_MODULES = 3;

    /*ModuleUniqueId id_10 = new ModuleUniqueId(1, 0);
    ModuleUniqueId id_11 = new ModuleUniqueId(1, 1);
    ModuleUniqueId id_12 = new ModuleUniqueId(1, 2);
    ModuleUniqueId id_20 = new ModuleUniqueId(2, 0);
    ModuleUniqueId id_21 = new ModuleUniqueId(2, 1);
    ModuleUniqueId id_30 = new ModuleUniqueId(3, 0);
    ModuleUniqueId id_31 = new ModuleUniqueId(3, 1);

    auto module_10_0 = new TestModule(id_10, 0, "module_10_0",
                                                    std::vector<zmf::ModuleDependency>());*/

        // Modules with no dependencies, auto enable
        List<AbstractModule> modules1 = new ArrayList<>();
        List<IFrameworkController> controller1 = new ArrayList<>();

        // Modules with dependencies to 1 - auto enable
        List<AbstractModule> modules2 = new ArrayList<>();
        List<IFrameworkController> controller2 = new ArrayList<>();

        // Modules with dependencies to 1, 2 - no auto enable
        List<AbstractModule> modules3 = new ArrayList<>();
        List<IFrameworkController> controller3 = new ArrayList<>();

        // Modules with dependencies to 1, 2, 3 - auto enable
        List<AbstractModule> modules4 = new ArrayList<>();
        List<IFrameworkController> controller4 = new ArrayList<>();

        // Modules with dependencies to 1, 2, 3, 4 - no auto enable
        List<AbstractModule> modules5 = new ArrayList<>();
        List<IFrameworkController> controller5 = new ArrayList<>();

        // Modules with dependencies to 1, 2, 3, 4, 5 - no auto enable
        List<AbstractModule> modules6 = new ArrayList<>();
        List<IFrameworkController> controller6 = new ArrayList<>();


        // Start modules 1
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            TestModule moduleTmp = new TestModule(new ModuleUniqueId(UnsignedInteger.valueOf(1), UnsignedLong.valueOf(i)), UnsignedInteger.fromIntBits(i),
                    "module_1:" + i, new ArrayList<>());
            modules1.add(moduleTmp);
            controller1.add(Launcher.createInstance(moduleTmp, true, true, true, false));
        }

        // Test if modules started
        Thread.sleep(100);
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(controller1.get(i).isStarted());
            Assert.assertTrue(!controller1.get(i).isStopped());
            Assert.assertTrue(modules1.get(i).isEnabled());
        }

        System.out.println("------------ Finished modules1 test ------------");


        // Start modules 2
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            List<ModuleDependency> dependenciesTmp = new ArrayList<>();
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(1), UnsignedInteger.valueOf(i)));
            TestModule moduleTmp = new TestModule(new ModuleUniqueId(UnsignedInteger.valueOf(2), UnsignedLong.valueOf(i)), UnsignedInteger.fromIntBits(i),
                    "module_2:" + i, dependenciesTmp);
            modules2.add(moduleTmp);
            controller2.add(Launcher.createInstance(moduleTmp, true, true, true, false));
        }

        // Test if modules started
        Thread.sleep(2000);
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(controller2.get(i).isStarted());
            Assert.assertTrue(!controller1.get(i).isStopped());
            Assert.assertTrue(modules2.get(i).isEnabled());
        }

        System.out.println("------------ Finished modules2 test ------------");


        // Start modules 3, no auto-enable
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            List<ModuleDependency> dependenciesTmp = new ArrayList<>();
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(1), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(2), UnsignedInteger.valueOf(i)));
            TestModule moduleTmp = new TestModule(new ModuleUniqueId(UnsignedInteger.valueOf(3), UnsignedLong.valueOf(i)), UnsignedInteger.fromIntBits(i),
                    "module_3:" + i, dependenciesTmp);
            modules3.add(moduleTmp);
            controller3.add(Launcher.createInstance(moduleTmp, true, false, true, false));
        }
        // Start modules 4, auto-enable
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            List<ModuleDependency> dependenciesTmp = new ArrayList<>();
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(1), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(2), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(3), UnsignedInteger.valueOf(i)));
            TestModule moduleTmp = new TestModule(new ModuleUniqueId(UnsignedInteger.valueOf(4), UnsignedLong.valueOf(i)), UnsignedInteger.fromIntBits(i),
                    "module_4:" + i, dependenciesTmp);
            modules4.add(moduleTmp);
            controller4.add(Launcher.createInstance(moduleTmp, true, true, true, false));
        }
        Thread.sleep(2000);

        // Test if modules not enabled (no auto enable for 3; 4 depends on 3)
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(controller3.get(i).isStarted());
            Assert.assertTrue(controller4.get(i).isStarted());
            Assert.assertTrue(!modules3.get(i).isEnabled());
            Assert.assertTrue(!modules4.get(i).isEnabled());
        }

        // Enable modules 3
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller3.get(i).requestEnableModule();
        }
        Thread.sleep(2000);

        // Test if modules enabled (enabled for 3; 4 depends on 3)
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(controller3.get(i).isStarted());
            Assert.assertTrue(controller4.get(i).isStarted());
            Assert.assertTrue(modules3.get(i).isEnabled());
            Assert.assertTrue(modules4.get(i).isEnabled());
        }

        System.out.println("------------ Finished modules 3/4 enable test ------------");


        // Disable modules 2
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller2.get(i).requestDisableModule();
        }
        Thread.sleep(2000);

        // Test if modules 1 enabled and 2,3,4 disabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(controller1.get(i).isStarted());
            Assert.assertTrue(controller2.get(i).isStarted());
            Assert.assertTrue(controller3.get(i).isStarted());
            Assert.assertTrue(controller4.get(i).isStarted());
            Assert.assertTrue(modules1.get(i).isEnabled());
            Assert.assertTrue(!modules2.get(i).isEnabled());
            Assert.assertTrue(!modules3.get(i).isEnabled());
            Assert.assertTrue(!modules4.get(i).isEnabled());
        }

        System.out.println("------------ Finished disable modules2 test ------------");


        // Re-enable modules 2,3 - 4 is auto-enabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            // Modules waiting for re-enable for dependencies but requestDisable stops this
            controller4.get(i).requestDisableModule();
            // Enable modules 2
            controller2.get(i).requestEnableModule();
        }
        Thread.sleep(2000);

        // Test if modules 1 enabled and 2,3,4 disabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(controller1.get(i).isStarted());
            Assert.assertTrue(controller2.get(i).isStarted());
            Assert.assertTrue(controller3.get(i).isStarted());
            Assert.assertTrue(controller4.get(i).isStarted());
            Assert.assertTrue(modules1.get(i).isEnabled());
            Assert.assertTrue(modules2.get(i).isEnabled());
            Assert.assertTrue(modules3.get(i).isEnabled());
            Assert.assertTrue(!modules4.get(i).isEnabled());
        }

        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller4.get(i).requestEnableModule();
        }
        Thread.sleep(2000);

        // Test if modules 1 enabled and 2,3,4 disabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(controller1.get(i).isStarted());
            Assert.assertTrue(controller2.get(i).isStarted());
            Assert.assertTrue(controller3.get(i).isStarted());
            Assert.assertTrue(controller4.get(i).isStarted());
            Assert.assertTrue(modules1.get(i).isEnabled());
            Assert.assertTrue(modules2.get(i).isEnabled());
            Assert.assertTrue(modules3.get(i).isEnabled());
            Assert.assertTrue(modules4.get(i).isEnabled());
        }

        System.out.println("------------ Finished re-enable modules2 test ------------");


        // Start modules 5, auto-enable
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            List<ModuleDependency> dependenciesTmp = new ArrayList<>();
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(1), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(2), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(3), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(4), UnsignedInteger.valueOf(i)));
            TestModule moduleTmp = new TestModule(new ModuleUniqueId(UnsignedInteger.valueOf(5), UnsignedLong.valueOf(i)), UnsignedInteger.fromIntBits(i),
                    "module_5:" + i, dependenciesTmp);
            modules5.add(moduleTmp);
            controller5.add(Launcher.createInstance(moduleTmp, true, false, true, false));
        }
        // Start modules 6, auto-enable
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            List<ModuleDependency> dependenciesTmp = new ArrayList<>();
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(1), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(2), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(3), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(4), UnsignedInteger.valueOf(i)));
            dependenciesTmp.add(new ModuleDependency(UnsignedInteger.valueOf(5), UnsignedInteger.valueOf(i)));
            TestModule moduleTmp = new TestModule(new ModuleUniqueId(UnsignedInteger.valueOf(6), UnsignedLong.valueOf(i)), UnsignedInteger.fromIntBits(i),
                    "module_6:" + i, dependenciesTmp);
            modules6.add(moduleTmp);
            controller6.add(Launcher.createInstance(moduleTmp, true, false, true, false));
        }
        Thread.sleep(2000);

        // Test if modules 1,2,3,4 enabled and 5,6 disabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(modules1.get(i).isEnabled());
            Assert.assertTrue(modules2.get(i).isEnabled());
            Assert.assertTrue(modules3.get(i).isEnabled());
            Assert.assertTrue(modules4.get(i).isEnabled());
            Assert.assertTrue(!modules5.get(i).isEnabled());
            Assert.assertTrue(!modules6.get(i).isEnabled());
        }

        System.out.println("------------ Finished start modules5 test ------------");


        // Enable modules 5
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller5.get(i).requestEnableModule();
        }
        Thread.sleep(2000);

        // Test if modules 1,2,3,4,5 enabled and 6 disabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(modules1.get(i).isEnabled());
            Assert.assertTrue(modules2.get(i).isEnabled());
            Assert.assertTrue(modules3.get(i).isEnabled());
            Assert.assertTrue(modules4.get(i).isEnabled());
            Assert.assertTrue(modules5.get(i).isEnabled());
            Assert.assertTrue(!modules6.get(i).isEnabled());
        }

        System.out.println("------------ Finished enable modules5 test ------------");


        // Enable modules 6
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller6.get(i).requestEnableModule();
        }
        Thread.sleep(2000);

        // Test if modules 1,2,3,4,5,6 enabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(modules1.get(i).isEnabled());
            Assert.assertTrue(modules2.get(i).isEnabled());
            Assert.assertTrue(modules3.get(i).isEnabled());
            Assert.assertTrue(modules4.get(i).isEnabled());
            Assert.assertTrue(modules5.get(i).isEnabled());
            Assert.assertTrue(modules6.get(i).isEnabled());
        }

        System.out.println("------------ Finished enable modules6 test ------------");


        // Stop modules 4, should also disable 5 and 6
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller4.get(i).requestStopInstance();
        }
        Thread.sleep(2000);

        // Test if modules 1,2,3,5,6 enabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(modules1.get(i).isEnabled());
            Assert.assertTrue(modules2.get(i).isEnabled());
            Assert.assertTrue(modules3.get(i).isEnabled());
            Assert.assertTrue(!modules5.get(i).isEnabled());
            Assert.assertTrue(!modules6.get(i).isEnabled());
        }

        System.out.println("------------ Finished stop modules4 test ------------");


        // Stop modules 2, should also disable 3
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller2.get(i).requestDisableModule();
        }
        Thread.sleep(2000);

        // Test if modules 1,2,3,5,6 enabled
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(modules1.get(i).isEnabled());
            Assert.assertTrue(!modules2.get(i).isEnabled());
            Assert.assertTrue(!modules3.get(i).isEnabled());
            Assert.assertTrue(!modules5.get(i).isEnabled());
            Assert.assertTrue(!modules6.get(i).isEnabled());
        }

        System.out.println("------------ Finished disable modules2 test ------------");


        // Stop modules 1,2,3,5,6
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller1.get(i).requestStopInstance();
            controller2.get(i).requestStopInstance();
            controller3.get(i).requestStopInstance();
            controller4.get(i).requestStopInstance();
            controller5.get(i).requestStopInstance();
            controller6.get(i).requestStopInstance();
        }
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            controller1.get(i).joinExecution();
            controller2.get(i).joinExecution();
            controller3.get(i).joinExecution();
            controller4.get(i).joinExecution();
            controller5.get(i).joinExecution();
            controller6.get(i).joinExecution();
        }

        // Test if modules 1,2,3,4,5,6 stopped
        for (int i = 0; i < NUM_TEST_MODULES; i++) {
            Assert.assertTrue(!controller1.get(i).isStarted());
            Assert.assertTrue(!controller2.get(i).isStarted());
            Assert.assertTrue(!controller3.get(i).isStarted());
            Assert.assertTrue(!controller4.get(i).isStarted());
            Assert.assertTrue(!controller5.get(i).isStarted());
            Assert.assertTrue(!controller6.get(i).isStarted());
            Assert.assertTrue(controller1.get(i).isStopped());
            Assert.assertTrue(controller2.get(i).isStopped());
            Assert.assertTrue(controller3.get(i).isStopped());
            Assert.assertTrue(controller4.get(i).isStopped());
            Assert.assertTrue(controller5.get(i).isStopped());
            Assert.assertTrue(controller6.get(i).isStopped());
        }

        System.out.println("------------ Joined all executions ------------");


        modules1.clear();
        modules2.clear();
        modules3.clear();
        modules4.clear();
        modules5.clear();
        modules6.clear();
        controller1.clear();
        controller2.clear();
        controller3.clear();
        controller4.clear();
        controller5.clear();
        controller6.clear();

        System.out.println("----------------------- Finished stressTest -----------------------");
    }
}